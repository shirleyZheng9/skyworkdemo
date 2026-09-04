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
 * 表格数据的快照
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_workbook_snapshot")
@Schema(hidden = true)
public class WorkbookSnapshotEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一标识")
  private String documentId;

  @DiffField(name = "revision")
  @Schema(description = "快照版本号")
  private Long revision;

  @DiffField(name = "sheet_count")
  @Schema(description = "快照生成时的sheet数量")
  private Integer sheetCount;

  @DiffField(name = "original_data")
  @Schema(description = "存储单元格样式")
  private String originalData;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
