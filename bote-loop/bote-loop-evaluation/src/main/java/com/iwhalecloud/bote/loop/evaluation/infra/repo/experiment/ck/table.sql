CREATE TABLE `expt_turn_result_filter`
(
  `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `space_id`            varchar(255) NOT NULL COMMENT '空间ID',
  `expt_id`             varchar(255) NOT NULL COMMENT '实验ID',
  `item_id`             varchar(255) NOT NULL COMMENT '数据项ID',
  `item_idx`            int          NOT NULL COMMENT '数据项序号',
  `turn_id`             varchar(255) NOT NULL COMMENT '轮次ID',
  `status`              int          NOT NULL COMMENT '状态',
  `eval_set_version_id` varchar(255) NOT NULL COMMENT '评估集版本ID',
  `created_date`        varchar(255) NOT NULL COMMENT '创建日期',
  `eval_target_data`    json                  DEFAULT NULL COMMENT '评估目标数据',
  `evaluator_score`     json                  DEFAULT NULL COMMENT '评估器得分',
  `evaluator_score_corrected` double DEFAULT NULL COMMENT '修正后的评估器得分',
  `annotation_float`    json                  DEFAULT NULL COMMENT '浮点标注数据',
  `annotation_bool`     json                  DEFAULT NULL COMMENT '布尔标注数据',
  `annotation_string`   json                  DEFAULT NULL COMMENT '字符串标注数据',
  `created_at`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY                   `idx_space_expt` (`space_id`, `expt_id`),
  KEY                   `idx_item_id` (`item_id`),
  KEY                   `idx_status` (`status`),
  KEY                   `idx_created_date` (`created_date`),
  KEY                   `idx_eval_set_version` (`eval_set_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实验轮次结果过滤器表';

