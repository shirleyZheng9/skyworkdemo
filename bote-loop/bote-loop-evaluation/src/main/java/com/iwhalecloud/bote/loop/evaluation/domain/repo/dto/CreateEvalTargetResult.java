package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEvalTargetResult {

  private Long id;

  private Long versionId;

}
