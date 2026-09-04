package com.iwhalecloud.bote.service.element.impl.manager;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocKnowledgeBaseQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import com.iwhalecloud.bote.dto.base.query.EntityPageQueryParams;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.plugin.response.PluginDefinition;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.base.EntityGlobalSearchMapper;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.element.IEntityInfoQueryService;
import com.iwhalecloud.bote.service.plugin.IPluginManageService;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bote.service.skill.ISkillFunctionManageService;
import com.iwhalecloud.bote.service.skill.ISkillPageFuncManageService;
import com.iwhalecloud.bote.service.skill.ISkillPageManageService;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bote.service.skill.ISkillSqlManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 实体查询服务实现类
 *
 * @author qian.sisheng
 * @since 2025-12-08
 */
@Service
@RequiredArgsConstructor
public class EntityInfoQueryServiceImpl implements IEntityInfoQueryService {
  private final Logger logger = LoggerFactory.getLogger(EntityInfoQueryServiceImpl.class);
  private final EntityGlobalSearchMapper entityGlobalSearchMapper;
  private final IBotSceneManageService botSceneManageService;
  private final ISkillFlowManageService skillFlowManageService;
  private final ISkillServiceManageService skillServiceManageService;
  private final ISkillFunctionManageService skillFunctionManageService;
  private final ISkillPageManageService skillPageManageService;
  private final ISkillPageFuncManageService skillPageFuncManageService;
  private final ISkillSqlManageService skillSqlManageService;
  private final IKnowledgeBaseManageService knowledgeBaseManageService;
  private final IPluginManageService pluginManageService;

  /** 实体分页查询 */
  private static final Map<DataSyncCodeEnum, Function<EntityPageQueryParams, PageInfo<?>>> entityPagequeryMap = new HashMap<>();
  /** 详情查询 */
  private static final Map<String, BiFunction<Long, Set<Long>, List<EntityInfoDTO>>> entityDetailqueryMap = new HashMap<>();

  @PostConstruct
  public void init() {
    // 实体详情查询
    entityDetailqueryMap.put(DataSyncCodeEnum.BOT.getCode(), entityGlobalSearchMapper::selectBotByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SCENE.getCode(), entityGlobalSearchMapper::selectSceneByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_FLOW.getCode(), entityGlobalSearchMapper::selectSkillFlowByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_PAGE.getCode(), entityGlobalSearchMapper::selectSkillPageByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_FUNCTION.getCode(), entityGlobalSearchMapper::selectSkillFunctionByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_PLUGIN.getCode(), entityGlobalSearchMapper::selectSkillPluginByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode(), entityGlobalSearchMapper::selectSkillPageFuncByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_SQL.getCode(), entityGlobalSearchMapper::selectSkillSqlByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_ATTR.getCode(), entityGlobalSearchMapper::selectSkillAttrByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_SERVICE.getCode(), entityGlobalSearchMapper::selectSkillServiceByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SKILL_MCP.getCode(), entityGlobalSearchMapper::selectMcpByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.KNOWLEDGE.getCode(), entityGlobalSearchMapper::selectKnowledgeByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.MODEL.getCode(), entityGlobalSearchMapper::selectLargeModelByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.DATA_TABLE.getCode(), entityGlobalSearchMapper::selectDataTableByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.A2A_AGENT.getCode(), entityGlobalSearchMapper::selectA2AByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.COPILOT_POINT.getCode(), entityGlobalSearchMapper::selectCopilotPointByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.SERVICE_PLATFORM.getCode(), entityGlobalSearchMapper::selectServicePlatformByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.DATA_SOURCE.getCode(), entityGlobalSearchMapper::selectDataSourceByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.PLUGIN.getCode(), this::queryPluginInfo);
    entityDetailqueryMap.put(DataSyncCodeEnum.JOB.getCode(), entityGlobalSearchMapper::selectJobByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.ENV_VAR.getCode(), entityGlobalSearchMapper::selectEnvByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.PROMPT.getCode(), entityGlobalSearchMapper::selectPromptByIds);
    entityDetailqueryMap.put(DataSyncCodeEnum.AGENT_SKILL.getCode(), entityGlobalSearchMapper::selectAgentSkillByIds);

    // 查询实体分页列表
    entityPagequeryMap.put(DataSyncCodeEnum.SCENE, this::queryScenePage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_FLOW, this::querySkillFlowPage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_SERVICE, this::querySkillServicePage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_FUNCTION, this::querySkillFunctionPage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_PAGE, this::querySkillPagePage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_PAGE_FUNC, this::querySkillPageFuncPage);
    entityPagequeryMap.put(DataSyncCodeEnum.SKILL_SQL, this::querySkillSqlPage);
    entityPagequeryMap.put(DataSyncCodeEnum.KNOWLEDGE, this::queryKnowledgeBasePage);
  }

  /**
   * 查询实体基本信息
   */
  @Override
  public List<EntityInfoDTO> getEntityInfo(Long tenantId, String entityType, Set<Long> entityIds) {
    // 根据类型调用对应的查询方法
    BiFunction<Long, Set<Long>, List<EntityInfoDTO>> queryFunction = entityDetailqueryMap.get(entityType);
    if (queryFunction != null && CollectionUtils.isNotEmpty(entityIds)) {
      return queryFunction.apply(tenantId, entityIds);
    }
    return Collections.emptyList();
  }

