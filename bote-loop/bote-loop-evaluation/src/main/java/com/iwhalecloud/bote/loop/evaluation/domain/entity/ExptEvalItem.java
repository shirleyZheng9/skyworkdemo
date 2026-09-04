package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验评估项实体
 * 迁移对应关系: Go语言ExptEvalItem
 * - 功能: 实验评估项数据结构
 * - 字段: exptId, evalSetVersionId, itemId, state, updatedAt
 * <p>
 * Java实现说明:
 * - 对应Go的ExptEvalItem结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现SetState方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go ItemRunState -> Java ItemRunState
 * - Go *time.Time -> Java LocalDateTime
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptEvalItem {
  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("eval_set_version_id")
  private Long evalSetVersionId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("state")
  private ItemRunState state;

  @JsonProperty("updated_at")
  private Date updatedAt;

  /**
   * 设置状态
   * 迁移对应关系: Go语言ExptEvalItem.SetState()
   */
  public ExptEvalItem setState(ItemRunState state) {
    this.state = state;
    return this;
  }
}
