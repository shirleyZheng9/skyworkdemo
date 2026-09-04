package com.iwhalecloud.bote.service.a2a.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.A2aUtil;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.PublishA2aAgentRequest;
import com.iwhalecloud.bote.dto.a2a.UnpublishA2aAgentRequest;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.mapper.a2a.A2aAgentMapper;
import com.iwhalecloud.bote.mapper.a2a.A2aPlatformMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.a2a.IA2aAgentManageService;
import com.iwhalecloud.bote.service.a2a.IA2aOpenApiService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.a2a.spec.APIKeySecurityScheme;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentProvider;
import io.a2a.spec.HTTPAuthSecurityScheme;
import io.a2a.spec.SecurityScheme;
import io.a2a.spec.TransportProtocol;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A2A 开放接口
 *
 * @author bianjp
 * @since 2025-09-09
 */
@Service
@RequiredArgsConstructor
public class A2aOpenApiServiceImpl implements IA2aOpenApiService, InitializingBean {
  private final A2aPlatformMapper platformMapper;
  private final A2aAgentMapper agentMapper;
  private final BotSceneManageMapper sceneManageMapper;
  private final IA2aAgentManageService agentManageService;
  private final ResourcePublishRecordMapper publishRecordMapper;

  // https://swagger.io/docs/specification/v3_0/authentication/#using-multiple-authentication-types
  /** 鉴权方式配置，用于配置每种鉴权方式如何使用。支持 Authorization 请求头、URL 参数两种鉴权方式，可以任选一种 */
  private final Map<String, SecurityScheme> securitySchemes = Map.of(
    "bearerAuth", new HTTPAuthSecurityScheme(null, "bearer", "密钥"),
    "tokenAuth", new APIKeySecurityScheme("query", "token", "密钥"));
  /** 鉴权组合列表，用于配置应该使用哪些鉴权方式。列表元素间的关系为 OR (使用任意一种鉴权方式即可), Map 元素的关系为 AND (必须同时使用所有鉴权方式) */
  private final List<Map<String, List<String>>> security = List.of(
    Map.of("bearerAuth", List.of()),
    Map.of("tokenAuth", List.of()));

  /** 智能体执行器地址模板. 对应接口: {@link com.iwhalecloud.bote.controller.a2a.A2aOpenApiController#invokeAgent} */
  private String agentExecutorUrlTemplate;
  /** 智能体图标地址模板 */
  private String agentIconUrlTemplate;
  /** 平台访问地址 */
  private String accessUrl;

  @Override
  public void afterPropertiesSet() {
    String baseUrl = StringUtils.stripEnd(SystemParameter.BOTE_API_URL.getValueFromEnv(), "/");
    this.agentExecutorUrlTemplate = baseUrl + "/bote/a2a/agent/%s/%s";
    this.agentIconUrlTemplate = baseUrl + "/bote/manager/scene/sceneIcon?tenantId=%s&sceneId=%s";
    String webUrl = SystemParameter.BOTE_WEB_URL.getValueFromEnv();
    if (StringUtils.isNotEmpty(webUrl)) {
      this.accessUrl = StringUtils.stripEnd(webUrl, "/");
    }
    else {
      this.accessUrl = baseUrl;
    }
  }

