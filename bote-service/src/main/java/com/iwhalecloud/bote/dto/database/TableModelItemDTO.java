package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.entity.database.TableModelItemEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表建模记录 DTO
 *
 * @author wangtingyun
 * @since 2025-11-30
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_table_model_item")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableModelItemDTO extends TableModelItemEntity {

  @Schema(description = "数据源编码")
  private String dataSourceCode;

}

