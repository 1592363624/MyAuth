-- ============================================================================
-- 功能票据签名密钥：建表 + 写入（幂等，可重复执行）
--
-- 适用：MyAuth 服务端已部署，需要为 Palworld Server Tool 这个软件
--       （skey = 9ace9f1a-f2e8-4161-866c-dbc19c1cac75）配置签名密钥。
--
-- 执行方式（在 MyAuth 服务器上）：
--   mysql -u myauthtest -p myauthtest < feature_sign_key_seed.sql
--
-- 说明：
--   - 服务端启动时已会自动建表（FeatureSignKeyService.initSchema），
--     本脚本的 CREATE TABLE 只是兜底，重复执行无副作用。
--   - 下面按 skey 自动查 ma_soft.id，不需要手填软件 ID。
--   - 如果查不到该 skey，脚本会输出 soft_id = NULL 且不插入任何记录。
-- ============================================================================

CREATE TABLE IF NOT EXISTS ma_soft_sign_key (
  id            INT          NOT NULL AUTO_INCREMENT,
  from_soft_id  INT          NOT NULL COMMENT '对应 ma_soft.id',
  kid           VARCHAR(32)  NOT NULL COMMENT '密钥ID，票据中携带，便于轮换',
  public_key    VARCHAR(64)  NOT NULL COMMENT 'Ed25519 公钥 raw 32 字节的 Base64（可公开）',
  private_key   VARCHAR(128) NOT NULL COMMENT 'Ed25519 私钥 PKCS8 的 Base64（绝不下发）',
  feature_rules TEXT         NULL COMMENT '功能规则 JSON',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  add_time      INT          NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_soft_kid (from_soft_id, kid),
  KEY idx_soft_status (from_soft_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能票据签名密钥（每软件一对）';

-- 1) 按 skey 定位软件
SET @soft_id := (
  SELECT id FROM ma_soft
  WHERE skey = '9ace9f1a-f2e8-4161-866c-dbc19c1cac75'
  LIMIT 1
);

-- 2) 清理同 kid 的旧记录，保证幂等
DELETE FROM ma_soft_sign_key
WHERE from_soft_id = @soft_id AND kid = 'k6af6bec36435';

-- 3) 写入密钥
--    feature_rules 先给"全部放行 + 两个赞助功能门槛 100"，跑通后再按需调整。
INSERT INTO ma_soft_sign_key
  (from_soft_id, kid, public_key, private_key, feature_rules, status, add_time)
SELECT
  @soft_id,
  'k6af6bec36435',
  'WgnCyRjWs0q1NComTEk3/WLSAvau9Gt0DJAwDW58beo=',
  'MC4CAQAwBQYDK2VwBCIEIIzocB25/R/YEKEQmKwhkBiPt4xo6HYDNLna8h5/VESE',
  '{"supportText":"感谢你的支持，这是我们持续维护的动力","defaultVisible":true,"disabledModules":[],"features":{"boss_operations":{"minPoint":100,"allowedUsers":[],"deniedUsers":[]},"webshop":{"minPoint":100,"allowedUsers":[],"deniedUsers":[]},"points_system":{"minPoint":0,"allowedUsers":[],"deniedUsers":[]},"cloud_pack":{"minPoint":0,"allowedUsers":[],"deniedUsers":[]},"admin":{"minPoint":0,"allowedUsers":[],"deniedUsers":[]},"palschema":{"minPoint":0,"allowedUsers":[],"deniedUsers":[]},"pal-monitor":{"minPoint":0,"allowedUsers":[],"deniedUsers":[]}}}',
  1,
  UNIX_TIMESTAMP()
FROM DUAL
WHERE @soft_id IS NOT NULL;

-- 4) 回显结果，soft_id 为 NULL 说明 skey 没匹配到，需要手动确认
SELECT @soft_id AS matched_soft_id, kid, status, add_time
FROM ma_soft_sign_key
WHERE from_soft_id = @soft_id;
