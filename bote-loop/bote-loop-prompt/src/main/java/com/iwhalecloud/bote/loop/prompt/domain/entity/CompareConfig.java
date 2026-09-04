package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比较配置实体
 * 迁移对应关系: Go语言entity.CompareConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareConfig {

  @JsonProperty("groups")
  private List<CompareGroup> groups;

}
