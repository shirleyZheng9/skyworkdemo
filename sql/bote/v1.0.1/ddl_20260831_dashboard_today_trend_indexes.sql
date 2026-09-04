-- 数据概览看板：今日动态与每日消息趋势索引。
-- 执行前请在目标库检查同名或等价索引，避免重复创建。

-- 今日大模型调用次数按租户和调用开始时间统计，当天实时数据只查当前日志表。
CREATE INDEX `idx_dashboard_model_usage_today`
  ON `bt_model_usage_log` (`tenant_id`, `start_time`, `log_id`);

-- 今日知识构建文档按租户、有效状态、构建状态和完成时间过滤。
CREATE INDEX `idx_dashboard_document_built_today`
  ON `bt_document` (`tenant_id`, `status_cd`, `doc_status`, `process_completed_at`, `knowledge_id`);

-- 每日消息趋势先按 session_id 关联，再按消息创建时间和用户角色过滤。
CREATE INDEX `idx_dashboard_session_tenant`
  ON `bt_bot_session` (`tenant_id`, `session_id`);

CREATE INDEX `idx_dashboard_session_his_tenant`
  ON `bt_bot_session_his` (`tenant_id`, `session_id`);

CREATE INDEX `idx_dashboard_session_msg_trend`
  ON `bt_bot_session_msg` (`session_id`, `created_time`, `role`, `status_cd`);

CREATE INDEX `idx_dashboard_session_msg_his_trend`
  ON `bt_bot_session_msg_his` (`session_id`, `created_time`, `role`, `status_cd`);
