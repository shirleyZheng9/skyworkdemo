package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查实验名称响应
 * 对应Go: expt.CheckExperimentNameResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckExperimentNameResponse {

  /**
   * 是否通过
   * 对应Go: Pass *bool
   */
  @Schema(description = "是否通过")
  private Boolean pass;

  /**
   * 消息
   * 对应Go: Message *string
   */
  @Schema(description = "消息")
  private String message;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
