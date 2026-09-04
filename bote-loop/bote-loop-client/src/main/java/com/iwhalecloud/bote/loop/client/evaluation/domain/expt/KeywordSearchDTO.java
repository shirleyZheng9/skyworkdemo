package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关键词搜索数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "关键词搜索数据传输对象")
public class KeywordSearchDTO {

  @Schema(description = "关键词")
  private String keyword;

  @Schema(description = "过滤字段列表")
  private List<FilterFieldDTO> filterFields;
}
