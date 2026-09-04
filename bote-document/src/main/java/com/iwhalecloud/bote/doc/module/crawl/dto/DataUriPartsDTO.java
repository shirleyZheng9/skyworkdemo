package com.iwhalecloud.bote.doc.module.crawl.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * data URI 解析结果 DTO（从 {@code DataUriParts} 调整而来）
 */
@Getter
@Setter
@ToString
public class DataUriPartsDTO {

  @Schema(description = "MIME 类型（如 image/svg+xml）")
  private String mimeType;

  @Schema(description = "data URI 的数据部分（逗号后的内容，非 base64）")
  private String data;
}

