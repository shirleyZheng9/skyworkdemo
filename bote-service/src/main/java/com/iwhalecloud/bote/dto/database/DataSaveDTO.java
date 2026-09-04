package com.iwhalecloud.bote.dto.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 测试数据保存 DTO
 *
 * @author wangtingyun
 * @since 2025-11-26
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSaveDTO {

  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "主键")
  private Long tableId;
  @Schema(description = "数据列表")
  private List<Map<String, Object>> dataList;
  @Schema(description = "旧数据列表")
  private List<Map<String, Object>> oldDataList;

}
