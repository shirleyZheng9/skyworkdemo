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
 * 表建模记录 entity
 *
 * @author wangtingyun
 * @since 2025-11-30
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_table_model_item")
public class TableModelItemEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键ID")
  private Long itemId;

  @DiffField(name = "TABLE_ID")
  @Schema(description = "表ID")
  private Long tableId;

  @DiffField(name = "CHANGE_SQL")
  @Schema(description = "变更脚本")
  private String changeSql;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

}

