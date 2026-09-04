package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.ck.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilterEntityDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemRunState;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果过滤器转换器
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/ck/convertor/expt_turn_result_filter.go
 * - 功能: 实验轮次结果过滤器DO和PO之间的转换
 * - 转换方向: DO ↔ PO
 * - 特殊处理: JSON字段的序列化和反序列化
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultFilterEntity2PO和ExptTurnResultFilterPO2Entity函数
 * - 处理MySQL数据库的JSON字段存储
 * - 支持复杂的数据类型转换
 * <p>
 * 技术栈迁移:
 * - Go func ExptTurnResultFilterEntity2PO -> Java convertToPO
 * - Go func ExptTurnResultFilterPO2Entity -> Java convertToDO
 * - Go stringifyInt64 -> Java stringifyLong
 * - Go ParseStringToInt64 -> Java parseStringToLong
 * - Go JSON字段 -> Java String存储JSON
 */
public final class ExptTurnResultFilterConvertor {

  private ExptTurnResultFilterConvertor() {
    // 工具类，禁止实例化
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 将DO转换为PO
   * 迁移对应关系: Go语言ExptTurnResultFilterEntity2PO
   *
   * @param filterDO 领域对象
   * @return 持久化对象
   */
  public static ExptTurnResultFilterEntity convertToPO(ExptTurnResultFilterEntityDO filterDO) {
    if (filterDO == null) {
      return null;
    }

    return ExptTurnResultFilterEntity.builder()
      .id(null) // PO类的主键由数据库生成
      .spaceId(filterDO.getSpaceId())
      .exptId(filterDO.getExptId())
      .itemId(filterDO.getItemId())
      .itemIdx(filterDO.getItemIdx())
      .turnId(filterDO.getTurnId())
      .status(filterDO.getStatus() != null ? filterDO.getStatus().getValue() : null)
      .evalSetVersionId(filterDO.getEvalSetVersionId())
      .createdDate(filterDO.getCreatedDate())
      .evalTargetData(convertMapToString(filterDO.getEvalTargetData()))
      .evaluatorScore(convertMapToString(filterDO.getEvaluatorScore()))
      .annotationFloat(convertMapToString(filterDO.getAnnotationFloat()))
      .annotationBool(convertMapToString(filterDO.getAnnotationBool()))
      .annotationString(convertMapToString(filterDO.getAnnotationString()))
      .evaluatorScoreCorrected(filterDO.getEvaluatorScoreCorrected())
      .createdAt(filterDO.getCreatedAt())
      .updatedAt(filterDO.getUpdatedAt())
      .build();
  }

  /**
   * 将PO转换为DO
   * 迁移对应关系: Go语言ExptTurnResultFilterPO2Entity
   *
   * @param filterPO 持久化对象
   * @return 领域对象
   */
  public static ExptTurnResultFilterEntityDO convertToDO(ExptTurnResultFilterEntity filterPO) {
    if (filterPO == null) {
      return null;
    }

    return ExptTurnResultFilterEntityDO.builder()
      .spaceId(filterPO.getSpaceId())
      .exptId(filterPO.getExptId())
      .itemId(filterPO.getItemId())
      .itemIdx(filterPO.getItemIdx())
      .turnId(filterPO.getTurnId())
      .status(filterPO.getStatus() != null ? ItemRunState.fromValue(filterPO.getStatus()) : null)
      .evalSetVersionId(filterPO.getEvalSetVersionId())
      .createdDate(filterPO.getCreatedDate())
      .evalTargetData(convertStringToMap(filterPO.getEvalTargetData(), String.class))
      .evaluatorScore(convertStringToMap(filterPO.getEvaluatorScore(), Double.class))
      .annotationFloat(convertStringToMap(filterPO.getAnnotationFloat(), Double.class))
      .annotationBool(convertStringToMap(filterPO.getAnnotationBool(), Boolean.class))
      .annotationString(convertStringToMap(filterPO.getAnnotationString(), String.class))
      .evaluatorScoreCorrected(filterPO.getEvaluatorScoreCorrected())
      .createdAt(filterPO.getCreatedAt())
      .updatedAt(filterPO.getUpdatedAt())
      .build();
  }

  /**
   * 将Map转换为JSON字符串
   *
   * @param map Map对象
   * @return JSON字符串
   */
  private static String convertMapToString(Map<?, ?> map) {
    if (map == null || map.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(map);
    }
    catch (JsonProcessingException e) {
      throw new BssException("Map转换为JSON字符串失败: " + e.getMessage(), e);
    }
  }

  /**
   * 将JSON字符串转换为Map
   *
   * @param jsonString JSON字符串
   * @param valueType 值类型
   * @return Map对象
   */
  private static <T> Map<String, T> convertStringToMap(String jsonString, Class<T> valueType) {
    if (jsonString == null || jsonString.isEmpty()) {
      return new HashMap<>();
    }
    try {
      TypeReference<Map<String, T>> typeRef = new TypeReference<>() {
      };
      return objectMapper.readValue(jsonString, typeRef);
    }
    catch (JsonProcessingException e) {
      throw new BssException("JSON字符串转换为Map失败: " + e.getMessage() + valueType.getName(), e);
    }
  }

  public static List<ExptTurnResultFilterEntity> convertToPOList(List<ExptTurnResultFilterEntityDO> filters) {
    return filters.stream().map(ExptTurnResultFilterConvertor::convertToPO).toList();
  }

  public static List<ExptTurnResultFilterEntityDO> convertToDOList(List<ExptTurnResultFilterEntity> pos) {
    return pos.stream().map(ExptTurnResultFilterConvertor::convertToDO).toList();
  }
}