  /**
   * 查询插件信息
   */
  private List<EntityInfoDTO> queryPluginInfo(Long tenantId, Set<Long> entityIds) {
    if (CollectionUtils.isEmpty(entityIds)) {
      return Collections.emptyList();
    }
    List<EntityInfoDTO> entityInfos = new ArrayList<>();
    // 查询插件市场插件信息
    try {
      for (Long entityId : entityIds) {
        ResultVO<PluginDefinition> pluginDefinition = pluginManageService.getPluginDefinition(tenantId, entityId, false);
        PluginDefinition resultObject = pluginDefinition.getResultObject();
        if (pluginDefinition.isSuccess() && resultObject != null) {
          EntityInfoDTO entityInfo = new EntityInfoDTO();
          entityInfo.setEntityId(resultObject.getPluginId());
          entityInfo.setEntityName(resultObject.getPluginName());
          entityInfo.setEntityCode(resultObject.getPluginCode());
          entityInfo.setEntityType(DataSyncCodeEnum.PLUGIN.getCode());
          entityInfos.add(entityInfo);
        }
      }
    }
    catch (BssException e) {
      // 忽略查询异常, 插件不存在可能是旧插件
      logger.error("查询插件信息异常", e);
    }
    // 兼容旧插件
    List<EntityInfoDTO> oldPluginEntityInfos = entityGlobalSearchMapper.selectPluginByIds(tenantId, entityIds);
    if (CollectionUtils.isNotEmpty(oldPluginEntityInfos)) {
      entityInfos.addAll(oldPluginEntityInfos);
    }
    return entityInfos;
  }


  @Override
  public PageInfo<?> queryEntityPage(EntityPageQueryParams queryParam) {
    DataSyncCodeEnum codeEnum = DataSyncCodeEnum.getCodeEnum(queryParam.getEntityType());
    if (codeEnum == null || !entityPagequeryMap.containsKey(codeEnum)) {
      throw new BssException("未知的类型");
    }
    return entityPagequeryMap.get(codeEnum).apply(queryParam);
  }

  /**
   * 查询智能体分页
   */
  public PageInfo<BotSceneDTO> queryScenePage(EntityPageQueryParams queryParam) {
    BotSceneQueryParams params = new BotSceneQueryParams();
    params.setPageNum(queryParam.getPageNum());
    params.setPageSize(queryParam.getPageSize());
    params.setTenantId(queryParam.getTenantId());
    params.setSearchContent(queryParam.getSearchContent());
    return botSceneManageService.queryScenePage(params);
  }

  /**
   * 知识库分页
   */
  public PageInfo<KnowledgeBaseDTO> queryKnowledgeBasePage(EntityPageQueryParams queryParam) {
    DocKnowledgeBaseQueryParams params = new DocKnowledgeBaseQueryParams();
    params.setPageNum(queryParam.getPageNum());
    params.setPageSize(queryParam.getPageSize());
    params.setTenantId(queryParam.getTenantId());
    params.setSearchContent(queryParam.getSearchContent());
    return knowledgeBaseManageService.queryKnowledgeBasePage(params);
  }

  /**
   * 查询工作流分页
   */
  public PageInfo<SkillFlowDTO> querySkillFlowPage(EntityPageQueryParams queryParam) {
    return skillFlowManageService.querySkillFlowPage(createSkillQueryParams(queryParam));
  }

  /**
   * 技API务分页
   */
  public PageInfo<SkillServiceDTO> querySkillServicePage(EntityPageQueryParams queryParam) {
    return skillServiceManageService.querySkillServicePage(createSkillQueryParams(queryParam));
  }

  /**
   * 技能服务函数分页
   */
  public PageInfo<SkillFunctionDTO> querySkillFunctionPage(EntityPageQueryParams queryParam) {
    return skillFunctionManageService.querySkillFunctionPage(createSkillQueryParams(queryParam));
  }

  /**
   * 技能页面分页
   */
  public PageInfo<SkillPageDTO> querySkillPagePage(EntityPageQueryParams queryParam) {
    return skillPageManageService.querySkillPagePage(createSkillQueryParams(queryParam));
  }

  /**
   * 技能页面函数分页
   */
  public PageInfo<SkillPageFuncDTO> querySkillPageFuncPage(EntityPageQueryParams queryParam) {
    return skillPageFuncManageService.querySkillPageFuncPage(createSkillQueryParams(queryParam));
  }

  /**
   * 技能SQL分页
   */
  public PageInfo<SkillSqlDTO> querySkillSqlPage(EntityPageQueryParams queryParam) {
    return skillSqlManageService.querySkillSqlPage(createSkillQueryParams(queryParam));
  }

  /**
   * 创建SkillQueryParams对象
   */
  private SkillQueryParams createSkillQueryParams(EntityPageQueryParams source) {
    SkillQueryParams params = new SkillQueryParams();
    params.setPageNum(source.getPageNum());
    params.setPageSize(source.getPageSize());
    params.setTenantId(source.getTenantId());
    params.setSearchContent(source.getSearchContent());
    return params;
  }

}
