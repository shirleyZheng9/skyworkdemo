package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 批量删除知识库文档列表
 *
 * @author auto
 * @since 2026-01-20
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DocDeleteDocumentsDTO extends TenantBaseRO {

  @Schema(description = "知识库id")
  private Long knowledgeId;

  @Schema(description = "文档列表")
  private List<Long> documentIds;

  @Schema(description = "是否删除知识库下所有文档")
  private String clrearAll =  DocBaseConsts.FALSE;

  @Schema(description = "知识库信息")
  private KnowledgeBaseDTO knowledge;

  @Schema(description = "知识库文档列表")
  private List<DocumentDTO> documentDTOS;
}
