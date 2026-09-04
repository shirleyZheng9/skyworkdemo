package com.iwhalecloud.bote.dto.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 微调模型推理出参
 *
 * @author chen.linfa
 * @since 2025-03-07
 */
@Getter
@Setter
@ToString
public class PredictResponse {
  /** 状态码，0 表示成功, 其它均为失败 */
  private String resultCode;
  /** 提示信息 */
  private String resultMsg;
  /** 信息 */
  private PredictInfo resultObject;

  @JsonIgnore
  public boolean isSuccess() {
    return "0".equals(this.resultCode);
  }

  /**
   * 流程信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PredictInfo {
    /** 命中的标签 */
    private String label;
    /** 分数 */
    private BigDecimal score;
    /** 信息 */
    private String message;
  }
}
