package com.iwhalecloud.bote.service.publish.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.beyond.BeyondApiClient;
import com.iwhalecloud.bote.beyond.BeyondParamHelper;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.PublishResourceConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BeyondResourceBizTypeEnum;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogItem;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondResourcePublishData;
import com.iwhalecloud.bote.dto.beyond.BeyondResourcePublishRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondResponse;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.beyond.ResourceDTO;
import com.iwhalecloud.bote.dto.beyond.param.AgentParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.BotParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.McpParamDTO;
import com.iwhalecloud.bote.dto.beyond.param.ToolDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordQueryParams;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRequest;
import com.iwhalecloud.bote.dto.publish.ResourceUnpublishRequest;
import com.iwhalecloud.bote.dto.skill.SkillFlowWithParamDTO;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import com.iwhalecloud.bote.service.publish.IPublishResourceService;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import org.springframework.lang.Nullable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 发布资源服务实现
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Service
@RequiredArgsConstructor
public class PublishResourceServiceImpl implements IPublishResourceService {
  private static final Logger logger = LoggerFactory.getLogger(PublishResourceServiceImpl.class);
  private static final String BOTE_API_URL = SystemParameter.BOTE_API_URL.getValueFromEnv();
  private static final String BOTE_OPEN_API_URL = SystemParameter.BOTE_OPEN_API_URL.getValueFromEnv();
  private final SdkConnectionManager sdkConnectionManager;
  private final McpServerManageMapper mcpServerManageMapper;
  private final ResourcePublishRecordMapper resourcePublishRecordMapper;
  private final BotSceneManageMapper botSceneManageMapper;
  private final BotQueryMapper botQueryMapper;
  private final SkillFlowManageMapper skillFlowManageMapper;

  private BeyondApiClient beyondApiClient;

  @Autowired(required = false)
  public void setBeyondApiClient(BeyondApiClient beyondApiClient) {
    this.beyondApiClient = beyondApiClient;
  }

