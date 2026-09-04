-- 数据概览看板：模型使用量汇总表增加产品系列快照。
-- 说明：
-- 1. 该脚本用于已经执行过旧版 bt_dashboard_model_usage_total 建表脚本的环境；
-- 2. 新部署环境直接使用 ddl_20260830_dashboard_model_usage_total.sql 即可；
-- 3. 执行前请检查是否已存在 product_type 字段或等价索引，避免重复执行报错。

ALTER TABLE `bt_dashboard_model_usage_total`
  ADD COLUMN `product_type` varchar(100) DEFAULT NULL COMMENT '模型产品系列快照' AFTER `model_name`;

DROP INDEX `idx_dashboard_model_usage_tenant_count` ON `bt_dashboard_model_usage_total`;

CREATE INDEX `idx_dashboard_model_usage_tenant_product_count`
  ON `bt_dashboard_model_usage_total` (`tenant_id`, `product_type`, `invoke_count`, `model_id`);

UPDATE `bt_dashboard_model_usage_total` s
LEFT JOIN `bt_library_large_model` own_model
  ON own_model.model_id = s.model_id
  AND s.tenant_id <> -1
  AND own_model.tenant_id = s.tenant_id
  AND own_model.status_cd = '00A'
LEFT JOIN `bt_library_large_model` platform_model
  ON platform_model.model_id = s.model_id
  AND platform_model.tenant_id = -1
  AND platform_model.status_cd = '00A'
SET s.product_type = COALESCE(NULLIF(own_model.product_type, ''), NULLIF(platform_model.product_type, ''))
WHERE s.product_type IS NULL;
