package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class RedisPromptQuery {

  private Long promptId;
  private Boolean withCommit;
  private String commitVersion;

}
