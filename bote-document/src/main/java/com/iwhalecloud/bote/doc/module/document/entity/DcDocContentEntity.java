package com.iwhalecloud.bote.doc.module.document.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线文本文档内容表
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_doc_content")
@Schema(hidden = true)
public class DcDocContentEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一标识")
  private String documentId;

  @DiffField(name = "content")
  @Schema(description = "文档内容json")
  private String content;

  @DiffField(name = "ydoc")
  @Schema(description = "文档yjs格式二进制内容")
  private String ydoc;

  @DiffField(name = "text_content")
  @Schema(description = "文档文本内容")
  private String textContent;

  @DiffField(name = "updator_id")
  @Schema(description = "修改人ID")
  private Long updatorId;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;

}
