-- 检查点在数据库变更执行时确定，因此上线前日志不会被误计入“自功能上线日起累计”。
-- NOT EXISTS 使脚本具备重复执行保护，不会覆盖已经推进的生产游标。
INSERT INTO bt_dashboard_aggregation_checkpoint (
  task_code, statistics_start_time, last_created_time, last_log_id, created_time, updated_time
)
SELECT 'dashboard_model_usage_total_v1', NOW(), NOW(), 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM bt_dashboard_aggregation_checkpoint
  WHERE task_code = 'dashboard_model_usage_total_v1'
);


