package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验项评估结果实体
 * 迁移对应关系: Go语言ExptItemEvalResult
 * - 功能: 实验项评估结果数据结构
 * - 字段: itemResultRunLog, turnResultRunLogs
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemEvalResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *ExptItemResultRunLog -> Java ExptItemResultRunLog
 * - Go map[int64]*ExptTurnResultRunLog -> Java Map<Long, ExptTurnResultRunLog>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemEvalResult {
  @JsonProperty("item_result_run_log")
  private ExptItemResultRunLog itemResultRunLog;

  @JsonProperty("turn_result_run_logs")
  private Map<Long, ExptTurnResultRunLog> turnResultRunLogs;
}
