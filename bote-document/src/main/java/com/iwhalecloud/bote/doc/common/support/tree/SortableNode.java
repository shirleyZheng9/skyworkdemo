package com.iwhalecloud.bote.doc.common.support.tree;

/**
 *
 * @author Aiqing
 * @since 2025/9/8
 */
public interface SortableNode {

  /**
   * 当前节点ID
   *
   * @return 节点ID
   */
  String getNodeId();

  /**
   * 前置节点ID
   *
   * @return 节点ID
   */
  String getPreNodeId();
}
