package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列评估集字段数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列评估集字段数据传输对象")
public class ColumnEvalSetFieldDTO {

  @Schema(description = "字段键")
  private String key;

  @Schema(description = "字段名称")
  private String name;

  @Schema(description = "字段描述")
  private String description;

  @Schema(description = "内容类型")
  private ContentTypeDTO contentType;

  @Schema(description = "文本模式")
  private String textSchema;
}
