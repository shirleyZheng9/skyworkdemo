package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务运行消息实体
 * 迁移对应关系: Go语言entity.JobRunMessage
 * - 功能: 存储任务运行消息信息
 * - 字段定义:
 * * Type: JobRunType - 任务运行类型
 * * SpaceID: int64 - 空间ID
 * * TaskID: int64 - 任务ID (已废弃，使用JobID代替)
 * * RunID: int64 - 多次运行的任务, 可指定 RunID
 * * JobID: int64 - 任务所属的 JobID
 * * Extra: map[string]string - 任务运行的额外信息
 * * Operator: string - 操作者
 * <p>
 * Java实现说明:
 * - 对应Go的entity.JobRunMessage结构体
 * - 使用Java类定义，包含任务运行消息字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go map -> Java Map
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRunMessage {

  @JsonProperty("type")
  private JobRunType type;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("task_id")
  private Long taskId;

  @JsonProperty("run_id")
  private Long runId;

  @JsonProperty("job_id")
  private Long jobId;

  @JsonProperty("extra")
  private Map<String, String> extra;

  @JsonProperty("operator")
  private String operator;
}
