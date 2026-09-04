package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验过滤字段实体
 * 迁移对应关系: Go语言ExptFilterFields
 * - 功能: 实验过滤字段条件
 * - 字段: createdBy, status, evalSetIDs, targetIDs, evaluatorIDs, targetType, exptType, sourceType, sourceID
 * <p>
 * Java实现说明:
 * - 对应Go的ExptFilterFields结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现isValid()方法进行验证
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []string -> Java List<String>
 * - Go []int64 -> Java List<Long>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptFilterFields {
  @JsonProperty("created_by")
  private List<String> createdBy;

  @JsonProperty("status")
  private List<Long> status;

  @JsonProperty("eval_set_ids")
  private List<Long> evalSetIds;

  @JsonProperty("target_ids")
  private List<Long> targetIds;

  @JsonProperty("evaluator_ids")
  private List<Long> evaluatorIds;

  @JsonProperty("target_type")
  private List<Long> targetType;

  @JsonProperty("expt_type")
  private List<Long> exptType;

  @JsonProperty("source_type")
  private List<Long> sourceType;

  @JsonProperty("source_id")
  private List<String> sourceId;

  /**
   * 验证过滤字段是否有效
   * 迁移对应关系: Go语言ExptFilterFields.IsValid()
   */
  public boolean isValid() {

    // 验证int64列表中的值是否都大于等于0
    List<List<Long>> int64Lists = List.of(
      status, evalSetIds, targetIds, evaluatorIds, targetType, exptType, sourceType
    );

    for (List<Long> list : int64Lists) {
      if (list != null) {
        for (Long item : list) {
          if (item < 0) {
            return false;
          }
        }
      }
    }

    // 验证字符串列表中的值是否都不为空
    if (createdBy != null) {
      for (String item : createdBy) {
        if (item == null || item.isEmpty()) {
          return false;
        }
      }
    }

    return true;
  }
}
