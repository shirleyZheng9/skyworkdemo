package com.iwhalecloud.bote.dto.a2a;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.a2a.spec.TaskState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * A2A 任务信息
 *
 * @author bianjp
 * @since 2025-09-12
 */
@Getter
@Setter
@ToString
public class A2aTaskInfo {
  /** 缓存 key */
  @JsonIgnore
  private String cacheKey;
  /** 任务 ID */
  @Nullable
  private String taskId;
  /** 上下文 ID */
  @Nullable
  @JsonIgnore
  private String contextId;
  /** 任务状态 */
  @Nullable
  private TaskState taskState;
}
