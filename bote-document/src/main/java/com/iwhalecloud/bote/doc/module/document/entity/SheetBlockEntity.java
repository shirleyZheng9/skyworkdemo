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
 * 关联sheet的实际数据记录
 *
 * @author Aiqing
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_dc_sheet_block")
@Schema(hidden = true)
public class SheetBlockEntity extends BaseEntity {
  @Id
  @DiffId
  private Long blockId;

  @DiffField(name = "start_row")
  @Schema(description = "起始行")
  private Integer startRow;

  @DiffField(name = "end_row")
  @Schema(description = "结束行")
  private Integer endRow;

  @DiffField(name = "sheet_id")
  @Schema(description = "对应表格ID")
  private String sheetId;

  @DiffField(name = "size")
  @Schema(description = "文件大小")
  private Long size;

  @DiffField(name = "file_id")
  @Schema(description = "文件记录表ID")
  private Long fileId;

  @DiffField(name = "tenant_id")
  @Schema(description = "租户ID")
  private Long tenantId;
}
