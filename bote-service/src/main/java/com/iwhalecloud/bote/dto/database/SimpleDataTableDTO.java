package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 简单表定义
 *
 * @author chen.linfa
 * @since 2025-11-25
 */
@Getter
@Setter
@ToString
public class SimpleDataTableDTO {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "数据源 ID")
  private Long dataSourceId;
  @Schema(description = "表 ID")
  private Long tableId;
  @Schema(description = "表名称")
  private String tableName;
  @Schema(description = "表编码")
  private String tableCode;
  @Schema(description = "字段列表")
  private List<SimpleDataTableColumnDTO> columns;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("dataSourceId", dataSourceId);
    map.put("tableId", tableId);
    map.put("tableName", tableName);
    map.put("tableCode", tableCode);
    map.put("columns", CollectionUtils.isEmpty(columns) ? null : JsonUtil.toJsonString(columns));
    return map;
  }
}
