package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "知识库更新学习的文档区分源文档是否已经归档")
@NoArgsConstructor
public class RebuildDocumentStatusBucketDTO {
  @Schema(description = "原文档还存在")
  private List<DocumentDTO> validDocuments;
  @Schema(description = "源文档失效了")
  private List<DocumentDTO> retryDocuments;
}
