package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "内容数据传输对象")
public class ContentDTO {

  @Schema(description = "内容类型")
  private String contentType;

  @Schema(description = "格式")
  private FieldDisplayFormatDTO format;

  @Schema(description = "文本内容")
  private String text;

  @Schema(description = "图片内容")
  private ImageDTO image;

  @Schema(description = "多部分内容")
  private List<ContentDTO> multiPart;

  @Schema(description = "音频内容")
  private AudioDTO audio;
}
