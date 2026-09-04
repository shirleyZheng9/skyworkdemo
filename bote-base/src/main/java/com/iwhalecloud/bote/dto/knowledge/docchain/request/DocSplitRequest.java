package com.iwhalecloud.bote.dto.knowledge.docchain.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档块拆分请求
 *
 * @author qian.sisheng
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString
public class DocSplitRequest {
  /** 文档块ID */
  private String topicId;
  /** 文档块内容 */
  private String content;
  /** 文档块标题 */
  private String title;
  /** 是否转换成PDF */
  private Boolean convertPdf;
  /** 是否生成摘要 */
  private Boolean summary;
  /** PDF拆分模式 */
  private String pdfSplitModel;
}
