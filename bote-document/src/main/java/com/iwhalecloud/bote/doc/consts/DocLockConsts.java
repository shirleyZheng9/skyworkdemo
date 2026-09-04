package com.iwhalecloud.bote.doc.consts;

/**
 *
 * @author Aiqing
 * @since 2025/8/21
 */
public final class DocLockConsts {

  /**
   * 文档节点删除锁定key
   */
  public static final String DOCUMENT_DELETE_NODE_LOCK = "document_delete_node_lock";

  /**
   * 文档节点移动锁定key
   */
  public static final String DOCUMENT_MOVE_NODE_LOCK = "document_move_node_lock";

  /**
   * 在线文档内容保存锁
   */
  public static final String DOCUMENT_CONTENT_SAVE_LOCK = "document_content_save_lock";

  /**
   * 文件夹结构创建锁
   */
  public static final String FOLDER_STRUCTURE_CREATE_LOCK = "folder_structure_create_lock";

  /**
   * 文件夹上传缓存锁
   */
  public static final String FOLDER_UPLOAD_CACHE_LOCK = "folder_upload_cache_lock";
  /**
   * 文件上传缓存锁
   */
  public static final String FILE_UPLOAD_CACHE_LOCK = "file_upload_cache_lock";

  /**
   * 文档权限变更
   */
  public static final String DOCUMENT_PERMISSION_SAVE_LOCK = "document_permission_save_lock";

  /**
   * 首页置顶项排序锁
   */
  public static final String HOMEPAGE_PIN_REORDER_LOCK = "homepage_pin_reorder_lock";

  /**
   * 知识库新建（按租户、空间、名称串行化，避免并发同名穿透校验）
   */
  public static final String KNOWLEDGE_BASE_CREATE_LOCK = "knowledge_base_create_lock";

  private DocLockConsts() {
    throw new IllegalStateException();
  }

}
