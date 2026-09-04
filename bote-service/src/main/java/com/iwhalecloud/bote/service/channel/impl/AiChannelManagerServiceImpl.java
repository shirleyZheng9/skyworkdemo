package com.iwhalecloud.bote.service.channel.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.cache.AttrSpecCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.PublishResourceConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BeyondResourceBizTypeEnum;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotDTO;
import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.mapper.channel.AiChannelManagerMapper;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.mapper.workspace.WorkspaceManageMapper;
import com.iwhalecloud.bote.service.bot.IBotQueryService;
import com.iwhalecloud.bote.service.channel.IAiChannelManagerService;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 渠道管理服务实现
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
@Service
@RequiredArgsConstructor
public class AiChannelManagerServiceImpl implements IAiChannelManagerService {

  private final AiChannelManagerMapper aiChannelManagerMapper;
  private final AttrSpecCache attrSpecCache;
  private final SdkConnectionManager sdkConnectionManager;
  private final ResourcePublishRecordMapper resourcePublishRecordMapper;
  private final WorkspaceManageMapper workspaceManageMapper;
  private final IBotQueryService botQueryService;

  /** 渠道类型参数 Map */
  private static final Map<String, String> AI_CHANNEL_PARAM_MAP = Map.of(
    "DingTalk", "AI_CHANNEL_DINGTALK",
    "Feishu", "AI_CHANNEL_FEISHU",
    "QQ", "AI_CHANNEL_QQ",
    "WECLAWBOT", "AI_CHANNEL_WECLAWBOT"
  );

