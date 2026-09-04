package com.iwhalecloud.bote.service.bot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneRelDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotQueryParams;
import com.iwhalecloud.bote.mapper.bot.BotQueryMapper;
import com.iwhalecloud.bote.mapper.bot.BotRelaManageMapper;
import com.iwhalecloud.bote.mapper.bot.BotUserExperienceManageMapper;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.service.bot.IBotRelaManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 机器人关联管理服务
 *
 * @author chen.linfa
 * @since 2025-04-24
 */
@Service
@RequiredArgsConstructor
public class BotRelaManageServiceImpl implements IBotRelaManageService {
  private final BotRelaManageMapper botRelaManageMapper;
  private final BotUserExperienceManageMapper experienceManageMapper;
  private final BotQueryMapper botQueryMapper;
  private final SceneQueryMapper sceneQueryMapper;
  private final IResourceElementService resourceElementService;

  @Override
  public PageInfo<BotSceneRelDTO> queryBotSceneRelPage(BotQueryParams query) {
    // noinspection resource
    return botRelaManageMapper.selectBotSceneRelPage(query, query.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<BotSceneRelDTO> queryBotSceneRelList(BotQueryParams query) {
    return botRelaManageMapper.selectBotSceneRels(query);
  }

  @Override
  @Transactional
  public ResultVO<Void> addBotSceneRel(Long tenantId, Long botId, List<Long> sceneIds) {
    // 排除掉已经关联的智能体
    List<Long> existingSceneIds = botRelaManageMapper.selectRelSceneIds(tenantId, botId);
    List<Long> addedSceneIds;
    if (!existingSceneIds.isEmpty()) {
      addedSceneIds = sceneIds.stream().filter(sceneId -> !existingSceneIds.contains(sceneId)).toList();
      if (addedSceneIds.isEmpty()) {
        return ResultVO.success();
      }
    }
    else {
      addedSceneIds = sceneIds;
    }

    // 查询智能体
    List<SimpleBotSceneDTO> scenes = sceneQueryMapper.selectSceneTypeBySceneIds(tenantId, addedSceneIds);
    if (scenes.size() != addedSceneIds.size()) {
      String missingIds = addedSceneIds.stream()
        .filter(id -> scenes.stream().noneMatch(scene -> scene.getSceneId().equals(id)))
        .map(Object::toString)
        .collect(Collectors.joining(","));
      return ResultVO.fail("智能体不存在: sceneId=" + missingIds);
    }

    // 普通智能体
    List<Long> normalSceneIds = new ArrayList<>();
    // A2A 类型的智能体（虚拟智能体，实际使用 bt_a2a_agent）
    List<Long> a2aSceneIds = new ArrayList<>();
    for (SimpleBotSceneDTO scene : scenes) {
      if (SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType())) {
        a2aSceneIds.add(scene.getSceneId());
      }
      else {
        normalSceneIds.add(scene.getSceneId());
      }
    }

    List<BotSceneRelDTO> list = new ArrayList<>(addedSceneIds.size());
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (Long sceneId : addedSceneIds) {
      BotSceneRelDTO rel = new BotSceneRelDTO();
      rel.setRelId(Sequences.BOT_SCENE_REL_ID.next());
      rel.setBotId(botId);
      rel.setSceneId(sceneId);
      rel.setTenantId(tenantId);
      rel.setCreatorId(userId);
      rel.setUpdatorId(userId);
      rel.setStatusCd(BaseConsts.STATUS_CD_VALID);
      list.add(rel);
    }
    botRelaManageMapper.batchInsertBotSceneRel(list);
    resourceElementService.batchAdd(tenantId, botId, DataSyncCodeEnum.BOT.getCode(), normalSceneIds, DataSyncCodeEnum.SCENE.getCode());
    resourceElementService.batchAdd(tenantId, botId, DataSyncCodeEnum.BOT.getCode(), a2aSceneIds, DataSyncCodeEnum.A2A_AGENT.getCode());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> removeBotSceneRel(Long tenantId, Long botId, Long sceneId) {
    List<String> titles = experienceManageMapper.selectBotUserExperienceBySceneId(tenantId, sceneId, botId);
    if (CollectionUtils.isNotEmpty(titles)) {
      return ResultVO.fail("该智能体下存在指令，请先删除指令：" + titles);
    }
    if (containsSceneInRecommendAgents(tenantId, botId, sceneId)) {
      return ResultVO.fail("该智能体存在主题设置的推荐智能体中，请先将该智能体从推荐智能体中移除");
    }
    botRelaManageMapper.deleteBotSceneRel(tenantId, botId, sceneId, SessionUtil.getLoginInfo().getUserId());
    SimpleBotSceneDTO scene = sceneQueryMapper.selectSceneTypeAndBotIdBySceneId(tenantId, sceneId);
    if (scene != null && SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType())) {
      resourceElementService.remove(tenantId, botId, sceneId, DataSyncCodeEnum.A2A_AGENT.getCode());
    }
    else {
      resourceElementService.remove(tenantId, botId, sceneId, DataSyncCodeEnum.SCENE.getCode());
    }
    return ResultVO.success();
  }

  /**
   * 判断 pageSettingInfo 中推荐智能体是否包含指定 sceneId
   */
  private boolean containsSceneInRecommendAgents(Long tenantId, Long botId, Long sceneId) {
    String pageSettingInfo = botQueryMapper.selectPageSettingInfo(tenantId, botId);
    if (StringUtils.isEmpty(pageSettingInfo)) {
      return false;
    }
    List<BotSceneDTO> botSceneList = parseBotSceneListFromPageSettingInfo(pageSettingInfo);
    return CollectionUtils.isNotEmpty(botSceneList) && botSceneList.stream().anyMatch(p -> sceneId.equals(p.getSceneId()));
  }

  /**
   * 从 pageSettingInfo JSON 字符串中解析出推荐智能体列表
   */
  @SuppressWarnings("unchecked")
  private List<BotSceneDTO> parseBotSceneListFromPageSettingInfo(String pageSettingInfoJson) {
    Map<String, Object> pageSettingInfo = JsonUtil.parseJson(pageSettingInfoJson, new TypeReference<Map<String, Object>>() {
    });
    if (pageSettingInfo == null) {
      return Collections.emptyList();
    }
    Map<String, Object> scenesSettingInfo = (Map<String, Object>) pageSettingInfo.get("scenesSettingInfo");
    if (scenesSettingInfo == null) {
      return Collections.emptyList();
    }
    String listJson = JsonUtil.toJsonString(scenesSettingInfo.get("list"));
    if (StringUtils.isEmpty(listJson)) {
      return Collections.emptyList();
    }
    return JsonUtil.parseJson(listJson, new TypeReference<List<BotSceneDTO>>() {
    });
  }

  @Override
  @Transactional
  public ResultVO<Void> setDefaultBotSceneRel(Long tenantId, Long botId, Long sceneId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    botRelaManageMapper.clearDefaultBotSceneRel(tenantId, botId, userId);
    botRelaManageMapper.updateDefaultBotSceneRel(tenantId, botId, sceneId, userId);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> cancelDefaultBotSceneRel(Long tenantId, Long botId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    botRelaManageMapper.clearDefaultBotSceneRel(tenantId, botId, userId);
    return ResultVO.success();
  }

  @Override
  public boolean existsBotSceneRel(Long tenantId, Long botId) {
    return botRelaManageMapper.existsBotSceneRelByBot(tenantId, botId);
  }

  @Override
  public List<BotSceneRelDTO> queryPlanAgentList(Long tenantId) {
    return botRelaManageMapper.selectPlanAgentList(tenantId, SceneConsts.SCENE_LABEL_PLAN_AGENT);
  }
}
