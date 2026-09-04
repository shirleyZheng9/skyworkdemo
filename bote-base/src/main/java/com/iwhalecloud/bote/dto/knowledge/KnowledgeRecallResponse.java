package com.iwhalecloud.bote.dto.knowledge;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识召回响应
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
public class KnowledgeRecallResponse {
  /** 文本列表 */
  private List<KnowledgeRecallTextItem> text;
  /** 图片列表 */
  private List<KnowledgeRecallImageItem> image;
  /** chatExcel 类型知识库召回内容 */
  private List<Map<String, Object>> data;
}
