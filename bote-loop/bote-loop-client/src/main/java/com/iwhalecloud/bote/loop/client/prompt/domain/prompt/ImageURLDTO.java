package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片URL DTO
 * 迁移对应关系: Thrift struct ImageURL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "图片URL DTO")
public class ImageURLDTO {

  @Schema(description = "URI")
  private String uri;

  @Schema(description = "URL")
  private String url;
}