  @Override
  @Transactional
  public ResultVO<Void> publishAgent(PublishA2aAgentRequest request, String apiKey) {
    // 校验平台和密钥
    ResultVO<A2aPlatformDTO> checkResult = checkPlatformAndApiKey(request.getTenantId(), request.getSystemCode(), apiKey);
    if (!checkResult.isSuccess()) {
      return new ResultVO<>(checkResult);
    }
    A2aPlatformDTO platform = checkResult.getResultObject();

    // 获取智能体卡片
    AgentCard agentCard = A2aUtil.getAgentCard(request.getAgentCardUrl(), null);

    A2aAgentDTO old = agentMapper.selectAgentByExtAgentId(request.getTenantId(), platform.getPlatformId(), request.getAgentId());
    A2aAgentDTO agent = old == null ? new A2aAgentDTO() : BeanUtil.copy(old, new A2aAgentDTO());
    agent.setAgentName(agentCard.name());
    agent.setAgentDesc(agentCard.description());
    agent.setAgentCardUrl(request.getAgentCardUrl());
    agent.setAgentCard(agentCard);
    if (old == null) {
      agent.setExtAgentId(request.getAgentId());
      agent.setPlatformId(platform.getPlatformId());
      agent.setTenantId(request.getTenantId());
      agent.setStatusCd(BaseConsts.STATUS_CD_VALID);
      agent.setCreatorId(BaseConsts.USER_ID_API);
    }
    agent.setUpdatorId(BaseConsts.USER_ID_API);

    ResultVO<A2aAgentDTO> result = agentManageService.saveA2aAgent(agent);
    // 数据无变化时当作成功
    if (BaseErrorConstant.NO_DIFFERENCE.getErrorConstant().getCode().equals(result.getResultCode())) {
      return ResultVO.successWithMsg("智能体无变化");
    }
    return new ResultVO<>(result);
  }

  @Override
  @Transactional
  public ResultVO<Void> unpublishAgent(UnpublishA2aAgentRequest request, String apiKey) {
    // 校验平台和密钥
    ResultVO<A2aPlatformDTO> checkResult = checkPlatformAndApiKey(request.getTenantId(), request.getSystemCode(), apiKey);
    if (!checkResult.isSuccess()) {
      return new ResultVO<>(checkResult);
    }
    A2aPlatformDTO platform = checkResult.getResultObject();
    A2aAgentDTO old = agentMapper.selectAgentByExtAgentId(request.getTenantId(), platform.getPlatformId(), request.getAgentId());
    if (old == null) {
      return new ResultVO<>("404", "A2A 智能体不存在");
    }
    return agentManageService.deleteA2aAgent(request.getTenantId(), old.getAgentId(), BaseConsts.USER_ID_API);
  }

  /**
   * 校验平台和密钥
   */
  private ResultVO<A2aPlatformDTO> checkPlatformAndApiKey(Long tenantId, String systemCode, String apiKey) {
    A2aPlatformDTO platform = platformMapper.selectPlatformByCode(tenantId, systemCode);
    if (platform == null) {
      return new ResultVO<>("404", "A2A 平台不存在");
    }
    if (!Objects.equals(platform.getPublishKey(), apiKey)) {
      return new ResultVO<>("401", "密钥错误");
    }
    return ResultVO.success(platform);
  }

  @Override
  @Nullable
  public AgentCard getAgentCard(Long tenantId, Long agentId) {
    ResourcePublishRecordDTO publishRecord = publishRecordMapper.getRecordByResourceAndChannel(agentId, PublishChannelEnum.A2A.getCode(), tenantId);
    if (publishRecord == null) {
      return null;
    }
    BotSceneEntity scene = sceneManageMapper.selectSceneBasicInfo(tenantId, agentId);
    if (scene == null) {
      return null;
    }
    PublishChannelDTO publishChannel = JsonUtil.parseJsonRequired(publishRecord.getPublishParams(), PublishChannelDTO.class);

    return new AgentCard.Builder()
      .protocolVersion("0.3.0")
      .name(StringUtils.defaultIfEmpty(publishChannel.getAgentName(), scene.getSceneName()))
      .description(StringUtils.defaultIfEmpty(publishChannel.getAgentDesc(), scene.getSceneDesc()))
      .url(agentExecutorUrlTemplate.formatted(tenantId, agentId))
      .iconUrl(agentIconUrlTemplate.formatted(tenantId, agentId))
      .preferredTransport(TransportProtocol.JSONRPC.asString())
      .provider(new AgentProvider(SystemParameter.PLATFORM_NAME.getValueFromDb(), accessUrl))
      .version("1.0.0")
      .capabilities(new AgentCapabilities.Builder().streaming(true).pushNotifications(true).build())
      .securitySchemes(securitySchemes)
      .security(security)
      .defaultInputModes(List.of(MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE))
      .defaultOutputModes(List.of(MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_MARKDOWN_VALUE))
      .skills(List.of())
      .supportsAuthenticatedExtendedCard(false)
      .build();
  }
}
