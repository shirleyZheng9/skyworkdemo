package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务日志数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLogDTO {

  @JsonProperty("content")
  private String content;

  @JsonProperty("level")
  private String level;

  @JsonProperty("timestamp")
  private Long timestamp;

  @JsonProperty("hidden")
  private Boolean hidden;
}
