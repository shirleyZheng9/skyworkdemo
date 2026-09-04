/*
 Navicat Premium Data Transfer

 Source Server         : 博特38数据库
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : 10.11.112.38:13306
 Source Schema         : market

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 21/05/2026 12:33:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pt_api
-- ----------------------------
DROP TABLE IF EXISTS `pt_api`;
CREATE TABLE `pt_api`  (
  `api_id` bigint NOT NULL,
  `api_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `api_desc` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `api_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `relative_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `req_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `header_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `path_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `query_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `body_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `response_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `example_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `catalog_item_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`api_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_api_auth
-- ----------------------------
DROP TABLE IF EXISTS `pt_api_auth`;
CREATE TABLE `pt_api_auth`  (
  `auth_id` bigint NOT NULL COMMENT '主键',
  `token_exp_time` datetime(0) NULL DEFAULT NULL COMMENT '令牌失效时间',
  `signature` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密钥',
  `auth_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鉴权类型',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`auth_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'API 鉴权' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_attr_spec
-- ----------------------------
DROP TABLE IF EXISTS `pt_attr_spec`;
CREATE TABLE `pt_attr_spec`  (
  `attr_id` bigint NOT NULL,
  `attr_nbr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `attr_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `par_attr_id` bigint NULL DEFAULT NULL,
  `default_value` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `data_type` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`attr_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_attr_value
-- ----------------------------
DROP TABLE IF EXISTS `pt_attr_value`;
CREATE TABLE `pt_attr_value`  (
  `attr_value_id` bigint NOT NULL,
  `attr_id` bigint NOT NULL,
  `attr_value_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `attr_value_desc` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `attr_value` varchar(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sortby` bigint NULL DEFAULT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`attr_value_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_auth_serv_log
-- ----------------------------
DROP TABLE IF EXISTS `pt_auth_serv_log`;
CREATE TABLE `pt_auth_serv_log`  (
  `log_id` bigint NOT NULL,
  `log_date` datetime(0) NULL DEFAULT NULL,
  `comments` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `event_src` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `event_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `event_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `party_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `party_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `party_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `party_id` bigint NULL DEFAULT NULL,
  `log_timestamp` bigint NULL DEFAULT NULL,
  `src_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `server_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `online_time` decimal(10, 0) NULL DEFAULT NULL,
  `session_id` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `browser_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `os_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_cache_cfg
-- ----------------------------
DROP TABLE IF EXISTS `pt_cache_cfg`;
CREATE TABLE `pt_cache_cfg`  (
  `cache_config_id` bigint NOT NULL,
  `group_code` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `view_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cache_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cache_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `refrensh_all_flag` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cache_key_prefix` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`cache_config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_catalog
-- ----------------------------
DROP TABLE IF EXISTS `pt_catalog`;
CREATE TABLE `pt_catalog`  (
  `catalog_id` bigint NOT NULL,
  `par_catalog_id` bigint NULL DEFAULT NULL,
  `catalog_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `catalog_type` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `catalog_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sortby` bigint NULL DEFAULT NULL,
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`catalog_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_dc_param
-- ----------------------------
DROP TABLE IF EXISTS `pt_dc_param`;
CREATE TABLE `pt_dc_param`  (
  `param_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `param_val` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `param_desc` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `param_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_time` datetime(0) NOT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`param_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_gateway
-- ----------------------------
DROP TABLE IF EXISTS `pt_gateway`;
CREATE TABLE `pt_gateway`  (
  `gateway_id` bigint NOT NULL COMMENT '网关ID',
  `gateway_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '网关名称',
  `gateway_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '网关类型',
  `url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '网关地址',
  `user_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `higress_runtime_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Higress 运行网关',
  PRIMARY KEY (`gateway_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '网关' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_higress_user
-- ----------------------------
DROP TABLE IF EXISTS `pt_higress_user`;
CREATE TABLE `pt_higress_user`  (
  `higress_user_id` bigint NOT NULL COMMENT '主键ID',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '网关ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `auth_id` bigint NULL DEFAULT NULL COMMENT '授权ID',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`higress_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'higress用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_oper_detail_log
-- ----------------------------
DROP TABLE IF EXISTS `pt_oper_detail_log`;
CREATE TABLE `pt_oper_detail_log`  (
  `detail_id` bigint NOT NULL,
  `log_id` bigint NOT NULL,
  `oper_type` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_table_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_table_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_field_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_field_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `obj_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `old_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `new_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `part_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`detail_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `pt_oper_log`;
CREATE TABLE `pt_oper_log`  (
  `log_id` bigint NOT NULL,
  `oper_class` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `obj_id` bigint NULL DEFAULT NULL,
  `obj_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `oper_type` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `oper_staff` bigint NULL DEFAULT NULL,
  `oper_date` datetime(0) NULL DEFAULT NULL,
  `batch_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_plugin
-- ----------------------------
DROP TABLE IF EXISTS `pt_plugin`;
CREATE TABLE `pt_plugin`  (
  `plugin_id` bigint NOT NULL COMMENT '插件ID',
  `plugin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件名称',
  `plugin_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件编码',
  `plugin_icon` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '插件图标',
  `gateway_id` bigint NULL DEFAULT NULL COMMENT '网关ID',
  `catalog_item_id` bigint NULL DEFAULT NULL COMMENT '插件分类',
  `plugin_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件类型',
  `plugin_status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件状态',
  `plugin_sub_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '插件子类型',
  `relative_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相对路径地址',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `auth_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '授权配置JSON',
  `plugin_intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '插件功能描述',
  `config_on_use` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否使用时配置',
  `auto_audit` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否自动审批',
  `higress_resource_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'higress 资源名称',
  `discovery_tool_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自动发现工具地址',
  `discovery_tool_type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自动发现工具方式',
  `auth_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '授权类型',
  `params_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '参数配置',
  PRIMARY KEY (`plugin_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '插件' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_plugin_auth
-- ----------------------------
DROP TABLE IF EXISTS `pt_plugin_auth`;
CREATE TABLE `pt_plugin_auth`  (
  `auth_id` bigint NOT NULL COMMENT '授权ID',
  `plugin_id` bigint NULL DEFAULT NULL COMMENT '插件ID',
  `auth_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '授权类型',
  `portal_id` bigint NULL DEFAULT NULL COMMENT '门户ID',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`auth_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '插件授权' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_plugin_auth_json
-- ----------------------------
DROP TABLE IF EXISTS `pt_plugin_auth_json`;
CREATE TABLE `pt_plugin_auth_json`  (
  `auth_id` bigint NOT NULL,
  `plugin_id` bigint NULL DEFAULT NULL,
  `auth_json` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`auth_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_plugin_subscribe
-- ----------------------------
DROP TABLE IF EXISTS `pt_plugin_subscribe`;
CREATE TABLE `pt_plugin_subscribe`  (
  `subscribe_id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `plugin_id` bigint NULL DEFAULT NULL COMMENT '插件ID',
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`subscribe_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '插件订阅' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_plugin_tool
-- ----------------------------
DROP TABLE IF EXISTS `pt_plugin_tool`;
CREATE TABLE `pt_plugin_tool`  (
  `tool_id` bigint NOT NULL COMMENT '工具ID',
  `plugin_id` bigint NOT NULL COMMENT '插件ID',
  `tool_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具名称',
  `tool_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工具编码',
  `relative_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相对路径',
  `req_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `header_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求头参数',
  `path_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '路径参数',
  `query_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `body_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求体参数',
  `response_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应参数',
  `is_sse` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否SSE',
  `form_data_enabled` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表单数据启用',
  `body_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求体类型',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`tool_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '插件工具' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_portal
-- ----------------------------
DROP TABLE IF EXISTS `pt_portal`;
CREATE TABLE `pt_portal`  (
  `portal_id` bigint NOT NULL COMMENT '门户ID',
  `portal_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门户名称',
  `portal_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '门户编码',
  `portal_icon` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '门户图标',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `auth_id` bigint NULL DEFAULT NULL COMMENT '授权ID',
  `auto_auth` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自动授权',
  `portal_icon_config` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '门户图标配置',
  PRIMARY KEY (`portal_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_portal_dir
-- ----------------------------
DROP TABLE IF EXISTS `pt_portal_dir`;
CREATE TABLE `pt_portal_dir`  (
  `dir_id` bigint NOT NULL COMMENT '目录ID',
  `dir_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目录名称',
  `dir_type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目录类型',
  `dir_icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目录图标',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '目录父ID',
  `sortby` bigint NULL DEFAULT NULL COMMENT '排序',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '关联菜单ID（当目录同时也是菜单时）',
  `menu_type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单类型',
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dir_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门户目录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_portal_dir_menu
-- ----------------------------
DROP TABLE IF EXISTS `pt_portal_dir_menu`;
CREATE TABLE `pt_portal_dir_menu`  (
  `rel_id` bigint NOT NULL COMMENT '主键ID',
  `dir_id` bigint NULL DEFAULT NULL COMMENT '目录ID',
  `menu_id` bigint NULL DEFAULT NULL COMMENT '目录名称',
  `menu_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `menu_type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单类型',
  `menu_icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `is_hidden` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否隐藏',
  `sortby` bigint NULL DEFAULT NULL COMMENT '排序',
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rel_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门户目录菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_portal_user
-- ----------------------------
DROP TABLE IF EXISTS `pt_portal_user`;
CREATE TABLE `pt_portal_user`  (
  `portal_user_id` bigint NOT NULL COMMENT '主键ID',
  `portal_id` bigint NULL DEFAULT NULL COMMENT '门户ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `auth_id` bigint NULL DEFAULT NULL COMMENT '授权ID',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `auth_status` int NULL DEFAULT NULL COMMENT '授权状态',
  `portal_user_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开发者类型（admin:管理员,developer:普通开发者）',
  PRIMARY KEY (`portal_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门户开发者' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_priv
-- ----------------------------
DROP TABLE IF EXISTS `pt_priv`;
CREATE TABLE `pt_priv`  (
  `priv_id` bigint NOT NULL COMMENT '权限ID',
  `priv_type` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型',
  `priv_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `priv_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限名称',
  `priv_url` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限菜单地址',
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`priv_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_role_priv
-- ----------------------------
DROP TABLE IF EXISTS `pt_role_priv`;
CREATE TABLE `pt_role_priv`  (
  `rel_id` bigint NOT NULL COMMENT '主键ID',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `priv_id` bigint NOT NULL COMMENT '权限ID',
  `status_cd` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（00A:有效, 00X:无效）',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `updator_id` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rel_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_user
-- ----------------------------
DROP TABLE IF EXISTS `pt_user`;
CREATE TABLE `pt_user`  (
  `user_id` bigint NOT NULL,
  `user_icon` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户类型',
  `phone_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_locked` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `login_fail_count` int NULL DEFAULT NULL,
  `user_state` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_exp_date` datetime(0) NULL DEFAULT NULL,
  `pwd_updated_time` datetime(0) NOT NULL,
  `creator_id` bigint NULL DEFAULT NULL,
  `created_time` datetime(0) NULL DEFAULT NULL,
  `status_cd` varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `updator_id` bigint NULL DEFAULT NULL,
  `updated_time` datetime(0) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
