-- ============================================================
-- 数据维护模块菜单重构迁移脚本
-- 适用场景：已部署环境升级，将平铺菜单重构为分组结构
-- 执行前请备份 ma_menu 表
-- ============================================================

-- 1. 添加 description 字段
ALTER TABLE `ma_menu` ADD COLUMN `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单描述，用于前端提示' AFTER `icon`;

-- 2. 创建二级分组目录
INSERT INTO `ma_menu` VALUES ('c1a2b3d4-e5f6-7890-abcd-ef1234560001', 'd6321208-4980-46e3-b3d4-ec057009472c', 2, 1, 1, NULL, '卡密交易', 'credit-card', '卡密生成、分发与订单管理');
INSERT INTO `ma_menu` VALUES ('c1a2b3d4-e5f6-7890-abcd-ef1234560002', 'd6321208-4980-46e3-b3d4-ec057009472c', 2, 2, 1, NULL, '自动化', 'thunderbolt', '自动回复、脚本与事件规则');
INSERT INTO `ma_menu` VALUES ('c1a2b3d4-e5f6-7890-abcd-ef1234560003', 'd6321208-4980-46e3-b3d4-ec057009472c', 2, 3, 1, NULL, '数据与存储', 'hdd', '业务数据存储与访问控制');

-- 3. 将原有菜单项移入对应分组（修改 parent_id 和 level）
-- 卡密交易分组
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560001', `level` = 3, `sort` = 1, `icon` = 'key', `description` = '生成、查询与核销授权卡密' WHERE `id` = 'e70b3176-a2fa-4fc5-90f3-5abd9e2d13b8';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560001', `level` = 3, `sort` = 2, `icon` = 'team', `description` = '代理商卡密生成与分发管理' WHERE `id` = 'd499258d-ea25-47e1-b580-ed98830df37f';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560001', `level` = 3, `sort` = 3, `title` = '支付订单', `icon` = 'pay-circle', `description` = '查看与管理在线支付订单' WHERE `id` = '8dcc2036-3b90-4033-abdb-2d7a5636998e';

-- 自动化分组
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560002', `level` = 3, `sort` = 1, `icon` = 'message', `description` = '配置关键词自动回复规则' WHERE `id` = 'b89eb2d8-40e4-4698-ab5a-ddaed7846ee1';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560002', `level` = 3, `sort` = 2, `title` = '脚本函数', `icon` = 'code', `description` = '管理自定义JS脚本函数' WHERE `id` = '1d3838df-bc61-42e1-a149-dcc2705e2894';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560002', `level` = 3, `sort` = 3, `icon` = 'schedule', `description` = '配置点数/时长变动事件规则' WHERE `id` = '5b35fe6a-da5f-46d5-b1ed-076308a38a13';

-- 数据与存储分组
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560003', `level` = 3, `sort` = 1, `icon` = 'table', `description` = '管理客户端上报的业务数据' WHERE `id` = 'dbe9effe-0dc8-4378-a64c-5bf904ac6fae';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560003', `level` = 3, `sort` = 2, `title` = '扩展存储', `icon` = 'cloud', `description` = '管理客户端额外存储数据' WHERE `id` = '9b69d1b6-f0a6-48f9-9f25-cc121fda88aa';
UPDATE `ma_menu` SET `parent_id` = 'c1a2b3d4-e5f6-7890-abcd-ef1234560003', `level` = 3, `sort` = 3, `icon` = 'stop', `description` = '管理用户/设备封禁规则' WHERE `id` = 'a59b674e-8d45-46c2-be6e-ff44d2ee7b86';

-- 4. 为其他模块菜单补充描述和图标
UPDATE `ma_menu` SET `description` = '系统概览与快捷入口' WHERE `id` = '59664367-11eb-489e-867d-af6c66129d03';
UPDATE `ma_menu` SET `description` = '可视化数据统计与分析' WHERE `id` = 'e4fa47b7-aa79-4708-9ce1-c7b686db6a50';
UPDATE `ma_menu` SET `description` = '管理软件产品配置' WHERE `id` = 'e50cadbc-8b2b-4c8f-a262-9da95d3371bb';
UPDATE `ma_menu` SET `description` = '管理软件版本发布' WHERE `id` = '94beb8ae-a606-4dea-bfa8-57bc734818bf';
UPDATE `ma_menu` SET `description` = '管理终端用户账号' WHERE `id` = 'bcb68a35-eb0f-4696-acd8-3c76897e0f0f';
UPDATE `ma_menu` SET `description` = '查看当前账号的授权列表' WHERE `id` = '922666ba-1ed1-4b6b-9043-30aeead1eebe';
UPDATE `ma_menu` SET `description` = '查看当前账号的卡密' WHERE `id` = '23a8778f-9424-449a-a68e-54b12e4c30f3';
UPDATE `ma_menu` SET `description` = '查看当前账号余额明细' WHERE `id` = 'cd256048-3f8e-4fb7-ada3-55d538ce7611';
UPDATE `ma_menu` SET `description` = '管理系统管理员账号' WHERE `id` = 'd7720078-3b65-4f83-b404-c117e5c46b68';
UPDATE `ma_menu` SET `description` = '查看管理员操作记录' WHERE `id` = '9d953050-f354-4e9f-9f15-b91ea6a666f0';
UPDATE `ma_menu` SET `description` = '查看用户操作日志' WHERE `id` = '71eedb8d-ae9e-4958-a351-c8dfb4711f21';
UPDATE `ma_menu` SET `description` = '查看系统操作日志' WHERE `id` = 'f9a80e4a-d473-40f9-847b-74916815c7b7';
UPDATE `ma_menu` SET `description` = '管理角色权限分配' WHERE `id` = 'd58b4547-e20f-4eb9-b7d5-b3195ed5cc18';
UPDATE `ma_menu` SET `description` = '配置授权验证策略' WHERE `id` = '014bf6d7-cff3-4b61-aa3b-f59cc0dd3235';
UPDATE `ma_menu` SET `description` = '管理系统菜单结构' WHERE `id` = '546ea73a-1d46-41f5-bfa6-e646b3e741ae';
UPDATE `ma_menu` SET `description` = '系统参数配置' WHERE `id` = 'd27462d2-b316-4983-9066-f283fcf17e46';
UPDATE `ma_menu` SET `title` = '存储类型管理', `description` = '管理扩展存储的分类类型' WHERE `id` = 'ddff63b5-38db-4889-882d-fd9d9c4e583f';
UPDATE `ma_menu` SET `description` = '配置在线支付通道' WHERE `id` = '1599a39b-38ce-4fca-9819-5fead2e63546';
UPDATE `ma_menu` SET `description` = '配置邮件通知规则' WHERE `id` = '7ddb181b-3c6a-4a63-b45c-172ee8d7917b';

-- 5. 更新角色权限中的菜单ID（将新分组目录加入已有角色）
-- 注意：请根据实际角色表结构执行，以下为示例
-- 如果角色的 meun_ids 字段是 JSON 数组格式，需要手动将新分组ID加入对应角色
-- 新分组ID列表：
--   c1a2b3d4-e5f6-7890-abcd-ef1234560001 (卡密交易)
--   c1a2b3d4-e5f6-7890-abcd-ef1234560002 (自动化)
--   c1a2b3d4-e5f6-7890-abcd-ef1234560003 (数据与存储)
