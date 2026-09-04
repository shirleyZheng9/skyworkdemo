package com.iwhalecloud.bote.common.diffc.factory;

import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneParamDTO;
import com.iwhalecloud.bote.dto.bot.BotScenePromptDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.bot.ClawEnvVariableDTO;
import com.iwhalecloud.bote.dto.bot.ClawWorkspaceDTO;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.database.DataTableColumnDTO;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.model.LargeModelDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientRedirectUriDTO;
import com.iwhalecloud.bote.dto.plugin.PluginDTO;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.TenantUserDTO;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceInstDTO;
import com.iwhalecloud.bote.dto.skill.PromptContentDTO;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.ServiceMockDTO;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrValueRelDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowParamDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageCompDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bss.litchi.diffc.factory.IDSupplierFactory;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import org.springframework.stereotype.Component;

/**
 * 主键提供者工厂初始化
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class IDSupplierFactoryInitializer {
  public IDSupplierFactoryInitializer() {

    // 租户
    IDSupplierFactory.register(TenantDTO.class, Sequences.TENANT_ID::next);
    // 用户
    IDSupplierFactory.register(UserDTO.class, Sequences.USER_ID::next);
    // 租户用户
    IDSupplierFactory.register(TenantUserDTO.class, Sequences.TENANT_USER_ID::next);
    // 租户设置信息
    IDSupplierFactory.register(TenantSettingInfoDTO.class, Sequences.TENANT_SETTING_INFO_SETTING_ID::next);

    // 外部门户
    IDSupplierFactory.register(ExternalPortalDTO.class, Sequences.EXTERNAL_PORTAL_ID::next);
    // 权限
    IDSupplierFactory.register(PrivDTO.class, Sequences.PRIV_ID::next);

    // 大模型
    IDSupplierFactory.register(LargeModelDTO.class, Sequences.LARGE_MODEL_ID::next);
    // 模型微调
    IDSupplierFactory.register(ModelFinetuneDTO.class, Sequences.MODEL_FINETUNE_ID::next);

    // 机器人
    IDSupplierFactory.register(BotDTO.class, Sequences.BOT_ID::next);
    // 用户会话辅助信息
    IDSupplierFactory.register(BotUserExperienceDTO.class, Sequences.BOT_USER_EXPERIENCE_ID::next);

    // 鉴权
    IDSupplierFactory.register(ApiAuthDTO.class, Sequences.API_AUTH_ID::next);
    // 鉴权指令
    IDSupplierFactory.register(CopilotPointDTO.class, Sequences.API_AUTH_POINT_ID::next);

    // 场景
    IDSupplierFactory.register(BotSceneDTO.class, Sequences.BOT_SCENE_ID::next);
    // 场景变量
    IDSupplierFactory.register(BotSceneParamDTO.class, Sequences.BOT_SCENE_PARAM_ID::next);
    // 场景提示词
    IDSupplierFactory.register(BotScenePromptDTO.class, Sequences.BOT_SCENE_PROMPT_ID::next);
    // 场景关联的技能
    IDSupplierFactory.register(BotSceneSkillDTO.class, Sequences.BOT_SCENE_SKILL_ID::next);
    // 场景关联的环境变量
    IDSupplierFactory.register(ClawEnvVariableDTO.class, Sequences.BOT_CLAW_ENV_VARIABLE_ID::next);
    // 场景关联的工作空间
    IDSupplierFactory.register(ClawWorkspaceDTO.class, Sequences.BOT_CLAW_WORKSPACE_ID::next);

    // 技能：属性规格
    IDSupplierFactory.register(SkillAttrSpecDTO.class, Sequences.SKILL_ATTR_SPEC_ID::next);
    // 技能：属性值规格
    IDSupplierFactory.register(SkillAttrValueDTO.class, Sequences.SKILL_ATTR_VALUE_ID::next);
    // 技能：属性值关联
    IDSupplierFactory.register(SkillAttrValueRelDTO.class, Sequences.SKILL_ATTR_REL_ID::next);
    // 技能：页面
    IDSupplierFactory.register(SkillPageDTO.class, Sequences.SKILL_PAGE_ID::next);
    // 技能：页面组件
    IDSupplierFactory.register(SkillPageCompDTO.class, Sequences.SKILL_PAGE_COMP_ID::next);
    // 技能：页面函数
    IDSupplierFactory.register(SkillPageFuncDTO.class, Sequences.SKILL_PAGE_FUNC_ID::next);
    // 技能：服务函数
    IDSupplierFactory.register(SkillFunctionDTO.class, Sequences.SKILL_FUNCTION_ID::next);
    // 技能: Agent Skill
    IDSupplierFactory.register(AgentSkillDTO.class, IDUtils::nextId);
    // 技能：插件
    IDSupplierFactory.register(SkillPluginDTO.class, Sequences.SKILL_PLUGIN_ID::next);
    // 技能：API
    IDSupplierFactory.register(SkillServiceDTO.class, Sequences.SKILL_SERVICE_ID::next);
    // 技能：API 平台
    IDSupplierFactory.register(ServicePlatformDTO.class, Sequences.SERVICE_PLATFORM_ID::next);
    // 技能：API 网关
    IDSupplierFactory.register(ServiceGatewayDTO.class, Sequences.SERVICE_GATEWAY_ID::next);
    // 技能：模拟响应报文
    IDSupplierFactory.register(ServiceMockDTO.class, Sequences.SERVICE_MOCK_RSP_ID::next);
    // 技能：SQL
    IDSupplierFactory.register(SkillSqlDTO.class, Sequences.SKILL_SQL_ID::next);
    // 技能：对象
    IDSupplierFactory.register(SkillObjectDTO.class, Sequences.SKILL_OBJECT_ID::next);
    // 技能：文本
    IDSupplierFactory.register(SkillTextDTO.class, Sequences.SKILL_TEXT_ID::next);
    // 技能：数据源
    IDSupplierFactory.register(DataSourceDTO.class, Sequences.DATA_SOURCE_ID::next);
    // 技能：数据源实例
    IDSupplierFactory.register(DataSourceInstDTO.class, Sequences.DATA_SOURCE_INST_ID::next);
    // 技能：提示词
    IDSupplierFactory.register(PromptDTO.class, Sequences.PROMPT_ID::next);
    // 技能：提示词内容
    IDSupplierFactory.register(PromptContentDTO.class, Sequences.CONTENT_ID::next);
    // 技能：流程
    IDSupplierFactory.register(SkillFlowDTO.class, Sequences.SKILL_FLOW_ID::next);
    // 技能：流程参数
    IDSupplierFactory.register(SkillFlowParamDTO.class, Sequences.SKILL_FLOW_PARAM_ID::next);
    // // 知识库
    // IDSupplierFactory.register(KnowledgeBaseDTO.class, Sequences.KNOWLEDGE_BASE_KNOWLEDGE_ID::next);
    // 文档
    IDSupplierFactory.register(DocumentDTO.class, Sequences.DOCUMENT_DOC_ID::next);
    // 文件信息
    IDSupplierFactory.register(FileInfoDTO.class, Sequences.FILE_INFO_ID::next);
    IDSupplierFactory.register(DocumentDTO.class, Sequences.DOCUMENT_DOCUMENT_ID::next);
    // 语料基本信息
    IDSupplierFactory.register(CorpusInfoDTO.class, Sequences.CORPUS_ID::next);

    // 插件
    IDSupplierFactory.register(PluginDTO.class, Sequences.PLUGIN_ID::next);

    // mcp
    IDSupplierFactory.register(McpServerDTO.class, Sequences.SEQ_BT_MCP_SERVER_SERVER_ID::next);

    // A2A
    IDSupplierFactory.register(A2aPlatformDTO.class, IDUtils::nextId);
    IDSupplierFactory.register(A2aAgentDTO.class, IDUtils::nextId);

    // 应用广场
    IDSupplierFactory.register(PlatBotInfoDTO.class, Sequences.SEQ_BT_PLAT_BOT_INFO_PLAT_BOT_ID::next);
    // oauth2客户端
    IDSupplierFactory.register(OAuth2ClientDTO.class, Sequences.OAUTH2_CLIENT_ID::next);
    // oauth2客户端关联url
    IDSupplierFactory.register(OAuth2ClientRedirectUriDTO.class, Sequences.OAUTH2_CLIENT_URL_ID::next);

    IDSupplierFactory.register(PlatSceneInfoDTO.class, Sequences.SEQ_BT_PLAT_SCENE_INFO_ID::next);

    // 主题
    IDSupplierFactory.register(ChatThemeDTO.class, Sequences.CHAT_THEME_ID::next);
    // 回复主题
    IDSupplierFactory.register(ChatReplyThemeDTO.class, Sequences.CHAT_REPLY_THEME_ID::next);
    // 网页应用
    IDSupplierFactory.register(WebAppDTO.class, Sequences.WEB_APP_ID::next);
    // 工作台应用
    IDSupplierFactory.register(WorkbenchAppDTO.class, Sequences.WORKBENCH_APP_ID::next);

    // 环境变量
    IDSupplierFactory.register(EnvVariableDTO.class, Sequences.ENV_VARIABLE_ID::next);
    // 环境变量值
    IDSupplierFactory.register(EnvVariableValDTO.class, Sequences.ENV_VARIABLE_VAL_ID::next);

    // 业务数据表
    IDSupplierFactory.register(DataTableDTO.class, Sequences.DATA_TABLE_ID::next);
    // 业务数据表字段
    IDSupplierFactory.register(DataTableColumnDTO.class, Sequences.DATA_TABLE_COLUMN_ID::next);

    // 在线环境维护
    IDSupplierFactory.register(PublishGatewayDTO.class, Sequences.PUBLISH_GATEWAY_ID::next);

    // 渠道
    IDSupplierFactory.register(AiChannelDTO.class, Sequences.AI_CHANNEL_ID::next);
    // 技能发布申请
    IDSupplierFactory.register(SkillPublishApplyDTO.class, Sequences.SEQ_SKILL_PUBLISH_APPLY_ID::next);
  }
}
