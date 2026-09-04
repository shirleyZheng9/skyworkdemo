package com.iwhalecloud.bote.service.a2a.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.A2aConsts;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aAgentQueryParams;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aAgentMapper;
import com.iwhalecloud.bote.mapper.bot.BotSceneManageMapper;
import com.iwhalecloud.bote.service.a2a.IA2aAgentManageService;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * A2A 服务管理服务实现
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class A2aAgentManageServiceImpl implements IA2aAgentManageService {

  private final A2aAgentMapper agentMapper;
  private final BotSceneManageMapper sceneManageMapper;
  private final IBotSceneManageService sceneManageService;
  private final ICatalogManageService catalogManageService;

  @Override
  public A2aAgentDTO findA2aAgent(Long tenantId, Long agentId) {
    A2aAgentDTO agent = agentMapper.selectAgent(tenantId, agentId);
    Assert.notNull(agent, () -> "A2A 服务不存在: agentId=" + agentId);
    return agent;
  }

  @Override
  @Transactional
  public ResultVO<A2aAgentDTO> saveA2aAgent(A2aAgentDTO agent) {
    Long tenantId = agent.getTenantId();
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.hasLength(agent.getAgentName(), "智能体名称不能为空");
    Assert.hasLength(agent.getAgentDesc(), "智能体描述不能为空");
    Assert.hasLength(agent.getAgentCardUrl(), "智能体卡片地址不能为空");
    Assert.notNull(agent.getAgentCard(), "智能体卡片不能为空");

    agent.setPlatformId(ObjectUtils.getIfNull(agent.getPlatformId(), A2aConsts.DEFAULT_A2A_PLATFORM_ID));
    agent.saveJsonConfig();
    agent.setSkillCount(CollectionUtils.size(agent.getAgentCard().skills()));

    A2aAgentDTO oldAgent;
    BotSceneDTO oldScene;
    if (agent.getAgentId() == null) {
      oldAgent = null;
      oldScene = null;
    }
    else {
      oldAgent = findA2aAgent(tenantId, agent.getAgentId());
      oldScene = sceneManageMapper.getScene(tenantId, agent.getAgentId());
      Assert.notNull(oldScene, () -> "数据异常，不存在 A2A 服务对应的智能体: sceneId=" + agent.getAgentId());
    }

    // 校验
    ResultVO<A2aAgentDTO> validationResult = validateAgent(agent, oldAgent);
    if (validationResult != null) {
      return validationResult;
    }

    // 先保存智能体
    BotSceneDTO scene = oldScene == null ? new BotSceneDTO() : BeanUtil.copy(oldScene, new BotSceneDTO());
    scene.setSceneName(agent.getAgentName());
    scene.setSceneDesc(agent.getAgentDesc());
    scene.setSceneIcon(agent.getAgentIcon());
    DataDifference<BotSceneDTO> saveSceneDifference;
    // 新增
    if (oldScene == null) {
      scene.setSceneType(SceneConsts.SCENE_TYPE_A2A);
      scene.setSceneStatus(SceneConsts.SCENE_STATUS_PUBLISH);
      scene.setCatalogItemId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
      scene.setTenantId(tenantId);
      scene.setStatusCd(BaseConsts.STATUS_CD_VALID);
      saveSceneDifference = DataDifferenceStarter.computeSave(null, scene, false, tenantId);
    }
    // 修改
    else {
      saveSceneDifference = DataDifferenceStarter.computeSave(oldScene, scene, false, tenantId);
    }

    // 使用智能体 ID 作为 A2A 服务 ID
    agent.setAgentId(scene.getSceneId());

    // 保存 A2A 服务
    DataDifference<A2aAgentDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldAgent, agent, false, tenantId, OperClassEnum.A2A_AGENT);
    if (difference == null && saveSceneDifference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(agent);
  }

  /**
   * 校验 A2A 服务
   */
  @Nullable
  private ResultVO<A2aAgentDTO> validateAgent(A2aAgentDTO agent, @Nullable A2aAgentDTO oldAgent) {
    if (oldAgent == null || !Objects.equals(oldAgent.getAgentName(), agent.getAgentName())) {
      if (agentMapper.existsAgentName(agent.getTenantId(), agent.getAgentName())) {
        return BaseErrorConstant.CHECK_NAME.toResult(agent.getAgentName());
      }
    }
    return null;
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteA2aAgent(Long tenantId, Long agentId, Long userId) {
    ResultVO<Void> result = sceneManageService.removeScene(tenantId, agentId);
    if (!result.isSuccess()) {
      return result;
    }
    agentMapper.deleteAgent(tenantId, agentId, userId);
    ResourceElementFactory.get(OperClassEnum.A2A_AGENT.name()).clear(tenantId, agentId);
    return ResultVO.success();
  }

  @Override
  public List<A2aAgentDTO> queryA2aAgentList(A2aAgentQueryParams queryParams) {
    queryParams.setCatalogItemIds(catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_A2A));
    return agentMapper.selectAgentList(queryParams);
  }

  @Override
  public PageInfo<A2aAgentDTO> queryA2aAgentPage(A2aAgentQueryParams queryParams) {
    queryParams.setCatalogItemIds(catalogManageService.queryChildrenCatalogIds(queryParams.getTenantId(), queryParams.getCatalogItemId(), CatalogConsts.TYPE_A2A));
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return agentMapper.selectAgentPage(queryParams, rowBounds).toPageInfo();
  }
}
