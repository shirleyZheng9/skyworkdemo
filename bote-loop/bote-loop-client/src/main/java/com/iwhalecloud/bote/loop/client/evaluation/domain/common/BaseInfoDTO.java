package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "基础信息数据传输对象")
public class BaseInfoDTO {

  @Schema(description = "创建者")
  private UserInfoDTO createdBy;

  @Schema(description = "更新者")
  private UserInfoDTO updatedBy;

  @Schema(description = "创建时间")
  private Long createdAt;

  @Schema(description = "更新时间")
  private Long updatedAt;

  @Schema(description = "删除时间")
  private Long deletedAt;
}
