package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 音频数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "音频数据传输对象")
public class AudioDTO {

  @Schema(description = "格式")
  private String format;

  @Schema(description = "URL")
  private String url;
}
