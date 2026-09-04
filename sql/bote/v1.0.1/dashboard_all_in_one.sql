-- 首页数据看板数据库脚本合并版
-- 适用场景：新环境直接执行。
-- 说明：bt_dashboard_model_usage_total 新增表已直接包含 product_type 字段，不需要再执行 product_type ALTER 补丁脚本。
-- 说明：今日动态已确认不统计插件/MCP调用次数，当前不新增 bt_flow_run_log 相关看板索引。

-- ============================================================
-- 1. 模型累计使用量汇总表
-- ============================================================

-- 数据概览看板：模型累计使用量汇总。
-- 业务口径为“自功能上线日起累计”，统计起点由检查点初始化时间确定，不回填旧日志。
-- 汇总粒度固定为“租户 + 单个模型”，不是每次调用一行，因此表大小由使用过的模型数量决定。
CREATE TABLE IF NOT EXISTS `bt_dashboard_model_usage_total` (
  `tenant_id` bigint(20) NOT NULL COMMENT '调用所属租户',
  `model_id` bigint(20) NOT NULL COMMENT '模型ID',
  `model_name` varchar(128) DEFAULT NULL COMMENT '最近一次调用时的模型名称快照',
  `product_type` varchar(100) DEFAULT NULL COMMENT '模型产品系列快照',
  `invoke_count` bigint(20) NOT NULL DEFAULT 0 COMMENT '累计调用次数',
  `model_status` varchar(12) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'ACTIVE/DISABLED/WAITING/DELETED/UNKNOWN',
  `model_source` varchar(12) NOT NULL DEFAULT 'UNKNOWN' COMMENT 'PLATFORM/CUSTOM/UNKNOWN',
  `last_status_check_time` datetime DEFAULT NULL COMMENT '最近模型状态校准时间',
  `created_time` datetime NOT NULL,
  `updated_time` datetime NOT NULL,
  PRIMARY KEY (`tenant_id`, `model_id`),
  KEY `idx_dashboard_model_usage_status_check` (`last_status_check_time`, `tenant_id`, `model_id`),
  KEY `idx_dashboard_model_usage_tenant_product_count` (`tenant_id`, `product_type`, `invoke_count`, `model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板模型累计使用量';

-- ============================================================
-- 2. 看板汇总任务检查点表
-- ============================================================

-- 内部检查点表用于增量游标和多节点互斥，不需要额外提供前端聚合接口。
CREATE TABLE IF NOT EXISTS `bt_dashboard_aggregation_checkpoint` (
  `task_code` varchar(64) NOT NULL,
  `statistics_start_time` datetime NOT NULL COMMENT '对外声明的统计起始时间',
  `last_created_time` datetime NOT NULL COMMENT '最后处理的日志入库时间',
  `last_log_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '同一入库时间内的稳定游标',
  `created_time` datetime NOT NULL,
  `updated_time` datetime NOT NULL,
  PRIMARY KEY (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板内部汇总任务检查点';

-- ============================================================
-- 3. 模型累计使用量增量汇总索引
-- ============================================================

-- 汇总只读取轻量字段，通过入库时间、日志ID复合索引快速定位增量区间。
CREATE INDEX `idx_model_usage_created_log`
  ON `bt_model_usage_log` (`created_time`, `log_id`);

-- 历史模型日志也参与累计汇总，因此历史表同步补充增量扫描索引。
CREATE INDEX `idx_model_usage_his_created_log`
  ON `bt_model_usage_log_his` (`created_time`, `log_id`);

-- ============================================================
-- 4. 顶部资源概览与分布类接口索引
-- ============================================================

-- 智能体数量、今日新增智能体、智能体模式分布按租户、状态、模式和创建时间过滤。
CREATE INDEX `idx_dashboard_scene_overview`
  ON `bt_bot_scene` (`tenant_id`, `status_cd`, `scene_type`, `created_time`);

-- 知识库数量、今日新增知识库按租户、状态和创建时间过滤。
CREATE INDEX `idx_dashboard_kb_overview`
  ON `bt_knowledge_base` (`tenant_id`, `status_cd`, `created_time`);

-- 接入大模型数量按租户、状态、来源、公开标识和创建时间过滤。
CREATE INDEX `idx_dashboard_model_overview`
  ON `bt_library_large_model` (`tenant_id`, `status_cd`, `data_from`, `is_public`, `created_time`);

-- 插件数量按租户、状态和创建时间过滤。
CREATE INDEX `idx_dashboard_plugin_overview`
  ON `bt_plugin` (`tenant_id`, `status_cd`, `created_time`);

-- MCP数量按租户、状态、来源和创建时间过滤。
CREATE INDEX `idx_dashboard_mcp_overview`
  ON `bt_mcp_server` (`tenant_id`, `status_cd`, `data_from`, `created_time`);

-- 知识库文件类型分布按文档、知识库和文件信息关联统计。
CREATE INDEX `idx_dashboard_document_file_distribution`
  ON `bt_document` (`tenant_id`, `status_cd`, `knowledge_id`, `file_info_id`);

-- ============================================================
-- 5. 今日动态与每日消息趋势索引
-- ============================================================

-- 今日大模型调用次数按租户和调用开始时间统计，当天实时数据只查当前日志表。
CREATE INDEX `idx_dashboard_model_usage_today`
  ON `bt_model_usage_log` (`tenant_id`, `start_time`, `log_id`);

-- 今日知识构建文档按租户、有效状态、构建状态和完成时间过滤。
CREATE INDEX `idx_dashboard_document_built_today`
  ON `bt_document` (`tenant_id`, `status_cd`, `doc_status`, `process_completed_at`, `knowledge_id`);

-- 每日消息趋势先按租户定位当前会话，再通过 session_id 关联当前消息表。
CREATE INDEX `idx_dashboard_session_tenant`
  ON `bt_bot_session` (`tenant_id`, `session_id`);

-- 每日消息趋势先按租户定位历史会话，再通过 session_id 关联历史消息表。
CREATE INDEX `idx_dashboard_session_his_tenant`
  ON `bt_bot_session_his` (`tenant_id`, `session_id`);

-- 当前会话消息趋势按 session_id、创建时间、角色和状态过滤。
CREATE INDEX `idx_dashboard_session_msg_trend`
  ON `bt_bot_session_msg` (`session_id`, `created_time`, `role`, `status_cd`);

-- 历史会话消息趋势按 session_id、创建时间、角色和状态过滤。
CREATE INDEX `idx_dashboard_session_msg_his_trend`
  ON `bt_bot_session_msg_his` (`session_id`, `created_time`, `role`, `status_cd`);

-- ============================================================
-- 6. 模型累计使用量汇总任务初始化
-- ============================================================

-- 检查点在数据库变更执行时确定，因此上线前日志不会被误计入“自功能上线日起累计”。
-- NOT EXISTS 使脚本具备重复执行保护，不会覆盖已经推进的生产游标。
INSERT INTO bt_dashboard_aggregation_checkpoint (
  task_code, statistics_start_time, last_created_time, last_log_id, created_time, updated_time
)
SELECT 'dashboard_model_usage_total_v1', NOW(), NOW(), 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM bt_dashboard_aggregation_checkpoint
  WHERE task_code = 'dashboard_model_usage_total_v1'
);


