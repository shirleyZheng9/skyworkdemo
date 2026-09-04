package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数模式数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "参数模式数据传输对象")
public class ArgsSchemaDTO {

  @Schema(description = "键")
  private String key;

  @Schema(description = "支持的内容类型列表")
  private List<String> supportContentTypes;

  @Schema(description = "JSON模式")
  private String jsonSchema;
}
