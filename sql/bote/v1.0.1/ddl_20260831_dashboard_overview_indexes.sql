-- 数据概览看板：顶部资源概览索引。
-- 说明：
-- 1. 顶部概览只做资源主表 COUNT 和今日新增条件聚合，不读取 longtext 字段；
-- 2. 以下索引用于让 tenant/status/type/time 过滤尽量走覆盖索引，稳定满足首屏 2 秒要求；
-- 3. 执行前请在目标库检查同名或等价索引，避免重复创建。

CREATE INDEX `idx_dashboard_scene_overview`
  ON `bt_bot_scene` (`tenant_id`, `status_cd`, `scene_type`, `created_time`);

CREATE INDEX `idx_dashboard_kb_overview`
  ON `bt_knowledge_base` (`tenant_id`, `status_cd`, `created_time`);

CREATE INDEX `idx_dashboard_model_overview`
  ON `bt_library_large_model` (`tenant_id`, `status_cd`, `data_from`, `is_public`, `created_time`);

CREATE INDEX `idx_dashboard_plugin_overview`
  ON `bt_plugin` (`tenant_id`, `status_cd`, `created_time`);

CREATE INDEX `idx_dashboard_mcp_overview`
  ON `bt_mcp_server` (`tenant_id`, `status_cd`, `data_from`, `created_time`);

CREATE INDEX `idx_dashboard_document_file_distribution`
  ON `bt_document` (`tenant_id`, `status_cd`, `knowledge_id`, `file_info_id`);
