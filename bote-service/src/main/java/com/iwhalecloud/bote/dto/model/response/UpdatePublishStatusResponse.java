package com.iwhalecloud.bote.dto.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型微调评测状态更新出参
 *
 * @author chen.linfa
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString
public class UpdatePublishStatusResponse {
  /** 状态码，0 表示成功, 其它均为失败 */
  private String resultCode;
  /** 提示信息 */
  private String resultMsg;
  /** 信息 */
  private PublishInfo resultObject;

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
  public static class PublishInfo {
    /** 流水号 */
    private Long publishId;

    /** 微调文件路径 */
    private String filePath;
    /** 微调文件类型 */
    private String fileServer;

    /** 评测准确率 */
    private BigDecimal accuracy;
  }
}
