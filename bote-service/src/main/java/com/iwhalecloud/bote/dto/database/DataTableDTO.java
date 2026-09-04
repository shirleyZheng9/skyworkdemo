package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.database.DataTableEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 业务数据表 DTO
 *
 * @author wangtingyun
 * @since 2025-11-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_data_table")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTableDTO extends DataTableEntity {

  @Schema(description = "操作人名称")
  private String updatorName;
  @Schema(description = "数据源名称")
  private String dataSourceName;

  @Schema(description = "操作类型：A/M/D")
  private String actionType;
  @Schema(description = "本次变动的SQL语句")
  private String currentSql;

  @Schema(description = "表字段列表")
  @DiffField(childNode = true, parent = true)
  private List<DataTableColumnDTO> tableColumns;

}
