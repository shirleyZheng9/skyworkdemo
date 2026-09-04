package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "排序数据传输对象")
public class OrderByDTO {

  @Schema(description = "排序字段")
  private String field;

  @Schema(description = "是否升序")
  private Boolean isAsc;
}
