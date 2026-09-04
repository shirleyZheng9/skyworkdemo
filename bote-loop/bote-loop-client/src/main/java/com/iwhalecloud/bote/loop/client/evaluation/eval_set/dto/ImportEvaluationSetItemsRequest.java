package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.DatasetIOJobOptionDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset_job.FieldMappingDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Schema(description = "本地导入评测集")
public class ImportEvaluationSetItemsRequest {
  @Schema(description = "评测集ID")
  private Long evaluationSetId;
  @Schema(description = "列映射")
  private List<FieldMappingDTO> fieldMappings;
  @Schema(description = "导入方式 overwriteDataset=true:全量覆盖;false:添加数据")
  private DatasetIOJobOptionDTO option;
  @Schema(description = "租户ID")
  protected Long spaceId;
}
