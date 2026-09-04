package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 保存主题响应
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Getter
@Setter
@ToString
public class UpdataTopicResponse {
  /** 是否成功 */
  private Boolean success;
  /** 异常信息 */
  private String err;
  /** 主题信息 */
  private DocChainTopic data;

  /**
   * 主题信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocChainTopic {
    /** 主题 ID */
    private Long id;
  }
}
