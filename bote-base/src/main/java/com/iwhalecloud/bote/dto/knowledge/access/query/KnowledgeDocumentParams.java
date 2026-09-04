package com.iwhalecloud.bote.dto.knowledge.access.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库文档查询参数
 *
 * @author lxs
 * @since 2025/7/12
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeDocumentParams {

  @Schema(description = "知识库类型")
  private String knowledgeType;

  @Schema(description = "文档ID")
  private Long docId;

  @Schema(description = "文档名称")
  private String docName;

}
