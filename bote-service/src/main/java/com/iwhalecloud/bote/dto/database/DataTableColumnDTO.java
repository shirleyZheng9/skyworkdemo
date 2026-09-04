package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.database.DataTableColumnEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 业务数据表字段 DTO
 *
 * @author wangtingyun
 * @since 2025-11-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_table_column")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTableColumnDTO extends DataTableColumnEntity {

  @Schema(description = "所属表名称")
  private String tableName;
  @Schema(description = "表编码")
  private String tableCode;
  @Schema(description = "默认值")
  private String defaultValue;
  @Schema(description = "唯一索引编码")
  private String uniqueIndexCode;

}

