package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单表字段定义
 *
 * @author chen.linfa
 * @since 2025-11-25
 */
@Getter
@Setter
@ToString
public class SimpleDataTableColumnDTO {

  @Schema(description = "表 ID")
  private Long tableId;
  @Schema(description = "字段名称")
  private String columnName;
  @Schema(description = "字段编码")
  private String columnCode;
  @Schema(description = "数据类型")
  private String dataType;
  @Schema(description = "数据长度")
  private String dataLength;
  @Schema(description = "数据精度")
  private String dataScale;
  @Schema(description = "是否主键")
  private String primaryKey;
  @Schema(description = "是否可为空")
  private String nullable;
  @Schema(description = "是否平台预置字段")
  private String platformColumn;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("columnName", columnName);
    map.put("columnCode", columnCode);
    map.put("dataType", dataType);
    map.put("dataLength", dataLength);
    map.put("dataScale", dataScale);
    map.put("primaryKey", primaryKey);
    map.put("nullable", nullable);
    map.put("platformColumn", platformColumn);
    return map;
  }
}
