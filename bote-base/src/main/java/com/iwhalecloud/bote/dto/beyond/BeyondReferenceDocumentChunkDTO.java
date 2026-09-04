package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应文档块
 *
 * @author bianjp
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class BeyondReferenceDocumentChunkDTO {
  /** 类型 */
  private String type;
  /** 知识库 ID */
  private String id;
  /** 文档 ID */
  private String documentId;
  /** 文档名称 */
  private String title;
  /** 文档内容 */
  private String content;
}
