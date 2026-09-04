package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 问答引用片段表 Entity
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_DC_QA_CHUNK_REFERENCE")
public class BtDcQaChunkReferenceEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long referenceId;
  @DiffField(name = "QA_ID")
  @Schema(description = "问答记录ID")
  private Long qaId;
  @DiffField(name = "DOC_ID")
  @Schema(description = "知识库文档ID")
  private Long docId;
  @DiffField(name = "kb_id")
  @Schema(description = "知识库ID")
  private Long kbId;
  @DiffField(name = "REFERENCE_TYPE")
  @Schema(description = "引用类型，doc,image")
  private String referenceType;
  @DiffField(name = "CHUNK_ID")
  @Schema(description = "知识片段ID")
  private String chunkId;
  @DiffField(name = "CHUNK_CONTENT")
  @Schema(description = "片段内容")
  private String chunkContent;
  @DiffField(name = "CHUNK_TITLE")
  @Schema(description = "片段标题")
  private String chunkTitle;
  @DiffField(name = "RELEVANCE_SCORE")
  @Schema(description = "相关性评分")
  private BigDecimal relevanceScore;
  @DiffField(name = "DOCUMENT_NAME")
  @Schema(description = "文档名称")
  private String documentName;
  @DiffField(name = "DOCUMENT_URL")
  @Schema(description = "文档路径")
  private String documentUrl;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
