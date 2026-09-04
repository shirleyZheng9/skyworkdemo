package com.iwhalecloud.bote.dto.knowledge;

import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeScoreItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回的文本项
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeRecallTextItem extends AbstractKnowledgeRecallItem {
  /** 内容 */
  private String content;

  public KnowledgeRecallTextItem() {
    super();
  }

  public KnowledgeRecallTextItem(KnowledgeScoreItem scoreItem) {
    super(scoreItem);
    this.content = scoreItem.getData().getContent();
  }
}
