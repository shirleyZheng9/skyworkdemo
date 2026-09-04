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
 * sheet页配置
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_worksheet")
@Schema(hidden = true)
public class WorksheetEntity extends BaseEntity {
  @Id
  @DiffId
  private Long id;

  @DiffField(name = "document_id")
  @Schema(description = "文档唯一标识")
  private String documentId;

  @DiffField(name = "sheet_id")
  @Schema(description = "Sheet页唯一标识")
  private String sheetId;

  @DiffField(name = "snapshot_id")
  @Schema(description = "关联workbook_top_snapshot表记录")
  private Long snapshotId;

  @DiffField(name = "name")
  @Schema(description = "Sheet页名称")
  private String name;

  @DiffField(name = "row_count")
  @Schema(description = "行数")
  private Integer rowCount;

  @DiffField(name = "column_count")
  @Schema(description = "列数")
  private Integer columnCount;

  @DiffField(name = "index")
  @Schema(description = "Sheet所在表格的顺序下标")
  private Integer index;

  @DiffField(name = "block_id")
  @Schema(description = "快照数据块记录ID")
  private Long blockId;

  @DiffField(name = "original_meta")
  @Schema(description = "存储sheet的配置")
  private String originalMeta;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
