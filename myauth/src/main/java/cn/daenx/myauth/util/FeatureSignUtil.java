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

    /**
     * 功能标识允许的字符：小写字母、数字、下划线、中划线。
     * 之所以要限制，是因为功能标识要参与 canonical 签名字符串（用 | 和 , 分隔），
     * 标识里若出现 , 或 | 会破坏分隔结构，导致客户端解析出错误的 features 表。
     */
    private static final java.util.regex.Pattern FEATURE_KEY_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");

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
     *
     * 功能清单是**数据驱动**的：以规则 JSON 里 features 对象的 key 为准，
     * 而不是平台内置一张固定表。MyAuth 是通用验证平台，
     * 不同软件的功能集合完全不同（一个游戏工具和一套 ERP 不可能共用同一批功能标识），
     * 因此功能项必须由每个软件自己在后台声明。
     *
     * 规则语义：
     *   1. deniedUsers 命中 → false
     *   2. allowedUsers 命中 → true
     *   3. point < minPoint → false
     *   4. 其余 → true
     * 未在 features 里声明的功能不会出现在票据中；
     * 客户端遇到票据里没有的功能标识时会放行，所以「没声明」等于「没上锁」，
     * 由调用方负责在未配置功能项时拒绝签发票据。
     *
     * @param disabledModulesOut 出参，回填全局禁用模块列表
     */
    public static Map<String, Boolean> evaluateFeatures(String rulesJson, String user, int point,
                                                        List<String> disabledModulesOut) {
        boolean defaultVisible = true;
        JSONObject rules = null;
        if (rulesJson != null && !rulesJson.isBlank()) {
            try {
                JSONObject root = JSON.parseObject(rulesJson);
                if (root == null) {
                    root = new JSONObject();
                }
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
                rules = root.getJSONObject("features");
            } catch (Exception e) {
                // 规则配置写错时按空规则处理，由调用方日志告警
                rules = null;
            }
        }

        Map<String, Boolean> result = new LinkedHashMap<>();
        if (rules != null) {
            for (String feature : rules.keySet()) {
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
                result.put(feature, visible);
            }
        }
        // 全局禁用模块即使没在 features 里声明也要下发 false，
        // 否则客户端拿不到该 key 会按「未知功能」放行。
        if (disabledModulesOut != null) {
            for (String module : disabledModulesOut) {
                result.put(module, false);
            }
        }
        return result;
    }

    /** 从规则 JSON 里取出已声明的功能标识列表，用于校验与日志 */
    public static List<String> extractFeatureKeys(String rulesJson) {
        List<String> keys = new ArrayList<>();
        if (rulesJson == null || rulesJson.isBlank()) {
            return keys;
        }
        try {
            JSONObject root = JSON.parseObject(rulesJson);
            JSONObject rules = root == null ? null : root.getJSONObject("features");
            if (rules != null) {
                keys.addAll(rules.keySet());
            }
        } catch (Exception ignored) {
            // 解析失败返回空列表，由调用方决定如何处理
        }
        return keys;
    }

    /**
     * 校验规则 JSON 的结构，返回错误信息；合法时返回 null。
     * 后台保存前调用，避免把破坏签名分隔符的标识写进库。
     */
    public static String validateRules(String rulesJson) {
        if (rulesJson == null || rulesJson.isBlank()) {
            return "规则不能为空";
        }
        JSONObject root;
        try {
            root = JSON.parseObject(rulesJson);
        } catch (Exception e) {
            return "规则不是合法 JSON：" + e.getMessage();
        }
        if (root == null) {
            return "规则不是合法 JSON 对象";
        }
        JSONObject features = root.getJSONObject("features");
        if (features == null) {
            return "规则缺少 features 对象";
        }
        for (String key : features.keySet()) {
            if (!FEATURE_KEY_PATTERN.matcher(key).matches()) {
                return "功能标识「" + key + "」不合法，只允许字母、数字、下划线、中划线，长度 1-32";
            }
            JSONObject rule = features.getJSONObject(key);
            if (rule == null) {
                return "功能「" + key + "」的规则必须是对象";
            }
            Object minPoint = rule.get("minPoint");
            if (minPoint != null && !(minPoint instanceof Number)) {
                return "功能「" + key + "」的解锁点数必须是数字";
            }
        }
        JSONArray disabled = root.getJSONArray("disabledModules");
        if (disabled != null) {
            for (int i = 0; i < disabled.size(); i++) {
                String module = String.valueOf(disabled.get(i));
                if (!FEATURE_KEY_PATTERN.matcher(module).matches()) {
                    return "全局禁用模块「" + module + "」不合法";
                }
            }
        }
        return null;
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

    /**
     * 新建密钥时的规则默认值。
     * features 留空：功能项由每个软件自己在后台声明，平台不预置任何功能标识。
     */
    public static String defaultRulesJson() {
        JSONObject root = new JSONObject(true);
        root.put("supportText", "感谢你的支持，这是我们持续维护的动力");
        root.put("defaultVisible", true);
        root.put("disabledModules", new ArrayList<>());
        root.put("features", new JSONObject(true));
        return root.toJSONString();
    }
}
