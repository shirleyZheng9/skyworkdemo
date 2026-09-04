package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.QuotaSpaceExpt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaUpdateResult {

  private QuotaSpaceExpt quotaSpaceExpt;
  private boolean update;

}
