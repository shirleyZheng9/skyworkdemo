package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提交实验请求
 * 对应Go: expt.SubmitExperimentRequest
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
@Schema(description = "提交实验请求")
public class SubmitExperimentRequest extends CreateExperimentRequest {

  /**
   * 扩展信息
   * 对应Go: Ext map[string]string
   */
  @Schema(description = "扩展信息")
  private Map<String, String> ext;
}
