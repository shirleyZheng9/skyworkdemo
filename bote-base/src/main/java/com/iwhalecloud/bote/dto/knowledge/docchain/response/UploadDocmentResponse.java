package com.iwhalecloud.bote.dto.knowledge.docchain.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档上传响应
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Getter
@Setter
@ToString
public class UploadDocmentResponse {
  /** 文档列表 */
  private List<DocmentFileSrc> fileSrc;

  /**
   * 主题下的文档信息
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocmentFileSrc {
    /** 文档 ID */
    private Long id;
    /** 文件名称 */
    private String path;
  }
}
