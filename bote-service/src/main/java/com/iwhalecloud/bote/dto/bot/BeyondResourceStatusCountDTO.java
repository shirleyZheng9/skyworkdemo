package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应平台资源状态统计DTO
 *
 * @author lizuyin
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
@Schema(description = "百应平台资源状态统计DTO")
public class BeyondResourceStatusCountDTO {
  @Schema(description = "未上架数量（status = 0）")
  private Long review;
  @Schema(description = "已上架数量（status = 1）")
  private Long shelf;
  @Schema(description = "已下架数量（status = 2）")
  private Long offShelf;
}

