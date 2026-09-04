package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文件预览DTO
 *
 * @author Aiqing
 * @since 2025/9/8
 */
@Getter
@Setter
@ToString
public class FilePreviewDTO {

  @Schema(description = "预览URL")
  private String previewUrl;
  @Schema(description = "文件的访问地址")
  private String fileUrl;
  @Schema(description = "是否需要预览转换")
  private Boolean needConvert;

  @Schema(description = "是否支持预览")
  private Boolean supportPreview;
  @Schema(description = "文件名称")
  private String fileName;
  @Schema(description = "文件大小")
  private Long fileSize;
}
