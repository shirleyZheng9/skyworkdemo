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
 * 工作簿内容表
 *
 * @author system
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_workbook_content")
@Schema(hidden = true)
public class WorkbookContentEntity extends BaseEntity {

  @Id
  @DiffId
  @Schema(description = "主键ID")
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档ID")
  private String documentId;

  @DiffField(name = "content")
  @Schema(description = "内容")
  private String content;

  @DiffField(name = "revision")
  @Schema(description = "版本号")
  private Long revision;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
