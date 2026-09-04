package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试配置实体
 * 迁移对应关系: Go语言entity.DebugConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugConfig {

  @JsonProperty("single_step_debug")
  private Boolean singleStepDebug;

}