  @Override
  public ResultVO<List<BeyondCatalogItem>> queryCatalogTree(BeyondCatalogTreeRequest request) {
    BeyondResponse<List<BeyondCatalogItem>> response = beyondApiClient.queryCatalogTree(request);
    Assert.notNull(response, "百应接口返回结果为空");
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "查询发布目录树失败"));
    }
    return ResultVO.success(response.getData());
  }

  @Override
  public ResultVO<List<BeyondOrgItem>> getOrgTree(BeyondOrgTreeRequest request) {
    BeyondResponse<List<BeyondOrgItem>> response = beyondApiClient.getOrgTree(request);
    Assert.notNull(response, "百应接口返回结果为空");
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "查询组织树失败"));
    }
    return ResultVO.success(response.getData());
  }

  @Override
  public ResultVO<List<BeyondOrgAdminItem>> getOrgAdmin(BeyondOrgAdminRequest request) {
    BeyondResponse<List<BeyondOrgAdminItem>> response = beyondApiClient.getOrgAdmin(request);
    Assert.notNull(response, "百应接口返回结果为空");
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "查询组织管理员失败"));
    }
    return ResultVO.success(response.getData());
  }

  @Override
  public ResultVO<BeyondOrgDetailItem> getOrgDetail(BeyondOrgDetailRequest request) {
    BeyondResponse<BeyondOrgDetailItem> response = beyondApiClient.getOrgDetail(request);
    Assert.notNull(response, "百应接口返回结果为空");
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "查询组织详情失败"));
    }
    return ResultVO.success(response.getData());
  }

  /**
   * 百应平台发布资源
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  @Override
  public ResultVO<?> beyondPublishResource(ResourcePublishRequest request) {
    return switch (request.getResourceType()) {
      case MCP -> beyondPublishMCP(request);
      case TOOL -> beyondPublishTool(request);
      case AGENT -> beyondPublishAgent(request);
      case BOT -> beyondPublishBot(request);
      default -> ResultVO.fail("不支持的资源类型：" + request.getResourceType().getDesc());
    };
  }

  /**
   * 微信公众号发布资源
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  @Override
  public ResultVO<String> wechatPublishResource(ResourcePublishRequest request) {
    if (request.getResourceType() == BeyondResourceBizTypeEnum.AGENT) {
      return wechatPublishAgent(request);
    }
    return ResultVO.fail("不支持的资源类型：" + request.getResourceType().getDesc());
  }

  @Override
  public ResultVO<String> a2aPublishResource(ResourcePublishRequest request) {
    Assert.isTrue(request.getResourceType() == BeyondResourceBizTypeEnum.AGENT, "不支持的资源类型：" + request.getResourceType().getDesc());
    Assert.isTrue(request.getResourceIdList().size() == 1, "A2A 渠道不支持批量发布");

    Long sceneId = request.getResourceIdList().getFirst();
    ResourcePublishRecordDTO oldRecord = resourcePublishRecordMapper.getRecordByResourceAndChannel(sceneId, request.getPublishChannel().getCode(), request.getTenantId());
    ResourcePublishRecordDTO record;
    if (oldRecord == null) {
      record = new ResourcePublishRecordDTO();
      record.setRecordId(IDUtils.nextId());
      record.setPublishChannel(request.getPublishChannel().getCode());
      record.setResourceType(request.getResourceType().getCode());
      record.setResourceId(sceneId);
      record.setExtResourceId("-1");
      record.setPublishStatus(BaseConsts.STATE_SUCCESS);
      record.setStatusCd(BaseConsts.STATUS_CD_VALID);
      record.setCreatorId(SessionUtil.getOptionalUserId());
      record.setTenantId(request.getTenantId());
    }
    else {
      record = BeanUtil.copy(oldRecord, ResourcePublishRecordDTO.class);
    }
    record.setPublishParams(JsonUtil.toJsonString(request.getPublishChannelConfig()));
    DataDifferenceStarter.computeSave(oldRecord, record, false, request.getTenantId());
    return ResultVO.success();
  }

  @Transactional
  @Override
  public ResultVO<Void> unpublishResource(ResourceUnpublishRequest request) {
    if (request.getRecordIdList() == null || request.getRecordIdList().isEmpty()) {
      return ResultVO.fail("发布ID列表不能为空");
    }
    for (Long recordId : request.getRecordIdList()) {
      ResourcePublishRecordDTO record = resourcePublishRecordMapper.selectById(recordId);
      if (record == null) {
        logger.warn("发布记录不存在: recordId={}", recordId);
        continue;
      }
      // 验证租户权限
      if (!record.getTenantId().equals(request.getTenantId())) {
        continue;
      }
      // 停止连接
      if (StringUtils.isNotEmpty(record.getCallbackCode())) {
        sdkConnectionManager.broadcastStopConnection(record.getCallbackCode());
      }
      // 删除发布记录
      resourcePublishRecordMapper.deleteResourcePublishRecord(request.getTenantId(), recordId, SessionUtil.getLoginInfo().getUserId());
    }
    return ResultVO.success();
  }

  @Override
  public List<ResourcePublishRecordDTO> queryPublishRecordsByResourceId(Long tenantId, String resourceType, Long resourceId) {
    List<ResourcePublishRecordDTO> records = resourcePublishRecordMapper.selectPublishRecordsByResourceId(tenantId, resourceType, resourceId);
    records.forEach(this::processPublishChannelInfo);
    return records;
  }

  @Override
  public ResultVO<String> weWorkPublishResource(ResourcePublishRequest request) {
    if (request.getResourceType() == BeyondResourceBizTypeEnum.AGENT) {
      return weWorkPublishAgent(request);
    }
    return ResultVO.fail("不支持的资源类型：" + request.getResourceType().getDesc());
  }

  @Override
  public ResultVO<String> dingTalkPublishResource(ResourcePublishRequest request) {
    if (request.getResourceType() == BeyondResourceBizTypeEnum.AGENT) {
      return dingTalkPublishAgent(request);
    }
    return ResultVO.fail("不支持的资源类型：" + request.getResourceType().getDesc());
  }

  @Override
  public ResultVO<String> feishuPublishResource(ResourcePublishRequest request) {
    if (request.getResourceType() == BeyondResourceBizTypeEnum.AGENT) {
      return feishuPublishAgent(request);
    }
    return ResultVO.fail("不支持的资源类型：" + request.getResourceType().getDesc());
  }

  @Override
  public ResourcePublishRecordDTO getRecordByCallbackCode(String callbackCode) {
    return resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
  }

  @Override
  public PageInfo<ResourcePublishRecordDTO> queryResourcePublishRecordPage(ResourcePublishRecordQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    PageInfo<ResourcePublishRecordDTO> pageInfo = resourcePublishRecordMapper.selectResourcePublishRecordPage(params, rowBounds).toPageInfo();
    ListUtils.emptyIfNull(pageInfo.getList()).forEach(this::processPublishChannelInfo);
    return pageInfo;
  }

  @Override
  @Nullable
  public ResourcePublishRecordDTO getRecordByResourceAndChannel(Long tenantId, Long resourceId, String publishChannel) {
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByResourceAndChannel(resourceId, publishChannel, tenantId);
    if (record != null) {
      processPublishChannelInfo(record);
    }
    return record;
  }

  /**
   * 处理发布渠道信息
   *
   * @param dto 资源发布记录
   */
  private void processPublishChannelInfo(ResourcePublishRecordDTO dto) {
    if (StringUtils.isEmpty(dto.getPublishChannel())) {
      return;
    }
    // 设置发布渠道名
    PublishChannelEnum channelEnum = PublishChannelEnum.getByCode(dto.getPublishChannel());
    dto.setPublishChannelDesc(channelEnum != null ? channelEnum.getDesc() : dto.getPublishChannel());
    // 解析发布渠道配置
    if (StringUtils.isNotEmpty(dto.getPublishParams())) {
      PublishChannelDTO publishChannelConfig = JsonUtil.parseJsonRequired(dto.getPublishParams(), PublishChannelDTO.class);
      dto.setPublishChannelConfig(publishChannelConfig);
      // 处理Beyond渠道的特殊逻辑
      if (PublishChannelEnum.BEYOND == channelEnum && StringUtils.isNotEmpty(publishChannelConfig.getRemark())) {
        dto.setRemark(publishChannelConfig.getRemark());
      }
    }
    dto.setPublishParams(null);
    //构建callbackURL
    buildCallBackUrl(dto);
    //构建发布名称
    buildPublishName(dto);
  }

  private void buildPublishName(ResourcePublishRecordDTO dto) {
    PublishChannelEnum channelEnum = PublishChannelEnum.getByCode(dto.getPublishChannel());
    if (PublishChannelEnum.FEISHU == channelEnum) {
      Map<String, String> applicationInfo = sdkConnectionManager.getAdapter(dto.getCallbackCode()).getApplicationInfo();
      dto.setName(applicationInfo.get("name"));
      dto.setAvatarUrl(applicationInfo.get("avatarUrl"));
      return;
    }
    dto.setName(dto.getPublishChannelConfig().getName());
  }

  private void buildCallBackUrl(ResourcePublishRecordDTO dto) {
    PublishChannelEnum channelEnum = PublishChannelEnum.getByCode(dto.getPublishChannel());
    if (PublishChannelEnum.WEWORK == channelEnum) {
      dto.setCallbackUrl(StringUtils.defaultIfEmpty(BOTE_OPEN_API_URL, BOTE_API_URL) + "bote/publish/webhook/" + dto.getCallbackCode());
    }
    if (PublishChannelEnum.WECHAT == channelEnum) {
      dto.setCallbackUrl(StringUtils.defaultIfEmpty(BOTE_OPEN_API_URL, BOTE_API_URL) + "bote/platform/proxy/wechat/" + dto.getCallbackCode());
    }
  }

  /**
   * 发布MCP资源到百应平台
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  private ResultVO<String> beyondPublishMCP(ResourcePublishRequest request) {
    List<McpServerDTO> mcpServers = mcpServerManageMapper.getMcpServersByIds(request.getResourceIdList(), request.getTenantId());
    if (mcpServers.isEmpty()) {
      return ResultVO.fail("未找到任何MCP服务，ID列表：" + request.getResourceIdList());
    }
    if (mcpServers.size() != request.getResourceIdList().size()) {
      List<Long> foundIds = mcpServers.stream().map(McpServerDTO::getServerId).collect(Collectors.toList());
      List<Long> notFoundIds = request.getResourceIdList().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
      return ResultVO.fail("以下MCP服务不存在：" + notFoundIds);
    }
    BeyondResourcePublishRequest publishRequest = buildMcpPublishRequest(request, mcpServers);
    BeyondResponse<List<BeyondResourcePublishData>> response = beyondApiClient.publishResource(publishRequest);
    Assert.notNull(response, "百应接口返回结果为空");
    String responseMsg = recordBeyondResourcePublish(request, response);
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "发布MCP资源失败"));
    }
    return ResultVO.success(responseMsg);
  }

  /**
   * 发布AGENT资源到百应平台
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  private ResultVO<String> beyondPublishAgent(ResourcePublishRequest request) {
    List<BotSceneDTO> scenes = botSceneManageMapper.getScenesByIds(request.getTenantId(), request.getResourceIdList());
    if (scenes.isEmpty()) {
      return ResultVO.fail("未找到任何已启用智能体，ID列表：" + request.getResourceIdList());
    }
    if (scenes.size() != request.getResourceIdList().size()) {
      List<Long> foundIds = scenes.stream().map(BotSceneDTO::getSceneId).collect(Collectors.toList());
      List<Long> notFoundIds = request.getResourceIdList().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
      return ResultVO.fail("以下智能体不存在或未启用：" + notFoundIds);
    }
    BeyondResourcePublishRequest publishRequest = buildAgentPublishRequest(request, scenes);
    BeyondResponse<List<BeyondResourcePublishData>> response = beyondApiClient.publishResource(publishRequest);
    Assert.notNull(response, "百应接口返回结果为空");
    String responseMsg = recordBeyondResourcePublish(request, response);
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "发布AGENT资源失败"));
    }
    return ResultVO.success(responseMsg);
  }

  /**
   * 发布BOT资源到百应平台
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  private ResultVO<?> beyondPublishBot(ResourcePublishRequest request) {
    BotQueryParams botQueryParams = new BotQueryParams();
    botQueryParams.setBotIds(request.getResourceIdList());
    botQueryParams.setTenantId(request.getTenantId());
    botQueryParams.setBotStatus(BaseConsts.BOT_STATUS_PUBLISH);
    List<BotDTO> bots = botQueryMapper.selectBotList(botQueryParams);
    if (bots.isEmpty()) {
      return ResultVO.fail("未找到任何已启用的应用，ID列表：" + request.getResourceIdList());
    }
    if (bots.size() != request.getResourceIdList().size()) {
      List<Long> foundIds = bots.stream().map(BotDTO::getBotId).toList();
      List<Long> notFoundIds = request.getResourceIdList().stream().filter(id -> !foundIds.contains(id)).toList();
      return ResultVO.fail("以下引用不存在或未启用：" + notFoundIds);
    }
    BeyondResourcePublishRequest publishRequest = buildBotPublishRequest(request, bots);
    BeyondResponse<List<BeyondResourcePublishData>> response = beyondApiClient.publishEmployee(publishRequest);
    Assert.notNull(response, "百应接口返回结果为空");
    recordBeyondResourcePublish(request, response);
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "发布BOT资源失败"));
    }
    return ResultVO.success(response.getData().getFirst());
  }

  /**
   * 发布TOOL资源到百应平台
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  private ResultVO<String> beyondPublishTool(ResourcePublishRequest request) {
    List<SkillFlowWithParamDTO> skillFlows = skillFlowManageMapper.getFlowsByIds(request.getTenantId(), request.getResourceIdList());
    if (skillFlows.isEmpty()) {
      return ResultVO.fail("未找到任何工作流，ID列表：" + request.getResourceIdList());
    }
    if (skillFlows.size() != request.getResourceIdList().size()) {
      List<Long> foundIds = skillFlows.stream().map(SkillFlowWithParamDTO::getFlowId).collect(Collectors.toList());
      List<Long> notFoundIds = request.getResourceIdList().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
      return ResultVO.fail("以下工作流不存在：" + notFoundIds);
    }
    for (SkillFlowWithParamDTO skillFlow : skillFlows) {
      skillFlow.parseParams();
    }
    BeyondResourcePublishRequest publishRequest = buildToolPublishRequest(request, skillFlows);
    BeyondResponse<List<BeyondResourcePublishData>> response = beyondApiClient.publishResource(publishRequest);
    Assert.notNull(response, "百应接口返回结果为空");
    String responseMsg = recordBeyondResourcePublish(request, response);
    if (!response.isSuccess()) {
      return ResultVO.fail(StringUtils.defaultIfEmpty(response.getMsg(), "发布TOOL资源失败"));
    }
    return ResultVO.success(responseMsg);
  }

  /**
   * 构建MCP发布请求
   *
   * @param request 资源发布请求
   * @param mcpServers MCP服务信息列表
   * @return 百应资源发布请求
   */
  private BeyondResourcePublishRequest buildMcpPublishRequest(ResourcePublishRequest request, List<McpServerDTO> mcpServers) {
    List<ResourceDTO> resources = new ArrayList<>();
    for (McpServerDTO mcpServer : mcpServers) {
      // 构建基础资源信息
      ResourceDTO resource = buildBaseResource(request, mcpServer.getServerId(), mcpServer.getServerName(), mcpServer.getServerDesc());

      // 使用BeyondParamHelper构建MCP参数
      McpParamDTO mcpParam = BeyondParamHelper.buildMcpParam(mcpServer);
      resource.setParam(mcpParam);
      resources.add(resource);
    }
    return buildPublishRequest(request, resources);
  }

  /**
   * 构建AGENT发布请求
   *
   * @param request 资源发布请求
   * @param scenes 场景信息列表
   * @return 百应资源发布请求
   */
  private BeyondResourcePublishRequest buildAgentPublishRequest(ResourcePublishRequest request, List<BotSceneDTO> scenes) {
    List<ResourceDTO> resources = new ArrayList<>();
    for (BotSceneDTO scene : scenes) {
      // 构建基础资源信息
      String resourceDesc = scene.getSceneDesc() != null ? scene.getSceneDesc() : scene.getSceneName();
      ResourceDTO resource = buildBaseResource(request, scene.getSceneId(), scene.getSceneName(), resourceDesc);

      // 使用BeyondParamHelper构建AGENT参数
      AgentParamDTO agentParam = BeyondParamHelper.buildAgentParam(scene);
      resource.setParam(agentParam);
      resources.add(resource);
    }
    return buildPublishRequest(request, resources);
  }

  /**
   * 构建BOT发布请求
   *
   * @param request 资源发布请求
   * @param bots 场景信息列表
   * @return 百应资源发布请求
   */
  private BeyondResourcePublishRequest buildBotPublishRequest(ResourcePublishRequest request, List<BotDTO> bots) {
    List<ResourceDTO> resources = new ArrayList<>();
    PublishChannelDTO publishChannelConfig = request.getPublishChannelConfig();
    for (BotDTO bot : bots) {
      // 构建基础资源信息
      String resourceDesc = bot.getBotUse() != null ? bot.getBotUse() : bot.getBotName();
      ResourceDTO resource = buildBaseResource(request, bot.getBotId(), bot.getBotName(), resourceDesc);

      // 设置数字员工特有的字段
      resource.setResourceBizType("DIG_EMPLOYEE");
      resource.setResourceType("COMBIN");
      resource.setSystemCode("BOT");

      // 使用BeyondParamHelper构建BOT参数
      BotParamDTO botParam = BeyondParamHelper.buildBotParam(bot);
      // 设置能力描述相关字段
      if (publishChannelConfig != null) {
        botParam.setAbility(publishChannelConfig.getAbility());
        botParam.setConstraints(publishChannelConfig.getConstraints());
        botParam.setFaqs(publishChannelConfig.getFaqs());
      }
      resource.setParam(botParam);
      resources.add(resource);
    }
    return buildPublishRequest(request, resources);
  }

  /**
   * 构建TOOL发布请求
   *
   * @param request 资源发布请求
   * @param skillFlows 技能流信息列表
   * @return 百应资源发布请求
   */
  private BeyondResourcePublishRequest buildToolPublishRequest(ResourcePublishRequest request, List<SkillFlowWithParamDTO> skillFlows) {
    List<ResourceDTO> resources = new ArrayList<>();
    for (SkillFlowWithParamDTO skillFlow : skillFlows) {
      // 构建基础资源信息
      String resourceDesc = StringUtils.defaultIfEmpty(skillFlow.getFlowExplanation(), skillFlow.getFlowName());
      ResourceDTO resource = buildBaseResource(request, skillFlow.getFlowId(), skillFlow.getFlowName(), resourceDesc);

      // 转换工具参数
      ToolDTO toolParam = BeyondParamHelper.convertFromFlow(skillFlow);
      resource.setParam(toolParam);
      resources.add(resource);
    }
    return buildPublishRequest(request, resources);
  }

  /**
   * 构建基础资源信息
   *
   * @param request 资源发布请求
   * @param resourceId 资源ID
   * @param resourceName 资源名称
   * @param resourceDesc 资源描述
   * @return 基础资源对象
   */
  private ResourceDTO buildBaseResource(ResourcePublishRequest request, Long resourceId, String resourceName, String resourceDesc) {
    ResourceDTO resource = new ResourceDTO();
    resource.setSystemCode(PublishResourceConsts.SYSTEM_CODE_BYAI);
    resource.setResourceSourcePkId(resourceId);
    resource.setResourceBizType(request.getResourceType().getCode());
    resource.setResourceName(resourceName);
    resource.setResourceDesc(StringUtils.isNotBlank(resourceDesc) ? resourceDesc : resourceName);
    resource.setTags(JsonUtil.toJsonString(PublishResourceConsts.DEFAULT_TAGS));
    resource.setHostType(PublishResourceConsts.HOST_TYPE_HOSTED);
    return resource;
  }

  /**
   * 构建发布请求
   *
   * @param request 资源发布请求
   * @param resources 资源列表
   * @return 百应资源发布请求
   */
  private BeyondResourcePublishRequest buildPublishRequest(ResourcePublishRequest request, List<ResourceDTO> resources) {
    BeyondResourcePublishRequest publishRequest = new BeyondResourcePublishRequest();
    publishRequest.setPublishChannels(Collections.singletonList(request.getPublishChannelConfig()));
    publishRequest.setResources(resources);
    publishRequest.setPublishType(request.getPublishType());
    return publishRequest;
  }

  /**
   * 记录百应资源发布结果
   *
   * @param request 资源发布请求
   * @param response 百应平台返回
   * @return 汇总结果
   */
  private String recordBeyondResourcePublish(ResourcePublishRequest request,
                                             BeyondResponse<List<BeyondResourcePublishData>> response) {
    List<BeyondResourcePublishData> dataList = response.getData() == null ? new ArrayList<>() : response.getData();
    for (BeyondResourcePublishData data : dataList) {
      if (data == null) {
        continue;
      }
      Long resourceId = data.getResourceSourcePkId();
      if (resourceId == null) {
        continue;
      }
      ResourcePublishRecordDTO oldRecord = resourcePublishRecordMapper.getRecordByResourceAndChannel(
        resourceId, request.getPublishChannel().getCode(), request.getTenantId());
      ResourcePublishRecordDTO newRecord = buildRecordFor(request, data);
      if (oldRecord == null) {
        newRecord.setRecordId(IDUtils.nextId());
      }
      DataDifferenceStarter.computeSave(oldRecord, newRecord, false, request.getTenantId());
    }
    return StringUtils.defaultString(response.getMsg());
  }

  /**
   * 构建单条发布记录。
   *
   * @param request 请求
   * @return 发布记录
   */
  private ResourcePublishRecordDTO buildRecordFor(ResourcePublishRequest request, BeyondResourcePublishData data) {
    ResourcePublishRecordDTO record = new ResourcePublishRecordDTO();
    record.setPublishChannel(request.getPublishChannel().getCode());
    record.setResourceType(request.getResourceType().getCode());
    record.setResourceId(data.getResourceSourcePkId());
    record.setExtResourceId(data.getResourceId() != null ? String.valueOf(data.getResourceId()) : "-1");
    record.setPublishStatus(data.isSuccess() ? BaseConsts.STATE_SUCCESS : BaseConsts.STATE_FAIL);
    record.setPublishParams(JsonUtil.toJsonString(request.getPublishChannelConfig()));
    record.setPublishMsg(JsonUtil.toJsonString(data));
    record.setStatusCd(BaseConsts.STATUS_CD_VALID);
    record.setCreatorId(SessionUtil.getOptionalUserId());
    record.setUpdatorId(SessionUtil.getOptionalUserId());
    record.setCreatedTime(new Date());
    record.setTenantId(request.getTenantId());
    return record;
  }

  /**
   * 发布AGENT资源到微信公众号
   *
   * @param request 资源发布请求
   * @return 发布结果
   */
  private ResultVO<String> wechatPublishAgent(ResourcePublishRequest request) {
    List<BotSceneDTO> scenes = botSceneManageMapper.getScenesByIds(request.getTenantId(), request.getResourceIdList());
    if (scenes.isEmpty()) {
      return ResultVO.fail("未找到任何已上架智能体，ID列表：" + request.getResourceIdList());
    }
    if (StringUtils.isBlank(request.getPublishChannelConfig().getToken())) {
      request.getPublishChannelConfig().setToken(RandomStringUtils.insecure().nextAlphanumeric(32));
    }
    if (StringUtils.isBlank(request.getPublishChannelConfig().getAesKey())) {
      request.getPublishChannelConfig().setAesKey(RandomStringUtils.insecure().nextAlphanumeric(43));
    }
    if (scenes.size() != request.getResourceIdList().size()) {
      List<Long> foundIds = scenes.stream().map(BotSceneDTO::getSceneId).collect(Collectors.toList());
      List<Long> notFoundIds = request.getResourceIdList().stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
      return ResultVO.fail("以下智能体不存在或未上架：" + notFoundIds);
    }
    String callbackCode = generateCallbackCode();
    resourcePublish(request, callbackCode);
    // 获取发布记录并构建回调URL
    ResourcePublishRecordDTO record = new ResourcePublishRecordDTO();
    record.setCallbackCode(callbackCode);
    record.setPublishChannel(request.getPublishChannel().getCode());
    buildCallBackUrl(record);
    Map<String, Object> result = new HashMap<>();
    result.put("callbackUrl", record.getCallbackUrl());
    Map<String, String> config = new HashMap<>();
    PublishChannelDTO channelConfig = request.getPublishChannelConfig();
    config.put("appId", channelConfig.getAppId());
    config.put("secret", channelConfig.getSecret());
    config.put("token", channelConfig.getToken());
    config.put("aesKey", channelConfig.getAesKey());
    result.put("config", config);

    return ResultVO.success(JsonUtil.toJsonString(result));
  }

  /**
   * 企业微信发布智能体
   */
  private ResultVO<String> weWorkPublishAgent(ResourcePublishRequest request) {
    // 1. 验证企业微信配置
    validateWeWorkConfig(request.getPublishChannelConfig());
    // 2. 生成回调地址 与token aeskey
    String callbackCode = generateCallbackCode();
    if (StringUtils.isBlank(request.getPublishChannelConfig().getToken())) {
      request.getPublishChannelConfig().setToken(RandomStringUtils.insecure().nextAlphanumeric(32));
    }
    if (StringUtils.isBlank(request.getPublishChannelConfig().getAesKey())) {
      request.getPublishChannelConfig().setAesKey(RandomStringUtils.insecure().nextAlphanumeric(43));
    }
    // 3. 保存发布记录
    resourcePublish(request, callbackCode);
    // 获取发布记录并构建回调URL
    ResourcePublishRecordDTO record = new ResourcePublishRecordDTO();
    record.setCallbackCode(callbackCode);
    record.setPublishChannel(request.getPublishChannel().getCode());
    buildCallBackUrl(record);
    Map<String, Object> result = new HashMap<>();
    result.put("callbackUrl", record.getCallbackUrl());
    Map<String, String> config = new HashMap<>();
    PublishChannelDTO channelConfig = request.getPublishChannelConfig();
    config.put("appId", channelConfig.getAppId());
    config.put("secret", channelConfig.getSecret());
    config.put("token", channelConfig.getToken());
    config.put("aesKey", channelConfig.getAesKey());
    result.put("config", config);

    return ResultVO.success(JsonUtil.toJsonString(result));
  }

  /**
   * 钉钉机器人发布智能体
   */
  private ResultVO<String> dingTalkPublishAgent(ResourcePublishRequest request) {

    // 1. 验证钉钉配置
    validateDingTalkConfig(request.getPublishChannelConfig());
    // 2. 生成回调地址
    String callbackCode = generateCallbackCode();
    // 3. 保存发布记录
    resourcePublish(request, callbackCode);
    // 4.启动长连接
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record != null) {
      sdkConnectionManager.startConnection(record);
    }
    return ResultVO.success("钉钉机器人发布成功");
  }

  /**
   * 飞书机器人发布智能体
   */
  private ResultVO<String> feishuPublishAgent(ResourcePublishRequest request) {
    // 1. 验证飞书配置
    validateFeishuConfig(request.getPublishChannelConfig());
    // 2. 生成回调地址
    String callbackCode = generateCallbackCode();
    // 3. 保存发布记录
    resourcePublish(request, callbackCode);
    // 4. 启动长连接
    ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
    if (record != null) {
      sdkConnectionManager.startConnection(record);
    }
    return ResultVO.success("飞书机器人发布成功");
  }

  /**
   * 生成回调编码
   */
  private String generateCallbackCode() {
    return java.util.UUID.randomUUID().toString().replace("-", "");
  }


  /**
   * 验证企业微信配置
   */
  private void validateWeWorkConfig(PublishChannelDTO config) {
    Assert.notNull(config, "企业微信配置不能为空");
    // 验证必要字段
    String appId = config.getAppId();
    String secret = config.getSecret();
    Assert.hasText(appId, "企业ID不能为空");
    Assert.hasText(secret, "密钥不能为空");

    // Token和AES密钥可以为空，会自动生成
    logger.info("企业微信配置验证通过，appId: {}", appId);
  }


  /**
   * 验证钉钉配置
   */
  private void validateDingTalkConfig(PublishChannelDTO config) {
    Assert.notNull(config, "钉钉配置不能为空");
    // 验证必要字段 - 使用PublishChannelDTO的基本字段
    String appId = config.getAppId();
    String secret = config.getSecret();
    String robotCode = config.getRobotCode();
    Assert.hasText(appId, "应用AppKey不能为空");
    Assert.hasText(secret, "应用AppSecret不能为空");
    Assert.hasText(robotCode, "机器人编码不能为空");
  }

  /**
   * 验证飞书配置
   */
  private void validateFeishuConfig(PublishChannelDTO config) {
    Assert.notNull(config, "飞书配置不能为空");
    // 验证必要字段 - 使用PublishChannelDTO的基本字段
    String appId = config.getAppId();
    String secret = config.getSecret();
    String verificationToken = config.getVerificationToken();
    Assert.hasText(appId, "应用ID不能为空");
    Assert.hasText(secret, "应用密钥不能为空");
    Assert.hasText(verificationToken, "verificationToken不能为空");
  }

  private void resourcePublish(ResourcePublishRequest request, String callbackCode) {
    Assert.isTrue(request.getResourceIdList().size() == 1, "当前渠道不支持批量发布");
    for (Long resourceId : request.getResourceIdList()) {
      ResourcePublishRecordDTO oldRecord = null;
      if (request.getRecordId() != null) {
        oldRecord = resourcePublishRecordMapper.selectById(request.getRecordId());
      }
      ResourcePublishRecordDTO record = new ResourcePublishRecordDTO();
      record.setPublishChannel(request.getPublishChannel().getCode());
      record.setResourceType(request.getResourceType().getCode());
      record.setResourceId(resourceId);
      record.setExtResourceId(String.valueOf(IDUtils.nextId()));
      record.setPublishStatus(BaseConsts.STATE_SUCCESS);
      record.setPublishParams(JsonUtil.toJsonString(request.getPublishChannelConfig()));
      record.setCallbackCode(callbackCode);
      record.setStatusCd(BaseConsts.STATUS_CD_VALID);
      record.setCreatorId(SessionUtil.getOptionalUserId());
      record.setUpdatorId(SessionUtil.getOptionalUserId());
      record.setCreatedTime(new Date());
      record.setTenantId(request.getTenantId());
      if (oldRecord == null) {
        record.setRecordId(IDUtils.nextId());
      }
      else {
        sdkConnectionManager.broadcastStopConnection(oldRecord.getCallbackCode());
      }
      DataDifferenceStarter.computeSave(oldRecord, record, false, request.getTenantId());
    }
  }
}
