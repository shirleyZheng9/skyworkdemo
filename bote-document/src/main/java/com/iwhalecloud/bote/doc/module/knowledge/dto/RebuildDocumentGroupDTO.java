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
@Schema(description = "知识库更新学习的文档区分老知识库的还是新的")
@NoArgsConstructor
public class RebuildDocumentGroupDTO {
  @Schema(description = "老知识库文档")
  private List<DocumentDTO> oldDocuments;
  @Schema(description = "新知识库文档")
  private List<DocumentDTO> newDocuments;
}
