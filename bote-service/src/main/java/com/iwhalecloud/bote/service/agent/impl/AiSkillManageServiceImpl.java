package com.iwhalecloud.bote.service.agent.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.GeneraAgentIdCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.mapper.agent.AiSkillManageMapper;
import com.iwhalecloud.bote.service.agent.IAiSkillManageService;
import com.iwhalecloud.bote.service.skill.IAgentSkillManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增表记录启用技能管理服务实现
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Service
@RequiredArgsConstructor
public class AiSkillManageServiceImpl implements IAiSkillManageService {

  private final IAgentSkillManageService agentSkillManageService;
  private final AiSkillManageMapper aiSkillManageMapper;
  private final GeneraAgentIdCache generaAgentIdCache;

  @Override
  @Transactional
  public ResultVO<AiSkillDTO> saveAiSkill(AiSkillDTO skill) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    AiSkillDTO old = aiSkillManageMapper.getAiSkill(skill.getSpaceId(), skill.getBotId(), skill.getSkillId(), userId);
    if (old != null) {
      return ResultVO.success(old);
    }
    skill.setId(Sequences.AI_SKILL_ID.next());
    skill.setStatusCd(CommonConsts.STATUS_CD_VALID);
    skill.setCreatorId(userId);
    aiSkillManageMapper.insertAiSkill(skill);
    return ResultVO.success(skill);
  }

  @Override
  public PageInfo<AgentSkillDTO> queryAiAgentSkillPage(AiQueryParams queryParams) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(queryParams.getSpaceId(), queryParams.getBotId(), SessionUtil.getLoginInfo().getUserId());
    queryParams.setUserId(userId);
    // noinspection resource
    return aiSkillManageMapper.selectAiAgentSkillPage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<AgentSkillDTO> queryAiAgentSkillList(AiQueryParams queryParams) {
    Long userId = generaAgentIdCache.getBotOnwerUserId(queryParams.getSpaceId(), queryParams.getBotId(), SessionUtil.getLoginInfo().getUserId());
    queryParams.setUserId(userId);
    return aiSkillManageMapper.selectAiAgentSkillList(queryParams);
  }

  @Override
  @Transactional
  public ResultVO<AgentSkillDTO> saveAiAgentSkill(AgentSkillDTO skill) {
    boolean add = skill.getSkillId() == null;
    skill.setPlatform(BaseConsts.PLATFORM_AI_PORTAL);
    ResultVO<AgentSkillDTO> agentSkillDTOResultVO = agentSkillManageService.saveAgentSkill(skill);
    if (add && agentSkillDTOResultVO.getResultObject() != null && agentSkillDTOResultVO.getResultObject().getSkillId() != null) {
      // 只有传了 botId 才需要启用技能
      if (skill.getBotId() != null) {
        AiSkillDTO aiSkill = new AiSkillDTO();
        aiSkill.setSkillId(agentSkillDTOResultVO.getResultObject().getSkillId());
        aiSkill.setSpaceId(skill.getTenantId());
        aiSkill.setBotId(skill.getBotId());
        saveAiSkill(aiSkill);
      }
    }
    return agentSkillDTOResultVO;
  }

  @Override
  @Transactional
  public ResultVO<Boolean> deleteAgentSkill(Long spaceId, Long skillId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    boolean existAiSkill = aiSkillManageMapper.existAiSkill(spaceId, skillId, userId);
    if (existAiSkill) {
      return ResultVO.success(false);
    }
    ResultVO<Void> voidResultVO = agentSkillManageService.deleteAgentSkill(spaceId, skillId);
    Assert.isTrue(voidResultVO.isSuccess(), voidResultVO.getResultMsg());
    return ResultVO.success(true);
  }

  @Override
  public ResultVO<AiSkillDTO> disabledBtAiSkill(AiSkillDTO skill) {
    AiSkillDTO aiSkillDTO = aiSkillManageMapper.getAiSkill(skill.getSpaceId(), skill.getBotId(), skill.getSkillId(), SessionUtil.getLoginInfo().getUserId());
    Assert.notNull(aiSkillDTO, "技能未启用");
    aiSkillManageMapper.deleteAiSkill(aiSkillDTO.getId(), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success(aiSkillDTO);
  }

  @Override
  public List<AiSkillDTO> queryBotListBySkillId(Long spaceId, Long skillId) {
    List<AiSkillDTO> aiSkillDTOS = aiSkillManageMapper.selectListBySkillId(spaceId, skillId);
    for (AiSkillDTO aiSkillDTO : aiSkillDTOS) {
      // 平台预置通用智能体需要设置平台空间ID给前端加载图片
      if (BaseConsts.BOTE_AI_ID.equals(aiSkillDTO.getBotId())) {
        aiSkillDTO.setSpaceId(BaseConsts.PLATFORM_TENANT_ID);
        break;
      }
    }
    return aiSkillDTOS;
  }

  @Override
  @Transactional
  public ResultVO<List<Long>> batchSaveAiAgentSkill(AiSkillDTO skill) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    List<Long> resultList = new ArrayList<>();

    // 查询技能关联旧的应用列表
    List<AiSkillDTO> oldList = aiSkillManageMapper.selectListBySkillId(skill.getSpaceId(), skill.getSkillId());
    List<Long> oldBotIds = oldList.stream().map(AiSkillDTO::getBotId).toList();

    // 新的应用 ID 列表
    List<Long> newBotIds = skill.getBotIdList();
    if (newBotIds == null) {
      newBotIds = List.of();
    }

    // 找出新增的应用（在新的列表中但不在旧的列表中）
    List<Long> addedBotIds = newBotIds.stream()
        .filter(botId -> !oldBotIds.contains(botId))
        .toList();

    // 批量插入新增的应用
    if (!addedBotIds.isEmpty()) {
      List<AiSkillDTO> insertList = addedBotIds.stream().map(botId -> {
        AiSkillDTO aiSkill = new AiSkillDTO();
        aiSkill.setId(Sequences.AI_SKILL_ID.next());
        aiSkill.setSpaceId(skill.getSpaceId());
        aiSkill.setBotId(botId);
        aiSkill.setSkillId(skill.getSkillId());
        aiSkill.setStatusCd(CommonConsts.STATUS_CD_VALID);
        aiSkill.setCreatorId(userId);
        aiSkill.setUpdatorId(userId);
        return aiSkill;
      }).toList();
      aiSkillManageMapper.batchInsertAiSkill(insertList);
      resultList.addAll(addedBotIds);
    }

    // 找出移除的应用（在旧的列表中但不在新的列表中）
    List<Long> removedIds = new ArrayList<>();
    for (AiSkillDTO skillDTO : oldList) {
      if (!newBotIds.contains(skillDTO.getBotId())) {
        removedIds.add(skillDTO.getId());
        resultList.add(skillDTO.getBotId());
      }
    }

    // 批量移除删除的应用
    if (!removedIds.isEmpty()) {
      aiSkillManageMapper.batchUpdateAiSkillStatus(removedIds, userId);
    }

    // 返回新增和移除的应用ID列表，用于刷新缓存
    return ResultVO.success(resultList);
  }

  @Override
  public List<AiSkillDTO> queryListByBotId(Long spaceId, Long botId) {
    return aiSkillManageMapper.selectListByBotId(spaceId, botId);
  }
}
