package com.iwhalecloud.bote.doc.common.constant;

/**
 * 异常状态码
 *
 * @author Aiqing
 * @since 2025/9/25
 */
public final class DocErrorCodeConsts {

  /**
   * 节点访问拒绝
   */
  public static final String NODE_ACCESS_DENY = "601";
  /**
   * 文档库访问拒绝
   */
  public static final String LIBRARY_ACCESS_DENY = "602";
  /**
   * 操作拒绝
   */
  public static final String OPERATE_DENY = "603";
  /**
   * 节点不存在
   */
  public static final String NODE_NOT_EXIST = "604";
  /**
   * 编辑拒绝
   */
  public static final String EDIT_DENY = "605";

  /**
   * 文档被锁定
   */
  public static final String DOCUMENT_LOCKED = "606";

  /**
   * 文档版本冲突
   */
  public static final String DOCUMENT_EDIT_VERSION_CONFLICT = "607";

  private DocErrorCodeConsts() {
  }
}
