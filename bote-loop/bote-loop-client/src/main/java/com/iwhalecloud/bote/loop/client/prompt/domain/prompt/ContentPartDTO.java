package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容部分DTO
 * 迁移对应关系: Thrift struct ContentPart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "内容部分DTO")
public class ContentPartDTO {

  @Schema(description = "内容类型")
  private ContentTypeDTO type;

  @Schema(description = "文本内容")
  private String text;

  @Schema(description = "图片URL")
  private ImageURLDTO imageUrl;
}
