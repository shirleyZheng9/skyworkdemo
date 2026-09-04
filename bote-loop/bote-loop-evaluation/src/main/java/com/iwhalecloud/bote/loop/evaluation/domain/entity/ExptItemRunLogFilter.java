package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验项目运行日志过滤器实体
 * 迁移对应关系: Go语言ExptItemRunLogFilter
 * - 功能: 实验项目运行日志过滤条件
 * - 字段: status, resultState
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemRunLogFilter结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现getResultState()和getStatus()方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go []ItemRunState -> Java List<ItemRunState>
 * - Go *ExptItemResultState -> Java ExptItemResultState
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemRunLogFilter {
  @JsonProperty("status")
  private List<ItemRunState> status;

  @JsonProperty("result_state")
  private ExptItemResultState resultState;

  /**
   * 获取结果状态
   * 迁移对应关系: Go语言ExptItemRunLogFilter.GetResultState()
   */
  public ExptItemResultState getResultState() {
    return resultState;
  }

  /**
   * 获取状态列表（转换为int32数组）
   * 迁移对应关系: Go语言ExptItemRunLogFilter.GetStatus()
   */
  public List<Integer> getStatus() {
    if (status == null || status.isEmpty()) {
      return List.of();
    }

    return status.stream()
      .map(ItemRunState::getValue)
      .toList();
  }
}
