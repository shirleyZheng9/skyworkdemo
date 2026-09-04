package com.iwhalecloud.bote.entity.database;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 业务数据表字段 entity
 *
 * @author wangtingyun
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_table_column")
public class DataTableColumnEntity extends BaseEntity {

  @DiffId
  @Schema(description = "字段主键ID")
  private Long tableColumnId;

  @DiffField(name = "TABLE_ID", parent = true)
  @Schema(description = "表ID")
  private Long tableId;

  @DiffField(name = "COLUMN_CODE")
  @Schema(description = "字段编码")
  private String columnCode;

  @DiffField(name = "COLUMN_NAME")
  @Schema(description = "字段名称")
  private String columnName;

  @DiffField(name = "DATA_TYPE")
  @Schema(description = "数据类型")
  private String dataType;

  @DiffField(name = "DATA_LENGTH")
  @Schema(description = "数据长度")
  private Long dataLength;

  @DiffField(name = "DATA_SCALE")
  @Schema(description = "数据精度")
  private Long dataScale;

  @DiffField(name = "PRIMARY_KEY")
  @Schema(description = "是否主键")
  private String primaryKey;

  @DiffField(name = "NULLABLE")
  @Schema(description = "是否可为空")
  private String nullable;

  @DiffField(name = "PLATFORM_COLUMN")
  @Schema(description = "是否为平台字段")
  private String platformColumn;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField
  @Schema(description = "是否自动创建序列")
  private String createSeq;

}

