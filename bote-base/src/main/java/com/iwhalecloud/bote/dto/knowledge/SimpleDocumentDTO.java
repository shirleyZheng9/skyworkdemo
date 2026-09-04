package com.iwhalecloud.bote.dto.knowledge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档简单信息
 *
 * @author qian.sisheng
 * @since 2024-09-26
 */
@Getter
@Setter
@ToString
public class SimpleDocumentDTO {
  @Schema(description = "文档 ID")
  private Long documentId;
  @Schema(description = "文档名称")
  private String docName;
  @Schema(description = "关联文件 ID")
  private Long fileInfoId;
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @Schema(description = "新文档表主键标识")
  private String dcDocumentId;
  @Schema(description = "文档库主键标识")
  private String libraryId;
  @Schema(description = "知识库主键标识")
  private Long knowledgeId;
  @Schema(description = "标记是否已勾选")
  private Boolean mark;
}
