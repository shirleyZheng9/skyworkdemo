package com.iwhalecloud.bote.beyond;

import com.iwhalecloud.bote.common.consts.PublishResourceConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.JsonSchemaUtil;
import com.iwhalecloud.bote.config.properties.BeyondProperties;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.beyond.param.AgentParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.BotParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.McpParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.ToolDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowWithParamDTO;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 百应平台参数构建工具类
 *
 * @author lizuyin
 * @since 2025-07-28
 */
public final class BeyondParamHelper {

  private BeyondParamHelper() {
    // 禁止实例化
  }

  private static final String TOOL_URL = "bote/flow/run/";
  private static final String SCENE_URL = "bote/beyond/chat?sceneId=";
  private static final String BOT_HOME_URL_TEMPLATE = "/bote/#/driver/bot?ownerTenantId={tenantId}&tenantId={tenantId}&botId={botId}&modeType=single&systemCode=BYAI&sso-token={sso-token}";
  private static final BeyondProperties beyondProperties = SpringUtil.getBean(BeyondProperties.class);

  /**
   * 将工作流参数转换为工具参数
   *
   * @param flow 工作流信息
   * @return 工具参数
   */
  public static ToolDTO convertFromFlow(SkillFlowWithParamDTO flow) {
    ToolDTO toolParam = new ToolDTO();
    toolParam.setTransferType(PublishResourceConsts.TRANSFER_TYPE);
    String urlOri = SystemParameter.BOTE_API_URL.getValueFromEnv() + TOOL_URL + flow.getFlowId();
    // 添加 tenantId 参数
    Long tenantId = flow.getTenantId();
    urlOri += "?tenantId=" + tenantId;
    toolParam.setUrlOri(urlOri);
    if (StringUtils.isNotEmpty(flow.getRequestJson())) {
      JsonSchemaNode inputSchema = convertParameterSpecToSchema(flow.getRequestJson());
      toolParam.setInputSchema(inputSchema);
    }
    if (StringUtils.isNotEmpty(flow.getResponseJson())) {
      JsonSchemaNode outputSchema = convertParameterSpecToSchema(flow.getResponseJson());
      toolParam.setOutputSchema(outputSchema);
    }
    return toolParam;
  }

  /**
   * 构建MCP参数
   *
   * @param mcpServer MCP服务信息
   * @return MCP参数
   */
  public static McpParamDTO buildMcpParam(McpServerDTO mcpServer) {
    McpParamDTO mcpParam = new McpParamDTO();
    mcpParam.setMcpTransferType(mcpServer.getServerType());
    mcpParam.setMcpTimeout(PublishResourceConsts.DEFAULT_MCP_TIMEOUT);
    mcpParam.setMcpServerUrlOri(mcpServer.getServerUrl());
    return mcpParam;
  }

  /**
   * 构建智能体参数
   *
   * @param scene 场景信息
   * @return 智能体参数
   */
  public static AgentParamDTO buildAgentParam(BotSceneDTO scene) {
    AgentParamDTO agentParam = new AgentParamDTO();
    String agentType = mapSceneTypeToAgentType(scene.getSceneType());
    agentParam.setAgentType(agentType);
    String agentSseUrlOri = SystemParameter.BOTE_API_URL.getValueFromEnv() + SCENE_URL + scene.getSceneId();
    // 添加 tenantId 参数
    Long tenantId = scene.getTenantId();
    agentSseUrlOri += "&tenantId=" + tenantId;
    agentParam.setAgentSseUrlOri(agentSseUrlOri);
    return agentParam;
  }

  /**
   * 构建数字员工参数
   *
   * @param bot 场景信息
   * @return 数字员工参数
   */
  public static BotParamDTO buildBotParam(BotDTO bot) {
    BotParamDTO botParam = new BotParamDTO();
    botParam.setHomeType("custom");
    String byaiURL = beyondProperties.getApiUrl() + BOT_HOME_URL_TEMPLATE.replace("{tenantId}", String.valueOf(bot.getTenantId()))
      .replace("{botId}", String.valueOf(bot.getBotId()));
    botParam.setAgentHomeUrl(byaiURL);
    botParam.setAgentWebUrlOri(byaiURL);
    botParam.setAuthType("oauth2");
    botParam.setCreateType("FROM_THIRD");
    botParam.setAgentDevType("byai");
    botParam.setAgentType("001");
    botParam.setIntegrationType("PAGE");
    return botParam;
  }

  /**
   * 映射场景类型到智能体类型
   *
   * @param sceneType 场景类型
   * @return 智能体类型
   */
  private static String mapSceneTypeToAgentType(String sceneType) {
    switch (sceneType) {
      case SceneConsts.SCENE_TYPE_SCENE:
        return "001"; // 综合类智能体
      case SceneConsts.SCENE_TYPE_CHATFLOW:
        return "002"; // 流程操作类智能体
      case SceneConsts.SCENE_TYPE_KNOWLEDGE:
        return "003"; // 文档问答类智能体
      default:
        return "001"; // 默认综合类智能体
    }
  }

  /**
   * 将 JSON 字符串转换为 JsonSchemaNode
   * 全部使用 JsonSchemaUtil.convertRoot 优化转换，解析失败直接报错
   *
   * @param jsonString JSON 字符串
   * @return JsonSchemaNode
   */
  private static JsonSchemaNode convertParameterSpecToSchema(String jsonString) {
    ParameterSpec parameterSpec = JsonUtil.parseJsonRequired(jsonString, ParameterSpec.class);
    JsonSchemaNode jsonSchemaNode = JsonSchemaUtil.convertRoot(parameterSpec);
    if (jsonSchemaNode == null) {
      return JsonSchemaNode.newObject();
    }
    return jsonSchemaNode;
  }
}
