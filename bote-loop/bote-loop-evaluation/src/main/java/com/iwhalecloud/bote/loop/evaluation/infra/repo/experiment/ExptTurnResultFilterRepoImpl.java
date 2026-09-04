package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterKeyMappingEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterAccelerator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterEntityDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterKeyMapping;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldFilterDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSnapshotFilterDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.KeywordFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.IExptTurnResultFilterRepo;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ExptTurnResultFilterMapCond;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ExptTurnResultFilterQueryCond;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.FieldFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ItemSnapshotFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.KeywordMapCond;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.Page;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck.IExptTurnResultFilterDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck.convertor.ExptTurnResultFilterConvertor;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.IExptTurnResultFilterKeyMappingDAO;
import com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql.convertor.ExptTurnResultFilterKeyMappingConvertor;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实验轮次结果过滤器REPO实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/expt_turn_result_filter_repo_impl.go
 * - 功能: 实验轮次结果过滤器数据访问实现
 * - 主要方法:
 * * save - 保存过滤器数据
 * * queryItemIdStates - 查询项目ID状态
 * * getExptTurnResultFilterKeyMappings - 获取过滤器键映射
 * * insertExptTurnResultFilterKeyMappings - 插入过滤器键映射
 * * getByExptIdItemIds - 根据实验ID和项目ID获取数据
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultFilterRepoImpl结构体
 * - 使用Spring组件注解
 * - 使用转换器进行DO和PO转换
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go []*entity.ExptTurnResultFilterEntity -> Java List<ExptTurnResultFilter>
 * - Go convertor.ExptTurnResultFilterEntity2PO -> Java ExptTurnResultFilterConvertor
 */
@Component
@RequiredArgsConstructor
public class ExptTurnResultFilterRepoImpl implements IExptTurnResultFilterRepo {
  private final IExptTurnResultFilterDAO exptTurnResultFilterDAO;
  private final IExptTurnResultFilterKeyMappingDAO exptTurnResultFilterKeyMappingDAO;

  @Override
  public void save(List<ExptTurnResultFilterEntityDO> filter) {
    try {
      List<ExptTurnResultFilterEntity> filterPOs = ExptTurnResultFilterConvertor.convertToPOList(filter);
      exptTurnResultFilterDAO.save(filterPOs);
    }
    catch (Exception e) {
      throw new BssException("保存过滤器数据失败: " + e.getMessage(), e);
    }
  }

