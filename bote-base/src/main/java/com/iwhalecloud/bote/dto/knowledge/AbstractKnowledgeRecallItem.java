package com.iwhalecloud.bote.dto.knowledge;

import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeScoreItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 知识召回项抽象类
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
public abstract class AbstractKnowledgeRecallItem {
  /** 评分 */
  protected Double score;
  /** 文档 ID */
  protected String docId;
  /** 文档名称 */
  protected String docName;
  /** 标题 */
  protected String heading;
  /** 关联文档 ID */
  protected Long fileInfoId;
  /** 文档块 ID */
  private String chunkId;

  protected AbstractKnowledgeRecallItem() {
  }

  protected AbstractKnowledgeRecallItem(KnowledgeScoreItem scoreItem) {
    // 标题格式为 "文档名称#标题链"，有的文档没有标题（比如 excel, txt），则格式为 "文档名称"
    String[] headingParts = StringUtils.split(StringUtils.defaultString(scoreItem.getData().getHeading()), "#", 2);
    this.score = scoreItem.getScore();
    this.docId = scoreItem.getData().getDocId().toString();
    this.chunkId = scoreItem.getData().getChunkId();
    this.docName = headingParts.length > 0 ? headingParts[0] : null;
    this.heading = headingParts.length == 2 ? headingParts[1] : null;
  }
}
