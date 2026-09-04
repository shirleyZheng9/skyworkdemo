package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验过滤器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验过滤器数据传输对象")
public class ExperimentFilterDTO {

  @Schema(description = "过滤器")
  private FiltersDTO filters;

  @Schema(description = "关键词搜索")
  private KeywordSearchDTO keywordSearch;
}
