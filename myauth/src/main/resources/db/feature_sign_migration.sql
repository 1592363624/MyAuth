-- ============================================================================
-- 功能票据（feature token）签名密钥表
--
-- 背景：客户端把赞助点数当作功能开关，点数来自登录/心跳响应，客户端不验响应签名，
-- 破解者可用 hosts + 本地假服务伪造 point=100。本表为「每软件一对 Ed25519 密钥」
-- 提供存储：私钥只存在于本表（MyAuth 服务端数据库），客户端只内嵌公钥。
--
-- 为什么单独建表，而不是给 soft 表加列：
--   web/SoftController.getSoftList / getSoft 会把整个 Soft 实体返回给后台浏览器，
--   密钥挂在 soft 表上会随列表接口明文带出。独立表天然不会被这些接口查到。
--
-- 执行：在 MyAuth 服务端连接的 MySQL 库（如 myauthtest）上执行一次。
-- ============================================================================

CREATE TABLE IF NOT EXISTS ma_soft_sign_key (
  id            INT          NOT NULL AUTO_INCREMENT,
  from_soft_id  INT          NOT NULL COMMENT '对应 ma_soft.id',
  kid           VARCHAR(32)  NOT NULL COMMENT '密钥ID，票据中携带，便于轮换',
  public_key    VARCHAR(64)  NOT NULL COMMENT 'Ed25519 公钥 raw 32 字节的 Base64（可公开）',
  private_key   VARCHAR(128) NOT NULL COMMENT 'Ed25519 私钥 PKCS8 的 Base64（绝不下发）',
  feature_rules TEXT         NULL COMMENT '功能规则 JSON，见下方说明',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  add_time      INT          NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_soft_kid (from_soft_id, kid),
  KEY idx_soft_status (from_soft_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能票据签名密钥（每软件一对）';

-- ---------------------------------------------------------------------------
-- feature_rules 填法
--
-- 直接填「远程功能」回复里那段 JSON 的明文（不用 base64），格式与客户端
-- internal/myauth/feature_config.go 的 FeatureConfig 一致，例如：
--
-- {
--   "supportText": "感谢你的支持",
--   "defaultVisible": true,
--   "disabledModules": [],
--   "features": {
--     "boss_operations": {"minPoint": 100, "allowedUsers": [], "deniedUsers": []},
--     "webshop":         {"minPoint": 100, "allowedUsers": [], "deniedUsers": []},
--     "points_system":   {"minPoint": 0,   "allowedUsers": [], "deniedUsers": []},
--     "cloud_pack":      {"minPoint": 0,   "allowedUsers": [], "deniedUsers": []}
--   }
-- }
--
-- 留空时服务端按 defaultVisible=true、所有功能 minPoint=0 处理（即全部放行），
-- 便于先部署链路、再逐步填规则。
--
-- 重要：规则从此以服务端的这份为准，客户端本地/远程配置不再参与权限判定，
-- 只用于界面文案展示。这样伪造配置 JSON 也无法解锁功能。
-- ---------------------------------------------------------------------------

-- 生成密钥对请调用后台接口：POST /myauth/web/genFeatureSignKey  {"id": 软件ID}
-- 接口会自动写入本表，只返回 kid 和 public_key（私钥不回显）。
-- 把返回的 public_key 填进客户端 hardcoded.go 的 FeatureSignPublicKey 后重新编译。
