-- Skill 目录/文件模型 + bt_agent_skill 扩展列（同步 bote-service Skill/Ontology 能力）
-- 日期: 2026-07-17
-- 说明: 新增稿，不覆盖既有初始化脚本；请在目标库执行前备份。

-- 1) bt_agent_skill 扩展列
ALTER TABLE `bt_agent_skill`
  ADD COLUMN `skill_file_name` varchar(255) DEFAULT NULL COMMENT '技能文件名' AFTER `skill_name`,
  ADD COLUMN `skill_type` varchar(32) DEFAULT NULL COMMENT '技能类型: online/local' AFTER `skill_file_name`,
  ADD COLUMN `skill_template_type` varchar(64) DEFAULT NULL COMMENT '技能模板类型: default/ontologyScene' AFTER `skill_type`,
  ADD COLUMN `skill_ext_json` text COMMENT '技能扩展信息' AFTER `skill_template_type`;

-- 2) Agent Skill 目录
CREATE TABLE IF NOT EXISTS `bt_agent_skill_dir` (
  `dir_id` bigint(20) NOT NULL COMMENT '目录主键',
  `parent_dir_id` bigint(20) DEFAULT NULL COMMENT '父目录 ID，一级目录固定 -1',
  `skill_id` bigint(20) NOT NULL COMMENT '技能 ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户 ID',
  `dir_name` varchar(255) DEFAULT NULL COMMENT '目录名称',
  `status_cd` varchar(4) NOT NULL,
  `creator_id` bigint(20) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updator_id` bigint(20) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`dir_id`),
  KEY `idx_skill_tenant` (`skill_id`, `tenant_id`),
  KEY `idx_parent_dir` (`parent_dir_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent Skill 目录';

-- 3) Agent Skill 文件
CREATE TABLE IF NOT EXISTS `bt_agent_skill_file` (
  `skill_file_id` bigint(20) NOT NULL COMMENT '文件主键',
  `dir_id` bigint(20) NOT NULL COMMENT '所属目录 ID',
  `skill_id` bigint(20) NOT NULL COMMENT '技能 ID',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户 ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `file_type` varchar(64) DEFAULT NULL COMMENT '文件类型（后缀小写，不含点）',
  `file_content` longtext COMMENT '文本文件内容',
  `file_info_id` bigint(20) DEFAULT NULL COMMENT '二进制文件对应 bt_file_info 主键',
  `status_cd` varchar(4) NOT NULL,
  `creator_id` bigint(20) DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `updator_id` bigint(20) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`skill_file_id`),
  KEY `idx_dir` (`dir_id`),
  KEY `idx_skill_tenant` (`skill_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent Skill 文件';