  @Override
  @Transactional
  public ResultVO<AiChannelDTO> saveAiChannel(AiChannelDTO dto) {
    if (CollectionUtils.isNotEmpty(dto.getChannelObjList())) {
      dto.setChannelJson(JsonUtil.toJsonString(dto.getChannelObjList()));
    }
    AiChannelDTO old = dto.getId() == null ? null : aiChannelManagerMapper.getAiChannel(dto.getId());
    DataDifference<AiChannelDTO> difference = DataDifferenceStarter.computeSave(old, dto, false, dto.getSpaceId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    // 启用时校验：同一渠道凭据不允许跨 BoteClaw 重复绑定，避免通讯混乱
    if (BaseConsts.TRUE.equals(dto.getIsEnabled())) {
      String conflictMsg = getChannelBindConflictMsg(dto);
      if (StringUtils.isNotEmpty(conflictMsg)) {
        throw new BssException(conflictMsg);
      }
    }
    // 记录渠道发布和创建连接
    recordAndConnectChannel(dto);
    return ResultVO.success(difference.getToSaveData());
  }

  /**
   * 启用时校验：同一发布渠道凭据是否已绑定到其他 BoteClaw
   *
   * <p>当前 bt_resource_publish_record 中活跃连接仅覆盖 FEISHU / DINGTALK，因此此处校验也限定在这两类。</p>
   */
  private String getChannelBindConflictMsg(AiChannelDTO channel) {
    String typeUpper = StringUtils.upperCase(channel.getChannelType());
    if (!PublishChannelEnum.DINGTALK.getCode().equals(typeUpper) && !PublishChannelEnum.FEISHU.getCode().equals(typeUpper)) {
      return null;
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<ResourcePublishRecordDTO> activeRecords = CollectionUtils.emptyIfNull(resourcePublishRecordMapper.getActivePublishRecords()).stream()
      .filter(p -> PublishResourceConsts.BOTECLAW_EXT_RESOURCE_ID.equals(p.getExtResourceId()))
      .filter(p -> channel.getChannelType().toUpperCase().equals(p.getPublishChannel()))
      // 忽略当前的
      .filter(p -> !(Objects.equals(p.getResourceId(), channel.getBotId()) && Objects.equals(p.getTenantId(), channel.getSpaceId()) && Objects.equals(
        p.getCreatorId(), userId))).toList();
    if (CollectionUtils.isEmpty(activeRecords)) {
      return null;
    }
    // 获取不同渠道的 appId
    String attrValueId = BaseConsts.CHANNEL_TYPE_DINGTALK.equals(channel.getChannelType()) ? "1348173681959890944" : "1348174020285034496";
    String appId = MapUtils.getString(IterableUtils.find(channel.getChannelObjList(), p -> attrValueId.equals(MapUtils.getString(p, "attrValueId"))),
      "attrValue");
    if (StringUtils.isBlank(appId)) {
      return null;
    }
    for (ResourcePublishRecordDTO resource : activeRecords) {
      Map<String, String> map = JsonUtil.parseJson(resource.getPublishParams(), new TypeReference<>() {
      });
      if (appId.equals(MapUtils.getString(map, "appId"))) {
        String spaceName = workspaceManageMapper.selectWorkspaceById(resource.getTenantId()).getSpaceName();
        String boteClawName = getBoteClawName(resource);
        return String.format("该渠道已绑定到其他 BoteClaw（%s）（%s），请先取消绑定后再启用。", boteClawName, spaceName);
      }
    }
    return null;
  }

  /**
   * 获取 BoteClaw 名称
   */
  private String getBoteClawName(ResourcePublishRecordDTO resource) {
    SimpleBotDTO bot = botQueryService.getBotById(resource.getResourceId(), resource.getTenantId());
    return bot != null && StringUtils.isNotBlank(bot.getBotName()) ? bot.getBotName() : "";
  }

  /**
   * 处理钉钉渠道
   */
  private void recordAndConnectChannel(AiChannelDTO dto) {
    if (BaseConsts.TRUE.equals(dto.getIsEnabled())) {
      // 生成 callbackCode
      String callbackCode = UUID.randomUUID().toString().replace("-", "");
      // 保存发布记录
      resourcePublish(dto, callbackCode);
      // 启动长连接
      ResourcePublishRecordDTO record = resourcePublishRecordMapper.getRecordByCallbackCode(callbackCode);
      if (record != null) {
        sdkConnectionManager.startConnection(record);
      }
    }
    else {
      // 查询发布记录
      Long userId = SessionUtil.getLoginInfo().getUserId();
      ResourcePublishRecordDTO record = resourcePublishRecordMapper.selectRecordForAiAgent(dto.getChannelType().toUpperCase(), dto.getSpaceId(),
        dto.getBotId(), userId);
      if (record == null) {
        return;
      }
      // 停止连接
      if (StringUtils.isNotEmpty(record.getCallbackCode())) {
        sdkConnectionManager.broadcastStopConnection(record.getCallbackCode());
      }
      // 删除发布记录
      resourcePublishRecordMapper.deleteResourcePublishRecord(dto.getSpaceId(), record.getRecordId(), userId);
    }
  }

  /**
   * 资源发布
   */
  private void resourcePublish(AiChannelDTO dto, String callbackCode) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    ResourcePublishRecordDTO record = new ResourcePublishRecordDTO();
    record.setPublishChannel(dto.getChannelType().toUpperCase());
    record.setResourceType(BeyondResourceBizTypeEnum.AGENT.getCode());
    record.setResourceId(dto.getBotId());
    record.setExtResourceId("-2");
    record.setPublishStatus(BaseConsts.STATE_SUCCESS);
    record.setPublishParams(buildPublishParams(dto));
    record.setCallbackCode(callbackCode);
    record.setCreatorId(userId);
    record.setStatusCd(BaseConsts.STATUS_CD_VALID);
    record.setTenantId(dto.getSpaceId());
    ResourcePublishRecordDTO oldRecord = resourcePublishRecordMapper.selectRecordForAiAgent(dto.getChannelType().toUpperCase(), dto.getSpaceId(),
      dto.getBotId(), userId);
    if (oldRecord == null) {
      record.setRecordId(IDUtils.nextId());
    }
    else if (BaseConsts.STATE_SUCCESS.equals(oldRecord.getPublishStatus())) {
      sdkConnectionManager.broadcastStopConnection(oldRecord.getCallbackCode());
    }
    DataDifferenceStarter.computeSave(oldRecord, record, false, dto.getSpaceId());
  }

  /**
   * 构建发布参数
   */
  private String buildPublishParams(AiChannelDTO dto) {
    Map<Object, Object> paramMap = new HashMap<>();
    for (Map<String, Object> map : CollectionUtils.emptyIfNull(dto.getChannelObjList())) {
      paramMap.put(map.get("attrValueName"), map.get("attrValue"));
    }
    Map<Object, Object> resultMap = new HashMap<>();
    if (PublishChannelEnum.DINGTALK.getCode().equalsIgnoreCase(dto.getChannelType())) {
      resultMap.put("appId", paramMap.get("Client ID"));
      resultMap.put("secret", paramMap.get("Client Secret"));
      resultMap.put("robotCode", paramMap.get("RobotCode"));
    }
    else if (PublishChannelEnum.FEISHU.getCode().equalsIgnoreCase(dto.getChannelType())) {
      resultMap.put("appId", paramMap.get("App ID"));
      resultMap.put("secret", paramMap.get("App Secret"));
      resultMap.put("verificationToken", paramMap.get("Verification Token"));
    }
    else if (PublishChannelEnum.WECLAWBOT.getCode().equalsIgnoreCase(dto.getChannelType())) {
      // 个人微信：baseUrl 可选，token 为空则由扫码登录获取后回填
      resultMap.put("baseUrl", paramMap.get("Base URL"));
      resultMap.put("botToken", paramMap.get("Bot Token"));
    }
    return JsonUtil.toJsonString(resultMap);
  }

  @Override
  public List<AiChannelDTO> queryAiChannelList(Long spaceId, Long botId) {
    // 获取用户配置的渠道列表
    List<AiChannelDTO> aiChannelList = aiChannelManagerMapper.selectAiChannelList(spaceId, botId, SessionUtil.getLoginInfo().getUserId());
    // 获取渠道类型列表
    List<SimpleAttrDTO> channelTypeList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, "AI_CHANNEL_TYPE");
    if (CollectionUtils.isEmpty(channelTypeList)) {
      return aiChannelList;
    }
    // 检查每个渠道类型配置
    for (SimpleAttrDTO attrDTO : channelTypeList) {
      String channelType = attrDTO.getAttrValue();
      // 检查是否包含该渠道配置
      boolean containsChannel = aiChannelList.stream().anyMatch(aiChannelDTO -> channelType.equals(aiChannelDTO.getChannelType()));
      // 如果不存在则添加默认配置
      if (!containsChannel) {
        AiChannelDTO aiChannel = new AiChannelDTO();
        aiChannel.setChannelType(channelType);
        aiChannel.setChannelName(attrDTO.getAttrValueName());
        aiChannel.setIsEnabled(BaseConsts.FALSE);
        // 设置渠道属性
        List<SimpleAttrDTO> simpleAttrDTOS = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, AI_CHANNEL_PARAM_MAP.get(channelType));
        if (CollectionUtils.isNotEmpty(simpleAttrDTOS)) {
          simpleAttrDTOS.forEach(attr -> attr.setAttrValue(null));
          aiChannel.setChannelJson(JsonUtil.toJsonString(simpleAttrDTOS));
        }
        aiChannelList.add(aiChannel);
      }
    }
    // 设置渠道帮助文档
    List<SimpleAttrDTO> channeHelpUrlList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, "PUBLISH_EXTERNAL_PLATFORM_URL");
    aiChannelList.forEach(aiChannel -> {
      String channelDocUrl = CollectionUtils.emptyIfNull(channeHelpUrlList).stream()
        .filter(attr -> aiChannel.getChannelType().equalsIgnoreCase(attr.getAttrValueName()))
        .map(SimpleAttrDTO::getAttrValue).findFirst().orElse(null);
      aiChannel.setChannelDocUrl(channelDocUrl);
      if (StringUtils.isNotBlank(aiChannel.getChannelJson())) {
        aiChannel.setChannelObjList(JsonUtil.parseJson(aiChannel.getChannelJson(), new TypeReference<>() {
        }));
        aiChannel.setChannelJson(null);
      }
    });
    return aiChannelList;
  }

  @Override
  public List<AiChannelDTO> queryEnabledAiChannelList(Long spaceId, Long botId) {
    // 获取启用的渠道列表
    List<AiChannelDTO> enabledAiChannelList = aiChannelManagerMapper.selectEnabledAiChannelList(spaceId, botId, SessionUtil.getLoginInfo().getUserId());
    if (CollectionUtils.isEmpty(enabledAiChannelList)) {
      return enabledAiChannelList;
    }
    // 获取渠道类型列表
    List<SimpleAttrDTO> channelTypeList = attrSpecCache.get(BaseConsts.PLATFORM_TENANT_ID, "AI_CHANNEL_TYPE");
    if (CollectionUtils.isEmpty(channelTypeList)) {
      return enabledAiChannelList;
    }
    // 填充渠道类型名称
    enabledAiChannelList.forEach(aiChannel -> {
      String channelType = aiChannel.getChannelType();
      channelTypeList.stream().filter(attr -> channelType.equals(attr.getAttrValue())).findFirst()
        .ifPresent(channelTypeDTO -> aiChannel.setChannelName(channelTypeDTO.getAttrValueName()));
    });
    return enabledAiChannelList;
  }

}
