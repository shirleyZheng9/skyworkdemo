package com.iwhalecloud.bote.dto.knowledge;

import com.iwhalecloud.bote.dto.knowledge.SearchKnowledgeResponse.KnowledgeScoreItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回的图片项
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class KnowledgeRecallImageItem extends AbstractKnowledgeRecallItem {
  /** 图片路径，不是完整链接 */
  private String path;

  public KnowledgeRecallImageItem() {
    super();
  }

  public KnowledgeRecallImageItem(KnowledgeScoreItem scoreItem) {
    super(scoreItem);
    this.path = scoreItem.getData().getUrl();
  }
}
