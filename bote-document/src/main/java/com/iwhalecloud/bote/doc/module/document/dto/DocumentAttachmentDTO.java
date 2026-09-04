package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档附件dto
 *
 * @author Aiqing
 * @since 2025/9/9
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class DocumentAttachmentDTO extends FileInfoVO {

  @Serial
  private static final long serialVersionUID = 1L;

  @Schema(description = "访问URL")
  private String url;

  @Schema(description = "附件ID")
  private Long attachmentId;

  @Schema(description = "所属文档ID")
  private String documentId;
  @Schema(description = "租户ID")
  private Long tenantId;
}
