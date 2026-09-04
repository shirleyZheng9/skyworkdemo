package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "字段数据")
public class FieldDataDTO {

  @Schema(description = "字段Key")
  private String key;

  @Schema(description = "字段名称")
  private String name;

  @Schema(description = "字段内容")
  private ContentDTO content;
}
