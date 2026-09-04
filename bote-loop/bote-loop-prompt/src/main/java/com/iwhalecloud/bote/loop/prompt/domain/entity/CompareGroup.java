package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比较组实体
 * 迁移对应关系: Go语言entity.CompareGroup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareGroup {

  @JsonProperty("prompt_detail")
  private PromptDetail promptDetail;

  @JsonProperty("debug_core")
  private DebugCore debugCore;
}
