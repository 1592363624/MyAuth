package cn.daenx.myauth.main.service;

import cn.daenx.myauth.util.FeatureSignUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能票据签名密钥存储（独立表 ma_soft_sign_key）。
 *
 * 用 JdbcTemplate 而不是挂在 Soft 实体上，原因：
 * web/SoftController.getSoftList / getSoft 会把整个 Soft 实体序列化给后台浏览器，
 * 密钥若挂在 soft 表上会随列表接口明文带出。独立表天然不会被这些接口查到。
 *
 * @author Shell
 */
@Slf4j
@Service
public class FeatureSignKeyService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 启动时自动建表（幂等）。
     * 表已存在时是无操作；数据库账号没有 DDL 权限时只记日志、不阻塞启动，
     * 此时仍需手动执行 resources/db/feature_sign_migration.sql。
     */
    @PostConstruct
    public void initSchema() {
        try {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS ma_soft_sign_key (" +
                            "id INT NOT NULL AUTO_INCREMENT," +
                            "from_soft_id INT NOT NULL COMMENT '对应 ma_soft.id'," +
                            "kid VARCHAR(32) NOT NULL COMMENT '密钥ID'," +
                            "public_key VARCHAR(64) NOT NULL COMMENT 'Ed25519 公钥 raw32 Base64'," +
                            "private_key VARCHAR(128) NOT NULL COMMENT 'Ed25519 私钥 PKCS8 Base64'," +
                            "feature_rules TEXT NULL COMMENT '功能规则 JSON'," +
                            "status TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用'," +
                            "add_time INT NOT NULL," +
                            "PRIMARY KEY (id)," +
                            "UNIQUE KEY uk_soft_kid (from_soft_id, kid)," +
                            "KEY idx_soft_status (from_soft_id, status)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能票据签名密钥'");
        } catch (Exception e) {
            log.warn("自动建表 ma_soft_sign_key 失败（可能数据库账号无 DDL 权限），" +
                    "请手动执行 resources/db/feature_sign_migration.sql: {}", e.getMessage());
        }
    }

    /** 服务端内部使用的完整记录（含私钥），禁止出现在任何 HTTP 返回值中 */
    public static class SignKeyRecord {
        public long id;
        public int softId;
        public String kid;
        public String publicKey;
        public String privateKey;
        public String featureRules;
    }

    /**
     * 取该软件当前启用的密钥；不存在时自动生成一把。
     *
     * 自动生成的动机：功能票据是「默认全开」的设计（defaultVisible=true、minPoint=0），
     * 对还没打算做付费分级的软件来说，配不配密钥在行为上没有区别，
     * 于是大多数接入方永远不会去后台点「生成新密钥」。
     * 但客户端在缺失密钥时会静默回退到旧的点数逻辑，
     * 等运营哪天想收紧功能，才发现这个软件从第一天起就没有签名保护。
     * 这里改成首次签发时自动补一把，让「有密钥」成为默认状态。
     *
     * @param autoCreate true 时缺失即生成（客户端申请票据路径使用）
     */
    public SignKeyRecord getActiveKey(int softId, boolean autoCreate) {
        SignKeyRecord rec = getActiveKey(softId);
        if (rec != null || !autoCreate) {
            return rec;
        }
        try {
            generateAndSave(softId);
            log.info("软件 {} 尚无功能签名密钥，已自动生成默认密钥", softId);
        } catch (Exception e) {
            log.error("自动生成功能签名密钥失败: softId={}", softId, e);
            return null;
        }
        return getActiveKey(softId);
    }

    /** 取该软件当前启用的密钥（按 id 倒序，最新生成的一把生效） */
    public SignKeyRecord getActiveKey(int softId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, from_soft_id, kid, public_key, private_key, feature_rules " +
                        "FROM ma_soft_sign_key WHERE from_soft_id = ? AND status = 1 ORDER BY id DESC LIMIT 1",
                softId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> r = rows.get(0);
        SignKeyRecord rec = new SignKeyRecord();
        rec.id = ((Number) r.get("id")).longValue();
        rec.softId = ((Number) r.get("from_soft_id")).intValue();
        rec.kid = str(r.get("kid"));
        rec.publicKey = str(r.get("public_key"));
        rec.privateKey = str(r.get("private_key"));
        rec.featureRules = str(r.get("feature_rules"));
        return rec;
    }

    /**
     * 为该软件生成一对新密钥并写入数据库。
     * 旧的启用记录保留（status 改为 0），便于按 kid 灰度轮换。
     *
     * 功能规则沿用上一把密钥的配置：功能项是运营辛苦填的，
     * 轮换密钥只该换公私钥，不该把功能声明清空。
     *
     * @return 只含 kid 与 publicKey 的安全信息，私钥不返回
     */
    public Map<String, Object> generateAndSave(int softId) throws GeneralSecurityException {
        FeatureSignUtil.KeyPairResult pair = FeatureSignUtil.generateKeyPair();
        SignKeyRecord previous = getActiveKey(softId);
        String rules = (previous != null && previous.featureRules != null
                && !previous.featureRules.isBlank())
                ? previous.featureRules
                : FeatureSignUtil.defaultRulesJson();
        jdbcTemplate.update("UPDATE ma_soft_sign_key SET status = 0 WHERE from_soft_id = ? AND status = 1", softId);
        jdbcTemplate.update(
                "INSERT INTO ma_soft_sign_key " +
                        "(from_soft_id, kid, public_key, private_key, feature_rules, status, add_time) " +
                        "VALUES (?, ?, ?, ?, ?, 1, ?)",
                softId, pair.kid, pair.publicKey, pair.privateKey,
                rules,
                (int) (System.currentTimeMillis() / 1000));
        Map<String, Object> safe = new LinkedHashMap<>();
        safe.put("softId", softId);
        safe.put("kid", pair.kid);
        safe.put("publicKey", pair.publicKey);
        return safe;
    }

    /**
     * 列出该软件的公钥与规则（后台展示/编辑用，不含私钥）。
     * featureRules 不是敏感信息（客户端本来就能看到功能开关），可以回显。
     */
    public List<Map<String, Object>> listPublicKeys(int softId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, kid, public_key, feature_rules, status, add_time FROM ma_soft_sign_key " +
                        "WHERE from_soft_id = ? ORDER BY id DESC", softId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> safe = new LinkedHashMap<>();
            safe.put("id", r.get("id"));
            safe.put("kid", r.get("kid"));
            safe.put("publicKey", r.get("public_key"));
            safe.put("featureRules", r.get("feature_rules"));
            safe.put("status", r.get("status"));
            safe.put("addTime", r.get("add_time"));
            result.add(safe);
        }
        return result;
    }

    /** 更新功能规则 JSON（后台维护规则，服务端据此计算 features） */
    public int updateRules(long keyId, int softId, String rulesJson) {
        return jdbcTemplate.update(
                "UPDATE ma_soft_sign_key SET feature_rules = ? WHERE id = ? AND from_soft_id = ?",
                rulesJson, keyId, softId);
    }

    /**
     * 启用/停用某把密钥。
     * 同一软件同时只允许一把启用（客户端只会认 hardcoded 里的那把公钥），
     * 启用一把时把该软件其余密钥置为停用。
     *
     * 先启用目标再停用其余，避免中途失败出现「没有任何启用密钥」的空窗。
     *
     * @return 受影响行数，0 表示密钥记录不存在
     */
    public int updateStatus(long keyId, int softId, int status) {
        int rows = jdbcTemplate.update(
                "UPDATE ma_soft_sign_key SET status = ? WHERE id = ? AND from_soft_id = ?",
                status, keyId, softId);
        if (rows > 0 && status == 1) {
            jdbcTemplate.update(
                    "UPDATE ma_soft_sign_key SET status = 0 WHERE from_soft_id = ? AND status = 1 AND id <> ?",
                    softId, keyId);
        }
        return rows;
    }

    /** 删除某把密钥（物理删除，公钥/私钥记录一并清除） */
    public int deleteKey(long keyId, int softId) {
        return jdbcTemplate.update(
                "DELETE FROM ma_soft_sign_key WHERE id = ? AND from_soft_id = ?",
                keyId, softId);
    }

    /**
     * 从数据库读取实时点数。
     * 不用 @UserLogin 放进 Redis 的 obj_user.point：那份是登录时缓存的快照，
     * 后台充值/扣费后不一定同步刷新，直接拿它签票据会让点数调整延迟生效。
     * 查询失败时回退到 0（保守，不误放权限），由调用方记录日志。
     */
    public int getRealtimePoint(int userId) {
        try {
            Integer p = jdbcTemplate.queryForObject(
                    "SELECT point FROM ma_user WHERE id = ?", Integer.class, userId);
            return p == null ? 0 : p;
        } catch (Exception e) {
            return 0;
        }
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
