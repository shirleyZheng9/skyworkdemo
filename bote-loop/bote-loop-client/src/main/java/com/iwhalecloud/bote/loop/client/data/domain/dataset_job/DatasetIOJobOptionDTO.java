package com.iwhalecloud.bote.loop.client.data.domain.dataset_job;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "导入方式")
public class DatasetIOJobOptionDTO {
  @Schema(description = "true:全量覆盖;false:添加数据")
  private Boolean overwriteDataset;
}
