package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.support.tree.SortableNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 */
@Getter
@Setter
@ToString
public class DocumentNodeTreeDTO implements SortableNode {

  /**
   * 节点ID
   */
  private String nodeId;

  /**
   * 节点类型
   */
  private String nodeType;

  /**
   * 父节点ID
   */
  private String parentId;

  /**
   * 前置节点ID
   */
  private String preNodeId;


}
