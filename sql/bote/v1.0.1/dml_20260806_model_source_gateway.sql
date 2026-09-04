-- 大模型来源字典增加「天工AI网关」
INSERT INTO bt_skill_attr_value (
  attr_value_id, attr_id, attr_value_name, attr_value_desc, attr_value,
  tenant_id, sortby, creator_id, created_time, status_cd,
  updator_id, updated_time, remark, attr_value_code, is_required, comp_type
)
SELECT
  202409050203, 2024090502, '天工', NULL, 'gateway',
  -1, 1, NULL, NOW(), '00A',
  NULL, NULL, NULL, NULL, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM bt_skill_attr_value WHERE attr_value_id = 202409050203
);

update bt_skill_attr_value set attr_value_name = '用户' where attr_value_id = '202409050202' and attr_id = '2024090502' and attr_value_name = '项目' ;