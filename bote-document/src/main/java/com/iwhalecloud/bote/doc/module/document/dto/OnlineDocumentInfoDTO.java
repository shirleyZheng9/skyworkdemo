package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * tiptap 文档信息
 *
 * @author Aiqing
 * @since 2025/8/26
 */
@Getter
@Setter
@ToString
public class OnlineDocumentInfoDTO extends DocumentContentDTO {

  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "更新时间")
  private Date updatedTime;
  @Schema(description = "最后更新人")
  private String updatorName;
}