  @Override
  public Map<Long, ItemRunState> queryItemIdStates(ExptTurnResultFilterAccelerator filter) {
    try {
      // 构建查询条件
      ExptTurnResultFilterQueryCond cond = buildQueryCond(filter);

      // 查询项目ID状态
      Map<String, Integer> itemIdStates = exptTurnResultFilterDAO.queryItemIdStates(cond);

      // 转换结果
      Map<Long, ItemRunState> result = new HashMap<>();
      for (Map.Entry<String, Integer> entry : itemIdStates.entrySet()) {
        Long itemId = Long.parseLong(entry.getKey());
        ItemRunState state = ItemRunState.fromValue(entry.getValue());
        result.put(itemId, state);
      }

      return result;
    }
    catch (Exception e) {
      throw new BssException("查询项目ID状态失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResultFilterKeyMapping> getExptTurnResultFilterKeyMappings(Long spaceId, Long exptId) {
    try {
      List<ExptTurnResultFilterKeyMappingEntity> mappingPOs = exptTurnResultFilterKeyMappingDAO.getByExptId(spaceId, exptId);
      return ExptTurnResultFilterKeyMappingConvertor.convertToDOList(mappingPOs);
    }
    catch (Exception e) {
      throw new BssException("获取过滤器键映射失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void insertExptTurnResultFilterKeyMappings(List<ExptTurnResultFilterKeyMapping> mappings) {
    try {
      if (mappings.isEmpty()) {
        return;
      }
      List<ExptTurnResultFilterKeyMappingEntity> mappingPOs = ExptTurnResultFilterKeyMappingConvertor.convertToPOList(mappings);
      exptTurnResultFilterKeyMappingDAO.insert(mappingPOs);
    }
    catch (Exception e) {
      throw new BssException("插入过滤器键映射失败: " + e.getMessage(), e);
    }
  }

  @Override
  public List<ExptTurnResultFilterEntityDO> getByExptIdItemIds(String spaceId, String exptId, String createdDate, List<String> itemIds) {
    try {
      List<ExptTurnResultFilterEntity> filterPOs = exptTurnResultFilterDAO.getByExptIdItemIds(spaceId, exptId, createdDate, itemIds);
      return ExptTurnResultFilterConvertor.convertToDOList(filterPOs);
    }
    catch (Exception e) {
      throw new BssException("根据实验ID和项目ID获取数据失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构建查询条件
   * 迁移对应关系: Go语言QueryItemIDStates方法中的条件构建逻辑
   */
  private ExptTurnResultFilterQueryCond buildQueryCond(ExptTurnResultFilterAccelerator filter) {
    ExptTurnResultFilterQueryCond cond = new ExptTurnResultFilterQueryCond();

    setBasicFields(cond, filter);
    setStatusFilters(cond, filter);
    setMapConditions(cond, filter);
    setItemSnapshotConditions(cond, filter);
    setKeywordSearch(cond, filter);
    setPagination(cond, filter);

    return cond;
  }

  private void setBasicFields(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    if (filter.getSpaceId() != null && filter.getSpaceId() != 0) {
      cond.setSpaceId(String.valueOf(filter.getSpaceId()));
    }
    if (filter.getExptId() != null && filter.getExptId() != 0) {
      cond.setExptId(String.valueOf(filter.getExptId()));
    }
    if (filter.getCreatedDate() != null) {
      cond.setCreatedDate(filter.getCreatedDate());
    }
    if (filter.getEvaluatorScoreCorrected() != null) {
      cond.setEvaluatorScoreCorrected(convertFieldFilter(filter.getEvaluatorScoreCorrected()));
    }
  }

  private void setStatusFilters(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    cond.setItemIds(convertFieldFilters(filter.getItemIds()));
    cond.setItemRunStatus(convertFieldFilters(filter.getItemRunStatus()));
    cond.setTurnRunStatus(convertFieldFilters(filter.getTurnRunStatus()));
  }

  private void setMapConditions(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    if (filter.getMapCond() != null) {
      ExptTurnResultFilterMapCond mapCond = createMapCondition(filter.getMapCond());
      cond.setMapCond(mapCond);
    }
  }

  private ExptTurnResultFilterMapCond createMapCondition(com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterMapCond mapCondDO) {
    ExptTurnResultFilterMapCond mapCond = new ExptTurnResultFilterMapCond();
    mapCond.setEvalTargetDataFilters(convertFieldFilters(mapCondDO.getEvalTargetDataFilters()));
    mapCond.setEvaluatorScoreFilters(convertFieldFilters(mapCondDO.getEvaluatorScoreFilters()));
    mapCond.setAnnotationFloatFilters(convertFieldFilters(mapCondDO.getAnnotationFloatFilters()));
    mapCond.setAnnotationBoolFilters(convertFieldFilters(mapCondDO.getAnnotationBoolFilters()));
    mapCond.setAnnotationStringFilters(convertFieldFilters(mapCondDO.getAnnotationStringFilters()));
    return mapCond;
  }

  private void setItemSnapshotConditions(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    cond.setEvalSetSyncCkDate(filter.getEvalSetSyncCkDate());
    if (filter.getItemSnapshotCond() != null) {
      ItemSnapshotFilter itemSnapshotCond = createItemSnapshotFilter(filter.getItemSnapshotCond());
      cond.setItemSnapshotCond(itemSnapshotCond);
    }
  }

  private ItemSnapshotFilter createItemSnapshotFilter(ItemSnapshotFilterDO itemSnapshotCondDO) {
    ItemSnapshotFilter itemSnapshotCond = new ItemSnapshotFilter();
    itemSnapshotCond.setBoolMapFilters(convertFieldFilters(itemSnapshotCondDO.getBoolMapFilters()));
    itemSnapshotCond.setFloatMapFilters(convertFieldFilters(itemSnapshotCondDO.getFloatMapFilters()));
    itemSnapshotCond.setIntMapFilters(convertFieldFilters(itemSnapshotCondDO.getIntMapFilters()));
    itemSnapshotCond.setStringMapFilters(convertFieldFilters(itemSnapshotCondDO.getStringMapFilters()));
    return itemSnapshotCond;
  }

  private void setKeywordSearch(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    if (filter.getKeywordSearch() != null) {
      KeywordMapCond keywordSearch = createKeywordSearch(filter.getKeywordSearch());
      cond.setKeywordSearch(keywordSearch);
    }
  }

  private KeywordMapCond createKeywordSearch(KeywordFilter keywordSearchDO) {
    KeywordMapCond keywordSearch = new KeywordMapCond();
    keywordSearch.setItemSnapshotFilter(convertItemSnapshotFilter(keywordSearchDO.getItemSnapshotFilter()));
    keywordSearch.setEvalTargetDataFilters(convertFieldFilters(keywordSearchDO.getEvalTargetDataFilters()));
    keywordSearch.setKeyword(keywordSearchDO.getKeyword());
    return keywordSearch;
  }

  private void setPagination(ExptTurnResultFilterQueryCond cond, ExptTurnResultFilterAccelerator filter) {
    if (filter.getPage() != null) {
      Page page = createPage(filter.getPage());
      cond.setPage(page);
    }
  }

  private Page createPage(com.iwhalecloud.bote.loop.evaluation.domain.entity.Page pageDO) {
    Page page = new Page();
    page.setOffset(pageDO.getOffset());
    page.setLimit(pageDO.getLimit());
    return page;
  }

  /**
   * 转换字段过滤器列表
   */
  private List<FieldFilter> convertFieldFilters(List<FieldFilterDO> fieldFilters) {
    if (fieldFilters == null) {
      return new ArrayList<>();
    }
    return fieldFilters.stream()
      .map(this::convertFieldFilter)
      .collect(Collectors.toList());
  }

  /**
   * 转换字段过滤器
   */
  private FieldFilter convertFieldFilter(FieldFilterDO fieldFilter) {
    if (fieldFilter == null) {
      return null;
    }
    FieldFilter result = new FieldFilter();
    result.setKey(fieldFilter.getKey());
    result.setOp(fieldFilter.getOp());
    result.setValues(fieldFilter.getValues());
    return result;
  }

  /**
   * 转换项目快照过滤器
   */
  private ItemSnapshotFilter convertItemSnapshotFilter(ItemSnapshotFilterDO itemSnapshotFilter) {
    if (itemSnapshotFilter == null) {
      return null;
    }
    ItemSnapshotFilter result = new ItemSnapshotFilter();
    result.setBoolMapFilters(convertFieldFilters(itemSnapshotFilter.getBoolMapFilters()));
    result.setFloatMapFilters(convertFieldFilters(itemSnapshotFilter.getFloatMapFilters()));
    result.setIntMapFilters(convertFieldFilters(itemSnapshotFilter.getIntMapFilters()));
    result.setStringMapFilters(convertFieldFilters(itemSnapshotFilter.getStringMapFilters()));
    return result;
  }
}
