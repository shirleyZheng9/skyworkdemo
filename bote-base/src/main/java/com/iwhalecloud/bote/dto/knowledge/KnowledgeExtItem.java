package com.iwhalecloud.bote.dto.knowledge;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 赵旭
 * @since 2025/7/23 21:14
 */
@Getter
@Setter
@ToString
public class KnowledgeExtItem {
  private Long knowledgeId;
  /** 记录数据库名称,用于流程节点回显 */
  private String knowledgeName;
}
