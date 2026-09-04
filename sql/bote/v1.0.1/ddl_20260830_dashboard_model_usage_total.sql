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

-- 汇总只读取轻量字段，通过入库时间、日志ID复合索引快速定位增量区间。
CREATE INDEX `idx_model_usage_created_log` ON `bt_model_usage_log` (`created_time`, `log_id`);
CREATE INDEX `idx_model_usage_his_created_log` ON `bt_model_usage_log_his` (`created_time`, `log_id`);
