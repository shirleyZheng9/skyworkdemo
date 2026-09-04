package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全量导入响应（同步返回 created/updated/failed）
 *
 * @author skill-square
 * @since 2026-03-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "全量导入响应")
public class ImportAllResponse {

  @Schema(description = "新增数量")
  private Integer created;
  @Schema(description = "更新数量")
  private Integer updated;
  @Schema(description = "失败数量")
  private Integer failed;
}
