package cn.daenx.myauth.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 功能票据（feature token）签名与规则评估工具。
 *
 * 设计要点：
 * 1. 私钥只存在于 MyAuth 服务端的 soft_sign_key 表，客户端只内嵌公钥，
 *    因此破解者即使搭假服务器也签不出合法票据。
 * 2. 服务端直接下发「功能开关结论 features」，而不是下发 point 让客户端自己算。
 *    客户端伪造登录响应里的 point 不再具有任何意义。
 * 3. 签名对象是自定义的 canonical 字符串（用 | 分隔），不是 JSON 对象，
 *    避免 Java / Go 两端 JSON 序列化顺序不一致导致验签失败。
 *
 * @author Shell
 */
public class FeatureSignUtil {

    /** 与客户端 internal/myauth/feature_config.go 的 knownFeatures 保持一致 */
    public static final List<String> KNOWN_FEATURES = List.of(
            "palschema", "pal-monitor", "admin", "boss_operations",
            "points_system", "webshop", "cloud_pack");

    private FeatureSignUtil() {
    }

    /** 生成密钥对的结果；privateKey 绝不返回给前端或客户端 */
    public static class KeyPairResult {
        public final String kid;
        public final String publicKey;
        public final String privateKey;

        KeyPairResult(String kid, String publicKey, String privateKey) {
            this.kid = kid;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }

    /**
     * 生成一对 Ed25519 密钥。
     * public_key 输出 raw 32 字节的 Base64（Go 的 ed25519.PublicKey 可直接使用），
     * private_key 输出 PKCS8 的 Base64（仅 Java 侧读回签名用）。
     */
    public static KeyPairResult generateKeyPair() throws GeneralSecurityException {
        KeyPair keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        // Java 的 getEncoded() 返回 X.509 SubjectPublicKeyInfo，末尾 32 字节才是 raw 公钥
        byte[] x509 = keyPair.getPublic().getEncoded();
        byte[] rawPublic = Arrays.copyOfRange(x509, x509.length - 32, x509.length);
        String publicKey = Base64.getEncoder().encodeToString(rawPublic);
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String kid = "k" + Long.toHexString(System.currentTimeMillis())
                + UUID.randomUUID().toString().substring(0, 4);
        return new KeyPairResult(kid, publicKey, privateKey);
    }

    /** 用 Base64(PKCS8) 私钥对 canonical payload 做 Ed25519 签名，返回 Base64 签名串 */
    public static String sign(String payload, String privateKeyBase64) throws GeneralSecurityException {
        byte[] der = Base64.getDecoder().decode(privateKeyBase64);
        PrivateKey privateKey = KeyFactory.getInstance("Ed25519")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(privateKey);
        signature.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * 构造待签名的 canonical 字符串。
     * 格式：v1|kid|skey|user|point|exp|nonce|deviceCode|features|disabledModules
     * features 形如 "admin=1,boss_operations=0,..."（按 key 字典序），
     * 客户端按同样规则解析并以该串整体验签，任何字段被篡改都会导致验签失败。
     */
    public static String buildPayload(String kid, String skey, String user, int point,
                                      long exp, String nonce, String deviceCode,
                                      Map<String, Boolean> features,
                                      List<String> disabledModules) {
        StringJoiner sj = new StringJoiner("|");
        sj.add("v1");
        sj.add(nullToEmpty(kid));
        sj.add(nullToEmpty(skey));
        sj.add(nullToEmpty(user));
        sj.add(String.valueOf(point));
        sj.add(String.valueOf(exp));
        sj.add(nullToEmpty(nonce));
        sj.add(nullToEmpty(deviceCode));
        sj.add(canonicalFeatures(features));
        sj.add(String.join(",", disabledModules == null ? List.of() : disabledModules));
        return sj.toString();
    }

    /** features 按 key 字典序输出 k=1 / k=0，保证跨语言一致 */
    public static String canonicalFeatures(Map<String, Boolean> features) {
        TreeMap<String, Boolean> sorted = new TreeMap<>(features);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> e : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(e.getKey()).append("=").append(Boolean.TRUE.equals(e.getValue()) ? "1" : "0");
        }
        return sb.toString();
    }

    /**
     * 按 feature_rules JSON 和账号实时点数计算功能开关。
     * 规则语义与客户端 evaluateRule 完全一致：
     *   1. deniedUsers 命中 → false
     *   2. allowedUsers 命中 → true
     *   3. point < minPoint → false
     *   4. 其余 → true
     * 未配置规则的功能使用 defaultVisible。
     *
     * @param disabledModulesOut 出参，回填全局禁用模块列表
     */
    public static Map<String, Boolean> evaluateFeatures(String rulesJson, String user, int point,
                                                        List<String> disabledModulesOut) {
        boolean defaultVisible = true;
        JSONObject root = null;
        if (rulesJson != null && !rulesJson.isBlank()) {
            try {
                root = JSON.parseObject(rulesJson);
                Boolean dv = root.getBoolean("defaultVisible");
                if (dv != null) {
                    defaultVisible = dv;
                }
                JSONArray disabled = root.getJSONArray("disabledModules");
                if (disabled != null && disabledModulesOut != null) {
                    for (int i = 0; i < disabled.size(); i++) {
                        disabledModulesOut.add(String.valueOf(disabled.get(i)));
                    }
                }
            } catch (Exception e) {
                // 规则配置写错时保守放行基础功能，但日志由调用方记录
                root = null;
            }
        }
        JSONObject rules = root == null ? null : root.getJSONObject("features");

        Map<String, Boolean> result = new LinkedHashMap<>();
        for (String feature : KNOWN_FEATURES) {
            if (rules == null || !rules.containsKey(feature)) {
                result.put(feature, defaultVisible);
                continue;
            }
            JSONObject rule = rules.getJSONObject(feature);
            if (rule == null) {
                result.put(feature, defaultVisible);
                continue;
            }
            boolean visible;
            if (contains(rule.getJSONArray("deniedUsers"), user)) {
                visible = false;
            } else if (contains(rule.getJSONArray("allowedUsers"), user)) {
                visible = true;
            } else {
                int minPoint = rule.getIntValue("minPoint");
                visible = point >= minPoint;
            }
            // 全局禁用模块对所有人生效
            if (disabledModulesOut != null && disabledModulesOut.contains(feature)) {
                visible = false;
            }
            result.put(feature, visible);
        }
        return result;
    }

    private static boolean contains(JSONArray array, String value) {
        if (array == null || value == null) {
            return false;
        }
        for (int i = 0; i < array.size(); i++) {
            if (value.equals(String.valueOf(array.get(i)))) {
                return true;
            }
        }
        return false;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** 供后台展示用的规则默认值（新增软件时可预置） */
    public static String defaultRulesJson() {
        JSONObject root = new JSONObject(true);
        root.put("supportText", "感谢你的支持，这是我们持续维护的动力");
        root.put("defaultVisible", true);
        root.put("disabledModules", new ArrayList<>());
        JSONObject features = new JSONObject(true);
        for (String f : KNOWN_FEATURES) {
            JSONObject rule = new JSONObject(true);
            rule.put("minPoint", 0);
            rule.put("allowedUsers", new ArrayList<>());
            rule.put("deniedUsers", new ArrayList<>());
            features.put(f, rule);
        }
        root.put("features", features);
        return root.toJSONString();
    }
}
