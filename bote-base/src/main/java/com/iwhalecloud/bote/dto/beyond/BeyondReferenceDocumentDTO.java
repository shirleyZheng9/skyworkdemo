package com.iwhalecloud.bote.dto.beyond;

import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应关联文档信息
 *
 * @author bianjp
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class BeyondReferenceDocumentDTO {
  /** 类型(DATASET: 知识库文档, ON_LINE: 联网检索文档, AGENT: 智能体输出文档) */
  private String type;
  /** 知识库 ID */
  private String id;
  /** 文档 ID */
  private String documentId;
  /** 文档链接 */
  private String documentUrl;
  /** 文档名称 */
  private String title;
  /** 文档内容 */
  private String content;
  /** 文档块列表 */
  private List<BeyondReferenceDocumentChunkDTO> chunkList;

  /**
   * 转为标准的参考文档对象
   */
  public ReferenceDocumentDTO convert() {
    ReferenceDocumentDTO reference = new ReferenceDocumentDTO();
    reference.setId(documentId);
    reference.setType("doc");
    reference.setName(title);
    reference.setBeyondDocument(this);
    return reference;
  }
}
