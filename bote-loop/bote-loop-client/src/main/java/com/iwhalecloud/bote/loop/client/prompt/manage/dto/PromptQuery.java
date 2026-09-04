package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt查询DTO
 * 对应Thrift: PromptQuery
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptQuery {

  @JsonProperty("prompt_id")
  private Long promptId;

  @JsonProperty("tenant_id")
  private Long tenantId;

  @JsonProperty("with_commit")
  private Boolean withCommit;

  @JsonProperty("commit_version")
  private String commitVersion;
}
