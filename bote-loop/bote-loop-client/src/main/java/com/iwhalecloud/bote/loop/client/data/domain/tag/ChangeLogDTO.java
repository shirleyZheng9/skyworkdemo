package com.iwhalecloud.bote.loop.client.data.domain.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变更日志数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeLogDTO {

  @JsonProperty("target")
  private ChangeTargetTypeDTO target;

  @JsonProperty("operation")
  private OperationTypeDTO operation;

  @JsonProperty("before_value")
  private String beforeValue;

  @JsonProperty("after_value")
  private String afterValue;

  @JsonProperty("target_value")
  private String targetValue;
}
