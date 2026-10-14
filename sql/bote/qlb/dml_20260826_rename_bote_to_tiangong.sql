-- 品牌更名：展示文案「博特」→「天工」
-- 说明：
--   1. 仅替换中文展示名/描述/配置文案，不改英文标识（BoteCopilot、/bote/、com.iwhalecloud.bote 等）
--   2. 适用于已按旧版 bote0522.sql 初始化、库中仍残留「博特」展示文案的环境
--   3. 新环境若直接使用已改名后的 bote0522.sql，无需再执行本脚本
--   4. 必须先 SET NAMES utf8mb4，否则中文 REPLACE/WHERE 可能匹配不到、改不动

SET NAMES utf8mb4;

-- 租户 / 组织（平台与应用入口名）
UPDATE bt_tenant
SET tenant_name = REPLACE(tenant_name, '博特', '天工'),
    tenant_use = REPLACE(IFNULL(tenant_use, ''), '博特', '天工'),
    remark = REPLACE(IFNULL(remark, ''), '博特', '天工'),
    prologue = REPLACE(IFNULL(prologue, ''), '博特', '天工'),
    bot_setting_info = REPLACE(IFNULL(bot_setting_info, ''), '博特', '天工'),
    question_setting_info = REPLACE(IFNULL(question_setting_info, ''), '博特', '天工')
WHERE IFNULL(tenant_name, '') LIKE '%博特%'
   OR IFNULL(tenant_use, '') LIKE '%博特%'
   OR IFNULL(remark, '') LIKE '%博特%'
   OR IFNULL(prologue, '') LIKE '%博特%'
   OR IFNULL(bot_setting_info, '') LIKE '%博特%'
   OR IFNULL(question_setting_info, '') LIKE '%博特%';

UPDATE bt_organization
SET org_name = REPLACE(org_name, '博特', '天工')
WHERE IFNULL(org_name, '') LIKE '%博特%';

-- 智能应用 / 平台应用
UPDATE bt_bot
SET bot_name = REPLACE(bot_name, '博特', '天工'),
    bot_use = REPLACE(IFNULL(bot_use, ''), '博特', '天工'),
    prologue = REPLACE(IFNULL(prologue, ''), '博特', '天工')
WHERE IFNULL(bot_name, '') LIKE '%博特%'
   OR IFNULL(bot_use, '') LIKE '%博特%'
   OR IFNULL(prologue, '') LIKE '%博特%';

UPDATE bt_plat_bot_info
SET bot_name = REPLACE(bot_name, '博特', '天工'),
    bot_desc = REPLACE(IFNULL(bot_desc, ''), '博特', '天工')
WHERE IFNULL(bot_name, '') LIKE '%博特%'
   OR IFNULL(bot_desc, '') LIKE '%博特%';

-- 知识库 / 场景技能
UPDATE bt_knowledge_base
SET knowledge_name = REPLACE(knowledge_name, '博特', '天工'),
    knowledge_desc = REPLACE(IFNULL(knowledge_desc, ''), '博特', '天工')
WHERE IFNULL(knowledge_name, '') LIKE '%博特%'
   OR IFNULL(knowledge_desc, '') LIKE '%博特%';

UPDATE bt_bot_scene_skill
SET skill_name = REPLACE(skill_name, '博特', '天工'),
    skill_json = REPLACE(IFNULL(skill_json, ''), '博特', '天工')
WHERE IFNULL(skill_name, '') LIKE '%博特%'
   OR IFNULL(skill_json, '') LIKE '%博特%';

-- 顶栏 / 登录页品牌名：platformName · platformSubName
-- 展示效果：天工 · AI 智能体开发平台
UPDATE bt_dc_param
SET param_val = '天工'
WHERE param_code = 'PLATFORM_NAME'
  AND IFNULL(param_val, '') <> '天工';

UPDATE bt_dc_param
SET param_val = 'AI 智能体开发平台'
WHERE param_code = 'PLATFORM_SUB_NAME'
  AND IFNULL(param_val, '') <> 'AI 智能体开发平台';

-- 参数 / 公共配置（其余含「博特」的文案）
UPDATE bt_dc_param
SET param_desc = REPLACE(IFNULL(param_desc, ''), '博特', '天工'),
    param_name = REPLACE(IFNULL(param_name, ''), '博特', '天工'),
    param_val = REPLACE(IFNULL(param_val, ''), '博特', '天工')
WHERE IFNULL(param_desc, '') LIKE '%博特%'
   OR IFNULL(param_name, '') LIKE '%博特%'
   OR IFNULL(param_val, '') LIKE '%博特%';

UPDATE bt_dc_public
SET pname = REPLACE(IFNULL(pname, ''), '博特', '天工'),
    comments = REPLACE(IFNULL(comments, ''), '博特', '天工'),
    codea = REPLACE(IFNULL(codea, ''), '博特', '天工')
WHERE IFNULL(pname, '') LIKE '%博特%'
   OR IFNULL(comments, '') LIKE '%博特%'
   OR IFNULL(codea, '') LIKE '%博特%';

-- 接口说明 / 插件说明 / 技能属性说明
UPDATE bt_api
SET api_name = REPLACE(IFNULL(api_name, ''), '博特', '天工'),
    api_desc = REPLACE(IFNULL(api_desc, ''), '博特', '天工'),
    api_detail = REPLACE(IFNULL(api_detail, ''), '博特', '天工'),
    response_json = REPLACE(IFNULL(response_json, ''), '博特', '天工'),
    example_json = REPLACE(IFNULL(example_json, ''), '博特', '天工'),
    remark = REPLACE(IFNULL(remark, ''), '博特', '天工')
WHERE IFNULL(api_name, '') LIKE '%博特%'
   OR IFNULL(api_desc, '') LIKE '%博特%'
   OR IFNULL(api_detail, '') LIKE '%博特%'
   OR IFNULL(response_json, '') LIKE '%博特%'
   OR IFNULL(example_json, '') LIKE '%博特%'
   OR IFNULL(remark, '') LIKE '%博特%';

UPDATE bt_plugin
SET plugin_name = REPLACE(IFNULL(plugin_name, ''), '博特', '天工'),
    plugin_desc = REPLACE(IFNULL(plugin_desc, ''), '博特', '天工'),
    plugin_detail = REPLACE(IFNULL(plugin_detail, ''), '博特', '天工')
WHERE IFNULL(plugin_name, '') LIKE '%博特%'
   OR IFNULL(plugin_desc, '') LIKE '%博特%'
   OR IFNULL(plugin_detail, '') LIKE '%博特%';

UPDATE bt_skill_attr_value
SET attr_value_name = REPLACE(IFNULL(attr_value_name, ''), '博特', '天工'),
    attr_value_desc = REPLACE(IFNULL(attr_value_desc, ''), '博特', '天工'),
    attr_value = REPLACE(IFNULL(attr_value, ''), '博特', '天工'),
    remark = REPLACE(IFNULL(remark, ''), '博特', '天工')
WHERE IFNULL(attr_value_name, '') LIKE '%博特%'
   OR IFNULL(attr_value_desc, '') LIKE '%博特%'
   OR IFNULL(attr_value, '') LIKE '%博特%'
   OR IFNULL(remark, '') LIKE '%博特%';

-- DocChain 说明文案（备份表）
UPDATE bt_docchain_extra_cfg_bak
SET tip = REPLACE(IFNULL(tip, ''), '博特', '天工')
WHERE IFNULL(tip, '') LIKE '%博特%';
