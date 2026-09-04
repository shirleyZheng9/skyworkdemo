package com.iwhalecloud.bote.common.diffc.factory;

import com.iwhalecloud.bote.common.diffc.persist.impl.ApiAuthDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.EnvVariableDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.EnvVariableValDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.ExternalPortalDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.LargeModelDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.McpServerDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.ModelFinetuneDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.OAuth2ClientDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.OAuth2ClientRedirectUriDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.PrivDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.ServiceMockDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.TenantDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.TenantSettingInfoDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.TenantUserDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.UserDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.a2a.A2aAgentDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.a2a.A2aPlatformDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.app.WebAppDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.app.WorkbenchAppDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotSceneDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotScenePromptIDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotSceneSkillIDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotSceneVariableDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.BotUserExperienceDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.ChatReplyThemeDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.ChatThemeDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.ClawVariableDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.ClawWorkspaceDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.CopilotPointDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.bot.PlatBotInfoDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.channel.AiChannelDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.database.DataTableColumnDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.database.DataTableDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.knowledge.CorpusInfoDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.knowledge.DocumentDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.publish.PublishGatewayDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.publish.ResourcePublishRecordDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.scene.PlatSceneInfoDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.AgentSkillDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.DataSourceDifferencePersists;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.DataSourceInstDifferencePersists;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.PluginDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.PromptContentDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.PromptDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SKillPluginDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.ServiceGatewayDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.ServicePlatformDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillAttrSpecDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillAttrValueDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillAttrValueRelDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillFlowDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillFlowParamDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillFunctionDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillObjectDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillPageCompDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillPageDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillPageFuncDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillPublishApplyDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillServiceDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillSqlDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.skill.SkillTextDifferencePersistence;
import com.iwhalecloud.bote.common.diffc.persist.impl.suggestion.SuggestionTermDifferencePersistence;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
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
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
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
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.entity.suggestion.SuggestionTermEntity;
import com.iwhalecloud.bss.litchi.diffc.factory.BatchPersistenceFactory;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务工厂初始化
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class PersistenceFactoryInitializer {

  public PersistenceFactoryInitializer() {

    // 租户
    BatchPersistenceFactory.register(TenantDTO.class, TenantDifferencePersistence.class);
    // 用户
    BatchPersistenceFactory.register(UserDTO.class, UserDifferencePersistence.class);
    // 租户用户
    BatchPersistenceFactory.register(TenantUserDTO.class, TenantUserDifferencePersistence.class);
    // 租户设置信息
    BatchPersistenceFactory.register(TenantSettingInfoDTO.class, TenantSettingInfoDifferencePersistence.class);

    // 外部门户
    BatchPersistenceFactory.register(ExternalPortalDTO.class, ExternalPortalDifferencePersistence.class);
    // 权限
    BatchPersistenceFactory.register(PrivDTO.class, PrivDifferencePersistence.class);

    // 大模型
    BatchPersistenceFactory.register(LargeModelDTO.class, LargeModelDifferencePersistence.class);
    // 模型微调
    BatchPersistenceFactory.register(ModelFinetuneDTO.class, ModelFinetuneDifferencePersistence.class);

    // 机器人
    BatchPersistenceFactory.register(BotDTO.class, BotDifferencePersistence.class);
    // 用户会话辅助信息
    BatchPersistenceFactory.register(BotUserExperienceDTO.class, BotUserExperienceDifferencePersistence.class);

    // 鉴权
    BatchPersistenceFactory.register(ApiAuthDTO.class, ApiAuthDifferencePersistence.class);
    // 鉴权指令
    BatchPersistenceFactory.register(CopilotPointDTO.class, CopilotPointDifferencePersistence.class);

    // 场景
    BatchPersistenceFactory.register(BotSceneDTO.class, BotSceneDifferencePersistence.class);
    // 场景变量
    BatchPersistenceFactory.register(BotSceneParamDTO.class, BotSceneVariableDifferencePersistence.class);
    // 场景提示词
    BatchPersistenceFactory.register(BotScenePromptDTO.class, BotScenePromptIDifferencePersistence.class);
    // 场景关联的技能
    BatchPersistenceFactory.register(BotSceneSkillDTO.class, BotSceneSkillIDifferencePersistence.class);
    // 场景关联的环境变量
    BatchPersistenceFactory.register(ClawEnvVariableDTO.class, ClawVariableDifferencePersistence.class);
    // 场景关联的工作空间
    BatchPersistenceFactory.register(ClawWorkspaceDTO.class, ClawWorkspaceDifferencePersistence.class);

    // 技能：属性规格
    BatchPersistenceFactory.register(SkillAttrSpecDTO.class, SkillAttrSpecDifferencePersistence.class);
    // 技能：属性值规格
    BatchPersistenceFactory.register(SkillAttrValueDTO.class, SkillAttrValueDifferencePersistence.class);
    // 技能： 属性值关系
    BatchPersistenceFactory.register(SkillAttrValueRelDTO.class, SkillAttrValueRelDifferencePersistence.class);
    // 技能：页面
    BatchPersistenceFactory.register(SkillPageDTO.class, SkillPageDifferencePersistence.class);
    // 技能：页面组件
    BatchPersistenceFactory.register(SkillPageCompDTO.class, SkillPageCompDifferencePersistence.class);
    // 技能：页面函数
    BatchPersistenceFactory.register(SkillPageFuncDTO.class, SkillPageFuncDifferencePersistence.class);
    // 技能：服务函数
    BatchPersistenceFactory.register(SkillFunctionDTO.class, SkillFunctionDifferencePersistence.class);
    // 技能：SQL
    BatchPersistenceFactory.register(SkillSqlDTO.class, SkillSqlDifferencePersistence.class);
    // 技能：插件
    BatchPersistenceFactory.register(SkillPluginDTO.class, SKillPluginDifferencePersistence.class);
    // 技能：对象
    BatchPersistenceFactory.register(SkillObjectDTO.class, SkillObjectDifferencePersistence.class);
    // 技能：文本
    BatchPersistenceFactory.register(SkillTextDTO.class, SkillTextDifferencePersistence.class);
    // 技能：API
    BatchPersistenceFactory.register(SkillServiceDTO.class, SkillServiceDifferencePersistence.class);
    // 技能：API 平台
    BatchPersistenceFactory.register(ServicePlatformDTO.class, ServicePlatformDifferencePersistence.class);
    // 技能：API 网关
    BatchPersistenceFactory.register(ServiceGatewayDTO.class, ServiceGatewayDifferencePersistence.class);
    // 技能：模拟响应报文
    BatchPersistenceFactory.register(ServiceMockDTO.class, ServiceMockDifferencePersistence.class);
    // 技能：数据源
    BatchPersistenceFactory.register(DataSourceDTO.class, DataSourceDifferencePersists.class);
    // 技能：数据源实例
    BatchPersistenceFactory.register(DataSourceInstDTO.class, DataSourceInstDifferencePersists.class);
    // 技能：提示词
    BatchPersistenceFactory.register(PromptDTO.class, PromptDifferencePersistence.class);
    // 技能：提示词内容
    BatchPersistenceFactory.register(PromptContentDTO.class, PromptContentDifferencePersistence.class);
    // 技能：流程
    BatchPersistenceFactory.register(SkillFlowDTO.class, SkillFlowDifferencePersistence.class);
    // 技能：流程参数
    BatchPersistenceFactory.register(SkillFlowParamDTO.class, SkillFlowParamDifferencePersistence.class);
    // 技能：Agent Skill
    BatchPersistenceFactory.register(AgentSkillDTO.class, AgentSkillDifferencePersistence.class);

    // 文档
    BatchPersistenceFactory.register(DocumentDTO.class, DocumentDifferencePersistence.class);
    // 语料基本信息
    BatchPersistenceFactory.register(CorpusInfoDTO.class, CorpusInfoDifferencePersistence.class);

    // 插件
    BatchPersistenceFactory.register(PluginDTO.class, PluginDifferencePersistence.class);
    // Mcp
    BatchPersistenceFactory.register(McpServerDTO.class, McpServerDifferencePersistence.class);

    // A2A
    BatchPersistenceFactory.register(A2aPlatformDTO.class, A2aPlatformDifferencePersistence.class);
    BatchPersistenceFactory.register(A2aAgentDTO.class, A2aAgentDifferencePersistence.class);

    // 应用广场
    BatchPersistenceFactory.register(PlatBotInfoDTO.class, PlatBotInfoDifferencePersistence.class);

    //oauth2客户端
    BatchPersistenceFactory.register(OAuth2ClientDTO.class, OAuth2ClientDifferencePersistence.class);

    //oauth2客户端 授权URL
    BatchPersistenceFactory.register(OAuth2ClientRedirectUriDTO.class, OAuth2ClientRedirectUriDifferencePersistence.class);
    // 模板智能体
    BatchPersistenceFactory.register(PlatSceneInfoDTO.class, PlatSceneInfoDifferencePersistence.class);

    // 对话主题
    BatchPersistenceFactory.register(ChatThemeDTO.class, ChatThemeDifferencePersistence.class);

    // 联想术语
    BatchPersistenceFactory.register(SuggestionTermEntity.class, SuggestionTermDifferencePersistence.class);

    // 资源发布记录
    BatchPersistenceFactory.register(ResourcePublishRecordDTO.class, ResourcePublishRecordDifferencePersistence.class);

    // 网页应用
    BatchPersistenceFactory.register(WebAppDTO.class, WebAppDifferencePersistence.class);
    // 工作台应用
    BatchPersistenceFactory.register(WorkbenchAppDTO.class, WorkbenchAppDifferencePersistence.class);

    // 环境变量
    BatchPersistenceFactory.register(EnvVariableDTO.class, EnvVariableDifferencePersistence.class);
    // 环境变量值
    BatchPersistenceFactory.register(EnvVariableValDTO.class, EnvVariableValDifferencePersistence.class);

    // 业务数据表
    BatchPersistenceFactory.register(DataTableDTO.class, DataTableDifferencePersistence.class);
    // 业务数据表字段
    BatchPersistenceFactory.register(DataTableColumnDTO.class, DataTableColumnDifferencePersistence.class);

    // 回复主题
    BatchPersistenceFactory.register(ChatReplyThemeDTO.class, ChatReplyThemeDifferencePersistence.class);

    // 技能：页面组件主题
    BatchPersistenceFactory.register(PublishGatewayDTO.class, PublishGatewayDifferencePersistence.class);

    // 渠道
    BatchPersistenceFactory.register(AiChannelDTO.class, AiChannelDifferencePersistence.class);
    // 技能发布申请
    BatchPersistenceFactory.register(SkillPublishApplyDTO.class, SkillPublishApplyDifferencePersistence.class);

  }

}
