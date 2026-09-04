package com.iwhalecloud.bote.loop.evaluation.application.convertor.experiment;

import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentFilterDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptFilterOptionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FieldTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FilterConditionDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FilterFieldDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FilterLogicOpDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FilterOperatorTypeDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.FiltersDTO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BatchGetEvalTargetBySourceReqParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptFilterFields;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptListFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterAccelerator;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterMapCond;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldFilterDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunStateFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemSnapshotFilterDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.KeywordFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ScoreFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunState;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TurnRunStateFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.service.IEvalTargetService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * 实验过滤器转换器
 * 对应Go: experiment/filter.go
 */
public final class ExperimentFilterApplicationConvertor {

  private ExperimentFilterApplicationConvertor() {
    // 工具类，禁止实例化
  }

  private static final IEvalTargetService evalTargetService = SpringUtil.getBean(IEvalTargetService.class);

  private static final Map<FilterOperatorTypeDTO, String> OPERATOR_MAP = new HashMap<>();

  static {
    OPERATOR_MAP.put(FilterOperatorTypeDTO.EQUAL, "=");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.NOT_EQUAL, "!=");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.GREATER, ">");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.GREATER_OR_EQUAL, ">=");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.LESS, "<");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.LESS_OR_EQUAL, "<=");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.IN, "IN");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.NOT_IN, "NOT IN");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.LIKE, "LIKE");
    OPERATOR_MAP.put(FilterOperatorTypeDTO.NOT_LIKE, "NOT LIKE");
  }

  /**
   * 转换过滤选项
   * 对应Go: Convert
   */
  public static ExptListFilter convert(ExptFilterOptionDTO efo, Long spaceId) {
    if (efo == null) {
      return null;
    }

    ExptListFilter filters = convertFilters(efo.getFilters(), spaceId);
    if (filters != null) {
      filters.setFuzzyName(efo.getFuzzyName());
    }

    return filters;
  }

  /**
   * 转换过滤器
   * 对应Go: ConvertFilters
   */
  public static ExptListFilter convertFilters(FiltersDTO filters, Long spaceId) {
    ExptListFilter efo = buildBasicFilter();
    if (filters == null) {
      return efo;
    }

    validateFilterLogic(filters);
    boolean setDefaultExptTypeFlag = processFilterConditions(filters, efo, spaceId);
    setDefaultExptTypeIfNeeded(efo, setDefaultExptTypeFlag);

    return efo;
  }

  private static ExptListFilter buildBasicFilter() {
    return ExptListFilter.builder()
      .includes(new ExptFilterFields())
      .excludes(new ExptFilterFields())
      .build();
  }

  private static boolean processFilterConditions(FiltersDTO filters, ExptListFilter efo, Long spaceId) {
    boolean setDefaultExptTypeFlag = true;
    for (FilterConditionDTO cond : filters.getFilterConditions()) {
      if (cond.getField() == null) {
        continue;
      }

      ExptFilterFields ff = getFilterFields(cond.getOperator(), efo);
      boolean processed = processFilterCondition(cond, ff) || processFilterConditionByTarget(cond, ff, spaceId);
      if (cond.getField().getFieldType() == FieldTypeDTO.EXPT_TYPE && processed) {
        setDefaultExptTypeFlag = false;
      }
    }
    return setDefaultExptTypeFlag;
  }

  private static boolean processFilterCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    return switch (cond.getField().getFieldType()) {
      case CREATOR_BY -> processCreatorByCondition(cond, ff);
      case EXPT_STATUS -> processExptStatusCondition(cond, ff);
      case EXPT_TYPE -> processExptTypeCondition(cond, ff);
      default -> false;
    };
  }

  private static boolean processFilterConditionByTarget(FilterConditionDTO cond, ExptFilterFields ff, Long spaceId) {
    return switch (cond.getField().getFieldType()) {
      case EVAL_SET_ID -> processEvalSetIdCondition(cond, ff);
      case TARGET_ID -> processTargetIdCondition(cond, ff);
      case EVALUATOR_ID -> processEvaluatorIdCondition(cond, ff);
      case TARGET_TYPE -> processTargetTypeCondition(cond, ff);
      case SOURCE_TARGET -> processSourceTargetCondition(cond, ff, spaceId);
      case SOURCE_TYPE -> processSourceTypeCondition(cond, ff);
      case SOURCE_ID -> processSourceIdCondition(cond, ff);
      default -> false;
    };
  }

  private static boolean processCreatorByCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<String> createdBys = parseStringList(cond.getValue());
    ff.setCreatedBy(intersectIgnoreNull(ff.getCreatedBy(), createdBys));
    return true;
  }

  private static boolean processExptStatusCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> exptStatusList = parseIntList(cond.getValue());
    ff.setStatus(intersectIgnoreNull(ff.getStatus(), exptStatusList));
    return true;
  }

  private static boolean processEvalSetIdCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> evalSetIds = parseIntList(cond.getValue());
    ff.setEvalSetIds(intersectIgnoreNull(ff.getEvalSetIds(), evalSetIds));
    return true;
  }

  private static boolean processTargetIdCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> targetIds = parseIntList(cond.getValue());
    ff.setTargetIds(intersectIgnoreNull(ff.getTargetIds(), targetIds));
    return true;
  }

  private static boolean processEvaluatorIdCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> evaluatorIds = parseIntList(cond.getValue());
    ff.setEvaluatorIds(intersectIgnoreNull(ff.getEvaluatorIds(), evaluatorIds));
    return true;
  }

  private static boolean processTargetTypeCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> targetTypes = parseIntList(cond.getValue());
    ff.setTargetType(intersectIgnoreNull(ff.getTargetType(), targetTypes));
    return true;
  }

  private static boolean processSourceTargetCondition(FilterConditionDTO cond, ExptFilterFields ff, Long spaceId) {
    if (cond.getSourceTarget() == null || CollectionUtils.isEmpty(cond.getSourceTarget().getSourceTargetIds())) {
      return false;
    }

    BatchGetEvalTargetBySourceReqParam param = buildSourceTargetParam(cond, spaceId);
    List<EvalTarget> targets = evalTargetService.batchGetEvalTargetBySource(param);

    if (cond.getSourceTarget().getSourceTargetIds().size() == 1 && targets.isEmpty()) {
      ff.getTargetIds().add(-1L); // 无效查询，返回空结果
      return true;
    }

    List<Long> targetIdList = targets.stream()
      .map(EvalTarget::getId)
      .collect(Collectors.toList());
    ff.setTargetIds(intersectIgnoreNull(ff.getTargetIds(), targetIdList));
    return true;
  }

  private static BatchGetEvalTargetBySourceReqParam buildSourceTargetParam(FilterConditionDTO cond, Long spaceId) {
    return BatchGetEvalTargetBySourceReqParam.builder()
      .spaceId(spaceId)
      .sourceTargetId(cond.getSourceTarget().getSourceTargetIds())
      .targetType(EvalTargetType.fromValue(cond.getSourceTarget().getEvalTargetType().getValue()))
      .build();
  }

  private static boolean processExptTypeCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> types = parseIntList(cond.getValue());
    ff.setExptType(intersectIgnoreNull(ff.getExptType(), types));
    return true;
  }

  private static boolean processSourceTypeCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<Long> sourceTypes = parseIntList(cond.getValue());
    ff.setSourceType(intersectIgnoreNull(ff.getSourceType(), sourceTypes));
    return true;
  }

  private static boolean processSourceIdCondition(FilterConditionDTO cond, ExptFilterFields ff) {
    if (cond.getValue() == null || cond.getValue().isEmpty()) {
      return false;
    }
    List<String> sourceIds = parseStringList(cond.getValue());
    ff.setSourceId(intersectIgnoreNull(ff.getSourceId(), sourceIds));
    return true;
  }

  private static void setDefaultExptTypeIfNeeded(ExptListFilter efo, boolean setDefaultExptTypeFlag) {
    if (setDefaultExptTypeFlag) {
      efo.getIncludes().setExptType(intersectIgnoreNull(
        efo.getIncludes().getExptType(),
        List.of((long) ExptTypeDTO.ONLINE.getValue())
      ));
    }
  }

  /**
   * 转换实验轮次结果过滤器
   * 对应Go: ConvertExptTurnResultFilter
   */
  public static ExptTurnResultFilter convertExptTurnResultFilter(FiltersDTO filters) {
    List<TurnRunStateFilter> turnRunStateFilters = new ArrayList<>();
    List<ItemRunStateFilter> itemRunStateFilters = new ArrayList<>();
    List<ScoreFilter> scoreFilters = new ArrayList<>();

    if (filters != null && !CollectionUtils.isEmpty(filters.getFilterConditions())) {
      if (filters.getLogicOp() != FilterLogicOpDTO.AND) {
        throw new IllegalArgumentException("invalid logic op");
      }

      for (FilterConditionDTO filterCondition : filters.getFilterConditions()) {
        if (filterCondition == null) {
          continue;
        }

        checkFilterCondition(filterCondition);
        String operator = parseOperator(filterCondition.getOperator());

        switch (filterCondition.getField().getFieldType()) {
          case TURN_RUN_STATE:
            List<TurnRunState> turnRunStates = parseTurnRunState(filterCondition);
            turnRunStateFilters.add(TurnRunStateFilter.builder()
              .status(turnRunStates)
              .operator(operator)
              .build());
            break;

          case EVALUATOR_SCORE:
            Double score = Double.parseDouble(filterCondition.getValue());
            Long evaluatorVersionId = Long.parseLong(filterCondition.getField().getFieldKey());
            scoreFilters.add(ScoreFilter.builder()
              .score(score)
              .operator(operator)
              .evaluatorVersionId(evaluatorVersionId)
              .build());
            break;
          case ITEM_RUN_STATE:
            List<ItemRunState> itemRunStates = parseItemRunState(filterCondition);
            itemRunStateFilters.add(ItemRunStateFilter.builder()
              .status(itemRunStates)
              .operator(operator)
              .build());
            break;
          default:
            throw new IllegalArgumentException("invalid field type");
        }
      }
    }

    return ExptTurnResultFilter.builder()
      .turnRunStateFilters(turnRunStateFilters)
      .itemRunStateFilters(itemRunStateFilters)
      .scoreFilters(scoreFilters)
      .build();
  }

  /**
   * 转换实验轮次结果过滤器加速器
   * 对应Go: ConvertExptTurnResultFilterAccelerator
   */
  public static ExptTurnResultFilterAccelerator convertExptTurnResultFilterAccelerator(ExperimentFilterDTO experimentFilter) {
    ExptTurnResultFilterAccelerator result = buildBasicFilterAccelerator();

    if (hasNoFilterConditions(experimentFilter)) {
      return result;
    }

    validateFilterLogic(experimentFilter.getFilters());
    processRegularFilters(experimentFilter, result);
    processKeywordSearch(experimentFilter, result);

    return result;
  }

  private static ExptTurnResultFilterAccelerator buildBasicFilterAccelerator() {
    return ExptTurnResultFilterAccelerator.builder()
      .itemIds(new ArrayList<>())
      .itemRunStatus(new ArrayList<>())
      .turnRunStatus(new ArrayList<>())
      .mapCond(buildMapCondition())
      .itemSnapshotCond(buildItemSnapshotCondition())
      .keywordSearch(buildKeywordSearch())
      .build();
  }

  private static ExptTurnResultFilterMapCond buildMapCondition() {
    return ExptTurnResultFilterMapCond.builder()
      .evalTargetDataFilters(new ArrayList<>())
      .evaluatorScoreFilters(new ArrayList<>())
      .annotationFloatFilters(new ArrayList<>())
      .annotationBoolFilters(new ArrayList<>())
      .annotationStringFilters(new ArrayList<>())
      .build();
  }

  private static ItemSnapshotFilterDO buildItemSnapshotCondition() {
    return ItemSnapshotFilterDO.builder()
      .boolMapFilters(new ArrayList<>())
      .stringMapFilters(new ArrayList<>())
      .intMapFilters(new ArrayList<>())
      .floatMapFilters(new ArrayList<>())
      .build();
  }

  private static KeywordFilter buildKeywordSearch() {
    return KeywordFilter.builder()
      .evalTargetDataFilters(new ArrayList<>())
      .itemSnapshotFilter(buildItemSnapshotCondition())
      .build();
  }

  private static boolean hasNoFilterConditions(ExperimentFilterDTO experimentFilter) {
    boolean noRegularFilters = experimentFilter.getFilters() == null ||
      CollectionUtils.isEmpty(experimentFilter.getFilters().getFilterConditions());
    boolean noKeywordSearch = experimentFilter.getKeywordSearch() == null ||
      CollectionUtils.isEmpty(experimentFilter.getKeywordSearch().getFilterFields()) ||
      experimentFilter.getKeywordSearch().getKeyword() == null;

    return noRegularFilters && noKeywordSearch;
  }

  private static void validateFilterLogic(FiltersDTO filters) {
    if (filters != null && filters.getLogicOp() != FilterLogicOpDTO.AND) {
      throw new IllegalArgumentException("invalid logic op");
    }
  }

  private static void processRegularFilters(ExperimentFilterDTO experimentFilter, ExptTurnResultFilterAccelerator result) {
    if (experimentFilter.getFilters() != null && !CollectionUtils.isEmpty(experimentFilter.getFilters().getFilterConditions())) {
      processFilterConditions(experimentFilter.getFilters().getFilterConditions(), result);
    }
  }

  private static void processKeywordSearch(ExperimentFilterDTO experimentFilter, ExptTurnResultFilterAccelerator result) {
    if (experimentFilter.getKeywordSearch() != null &&
      !CollectionUtils.isEmpty(experimentFilter.getKeywordSearch().getFilterFields()) &&
      experimentFilter.getKeywordSearch().getKeyword() != null) {

      result.getKeywordSearch().setKeyword(experimentFilter.getKeywordSearch().getKeyword());
      processKeywordSearchFields(experimentFilter.getKeywordSearch().getFilterFields(), result, experimentFilter.getKeywordSearch().getKeyword());
    }
  }

  // 辅助方法
  private static ExptFilterFields getFilterFields(FilterOperatorTypeDTO operatorType, ExptListFilter efo) {
    return switch (operatorType) {
      case IN, EQUAL -> efo.getIncludes();
      case NOT_IN, NOT_EQUAL -> efo.getExcludes();
      default -> new ExptFilterFields();
    };
  }

  private static <T> List<T> intersectIgnoreNull(List<T> list1, List<T> list2) {
    if (CollectionUtils.isEmpty(list1)) {
      return list2;
    }
    if (CollectionUtils.isEmpty(list2)) {
      return list1;
    }

    Set<T> set1 = new HashSet<>(list1);
    return list2.stream()
      .filter(set1::contains)
      .collect(Collectors.toList());
  }

  private static List<Long> parseIntList(String str) {
    if (str == null || str.isEmpty()) {
      return new ArrayList<>();
    }

    return Arrays.stream(str.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .map(Long::parseLong)
      .collect(Collectors.toList());
  }

  private static List<String> parseStringList(String str) {
    if (str == null || str.isEmpty()) {
      return new ArrayList<>();
    }

    return Arrays.asList(str.split(","));
  }

  private static String parseOperator(FilterOperatorTypeDTO operatorType) {
    String operator = OPERATOR_MAP.get(operatorType);
    if (operator == null) {
      throw new IllegalArgumentException("invalid operator: " + operatorType);
    }
    return operator;
  }

  private static List<TurnRunState> parseTurnRunState(FilterConditionDTO filterCondition) {
    // 使用","分割
    String[] strStates = filterCondition.getValue().split(",");

    // 解析为TurnRunState
    List<TurnRunState> states = new ArrayList<>();
    for (String strState : strStates) {
      if (strState.trim().isEmpty()) { // 兜底：前端取消筛选后TurnRunState可能会传空字符串
        continue;
      }

      try {
        int turnRunState = Integer.parseInt(strState.trim());
        states.add(TurnRunState.fromValue(turnRunState));
      }
      catch (NumberFormatException e) {
        throw new IllegalArgumentException("invalid turn run state: " + strState, e);
      }
    }

    return states;
  }

  private static List<ItemRunState> parseItemRunState(FilterConditionDTO filterCondition) {
    // 使用","分割
    String[] strStates = filterCondition.getValue().split(",");

    // 解析为ItemRunState
    List<ItemRunState> states = new ArrayList<>();
    for (String strState : strStates) {
      if (strState.trim().isEmpty()) { // 兜底：前端取消筛选后ItemRunState可能会传空字符串
        continue;
      }

      try {
        int itemRunState = Integer.parseInt(strState.trim());
        states.add(ItemRunState.fromValue(itemRunState));
      }
      catch (NumberFormatException e) {
        throw new IllegalArgumentException("invalid item run state: " + strState, e);
      }
    }

    return states;
  }

  private static void checkFilterCondition(FilterConditionDTO filterCondition) {
    // 其他字段类型暂不需要特殊校验
    if (Objects.requireNonNull(filterCondition.getField().getFieldType()) == FieldTypeDTO.TURN_RUN_STATE) {
      if (filterCondition.getOperator() != FilterOperatorTypeDTO.IN &&
        filterCondition.getOperator() != FilterOperatorTypeDTO.NOT_IN) {
        throw new IllegalArgumentException("invalid operator for TURN_RUN_STATE");
      }
    }
    if (Objects.requireNonNull(filterCondition.getField().getFieldType()) == FieldTypeDTO.ITEM_RUN_STATE) {
      if (filterCondition.getOperator() != FilterOperatorTypeDTO.IN &&
        filterCondition.getOperator() != FilterOperatorTypeDTO.NOT_IN) {
        throw new IllegalArgumentException("invalid operator for ITEM_RUN_STATE");
      }
    }
  }

  private static void processFilterConditions(List<FilterConditionDTO> filterConditions, ExptTurnResultFilterAccelerator result) {
    for (FilterConditionDTO filterCondition : filterConditions) {
      if (filterCondition == null || filterCondition.getField() == null) {
        continue;
      }

      FieldFilterDO fieldFilter = buildFieldFilter(filterCondition);
      distributeFieldFilter(filterCondition.getField().getFieldType(), fieldFilter, result);
    }
  }

  private static FieldFilterDO buildFieldFilter(FilterConditionDTO filterCondition) {
    String op = parseOperator(filterCondition.getOperator());
    List<Object> values = parseFilterValues(filterCondition.getValue(), op);

    return FieldFilterDO.builder()
      .key(filterCondition.getField().getFieldKey())
      .op(op)
      .values(values)
      .build();
  }

  private static List<Object> parseFilterValues(String value, String op) {
    if ("IN".equals(op) || "NOT IN".equals(op)) {
      return Arrays.stream(value.split(","))
        .map(String::trim)
        .collect(Collectors.toList());
    }
    else {
      return List.of(value);
    }
  }

  private static void distributeFieldFilter(FieldTypeDTO fieldType, FieldFilterDO fieldFilter, ExptTurnResultFilterAccelerator result) {
    switch (fieldType) {
      case ANNOTATION:
        result.getMapCond().getAnnotationFloatFilters().add(fieldFilter);
        break;
      case EVAL_SET_COLUMN:
        result.getItemSnapshotCond().getStringMapFilters().add(fieldFilter);
        break;
      case ACTUAL_OUTPUT:
        result.getMapCond().getEvalTargetDataFilters().add(fieldFilter);
        break;
      case EVALUATOR_SCORE_CORRECTED:
        result.setEvaluatorScoreCorrected(fieldFilter);
        break;
      case EVALUATOR_SCORE:
        result.getMapCond().getEvaluatorScoreFilters().add(fieldFilter);
        break;
      case ITEM_RUN_STATE:
        result.getItemRunStatus().add(fieldFilter);
        break;
      case ITEM_ID:
        result.getItemIds().add(fieldFilter);
        break;
      default:
        // 其它主表字段可按需补充
        break;
    }
  }

  private static void processKeywordSearchFields(List<FilterFieldDTO> filterFields, ExptTurnResultFilterAccelerator result, String keyword) {
    for (FilterFieldDTO filterField : filterFields) {
      if (filterField == null) {
        continue;
      }

      FieldTypeDTO fieldType = filterField.getFieldType();
      String fieldKey = filterField.getFieldKey();
      FieldFilterDO fieldFilter = FieldFilterDO.builder()
        .key(fieldKey)
        .op("LIKE")
        .values(List.of(keyword))
        .build();

      switch (fieldType) {
        case EVAL_SET_COLUMN:
          // 评测集列字段，统一作为item_snapshot的string_map条件
          result.getKeywordSearch().getItemSnapshotFilter().getStringMapFilters().add(fieldFilter);
          break;
        case ACTUAL_OUTPUT:
          // 实际输出，通常为string类型
          result.getKeywordSearch().getEvalTargetDataFilters().add(fieldFilter);
          break;
        default:
          // 其他字段类型暂不支持关键词搜索
          break;
      }
    }
  }
}
