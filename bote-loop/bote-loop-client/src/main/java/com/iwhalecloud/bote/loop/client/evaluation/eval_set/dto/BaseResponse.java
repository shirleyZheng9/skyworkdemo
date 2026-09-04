package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础响应DTO
 * 用于只有 BaseResp 的响应
 */
@Data
@NoArgsConstructor
@Schema(description = "基础响应")
public class BaseResponse {

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}

