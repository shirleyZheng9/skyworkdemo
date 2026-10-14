-- 会话表新增发布记录ID列：发布链接打开的会话按发布记录隔离历史记录
-- 日期: 2026-09-30
-- 说明: 新增稿，不覆盖既有初始化脚本；请在目标库执行前备份。

-- 1) 会话表新增发布记录 ID
ALTER TABLE `bt_bot_session`
  ADD COLUMN `publish_id` bigint(20) DEFAULT NULL COMMENT '发布记录 ID（发布链接会话隔离）' AFTER `is_test`;

-- 2) 会话历史表同步新增（会话归档为 INSERT INTO ... SELECT 列对齐，必须同步加列）
ALTER TABLE `bt_bot_session_his`
  ADD COLUMN `publish_id` bigint(20) DEFAULT NULL COMMENT '发布记录 ID（发布链接会话隔离）' AFTER `is_test`;
