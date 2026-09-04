package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ImportEvaluationSetItemsResponse {
  private int successCount;
  private int failCount;
  private int totalCount;
  private List<ItemErrorGroupDTO> errors;
}
