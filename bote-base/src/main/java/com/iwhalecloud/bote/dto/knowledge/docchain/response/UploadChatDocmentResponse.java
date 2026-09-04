package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 对话文档上传响应
 *
 * @author chen.linfa
 * @since 2025-03-19
 */
@Getter
@Setter
@ToString
public class UploadChatDocmentResponse {

  /** 是否成功 */
  private Boolean success;
  /** 异常信息 */
  private String err;
  /** 文档列表 */
  private List<DocmentFile> data;

  /**
   * 文档信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocmentFile {
    /** 文档 ID */
    private Long id;
  }
}
