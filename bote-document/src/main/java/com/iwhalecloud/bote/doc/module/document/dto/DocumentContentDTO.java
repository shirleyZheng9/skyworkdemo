package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文档的内容信息
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Getter
@Setter
@ToString
public class DocumentContentDTO {

  @Schema(description = "文档内容")
  private YdocContent content;
  @Schema(description = "文档内容-ydoc格式base64编码内容")
  private String ydocBase64;
  @Schema(description = "文档内容-纯文本")
  private String textContent;
  @Schema(description = "更新人")
  private Long updatorId;
  @Schema(description = "编辑模式")
  private String mode;
}
