package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "图片数据传输对象")
public class ImageDTO {

  @Schema(description = "名称")
  private String name;

  @Schema(description = "URL")
  private String url;

  @Schema(description = "URI")
  private String uri;

  @Schema(description = "缩略图URL")
  private String thumbUrl;
}
