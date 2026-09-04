package com.iwhalecloud.bote.doc.consts;

/**
 * 文档模块缓存相关常量
 *
 * @author lizuyin
 * @since 2025/08/19
 */
public final class DocCacheConsts {
  /** 缓存分组: 文档模块 */
  public static final String GROUP_DOC = "bote-doc";
  public static final String CACHE_KEY_SPLIT = ":";
  /** 缓存名称: 置顶文档 */
  public static final String CACHE_NAME_PINNED_DOCUMENT = "pinnedDocument";
  /** 缓存名称: 置顶文档库 */
  public static final String CACHE_NAME_PINNED_LIBRARY = "pinnedLibrary";
  /** 缓存名称: 置顶知识库 */
  public static final String CACHE_NAME_PINNED_KNOWLEDGE = "pinnedKnowledge";
  /**
   * 缓存名称：文档库文档的路径
   */
  public static final String CACHE_PREFIX_DOCUMENT_PATH = "documentPath:";
  /** 缓存名称: 租户设置 */
  public static final String CACHE_NAME_TENANT_SETTING = "tenantSetting";
  /** 缓存名称: 知识库 */
  public static final String CACHE_NAME_KNOWLEDGE = "knowledge";
  /** 缓存名称: 文件分片上传 */
  public static final String CACHE_NAME_FILE_CHUNK_UPLOAD = "fileChunkUpload";
  /** 缓存名称: 文件夹上传进度 */
  public static final String CACHE_NAME_FOLDER_UPLOAD = "folderUpload";
  /**
   * 缓存名称：socket服务
   */
  public static final String CACHE_PREFIX_SOCKET_SERVER = "docSocketServer:";
  /**
   * 缓存名称：在线表格
   */
  public static final String CACHE_PREFIX_WORKBOOK = "docWorkbook:";
  /**
   * 文档下载相关的缓存前缀
   */
  public static final String CACHE_DOCUMENT_DOWNLOAD_PREFIX = "document_download:";
  /**
   * 文档的缓存前缀
   */
  public static final String CACHE_DOCUMENT_NODE_PREFIX = "document_node:";
  /**
   * 文档附件的访问权限检查结果缓存前缀
   */
  public static final String CACHE_PREFIX_DOCUMENT_ATTACHMENT = "document_attachment:";
  /**
   * 文档库缓存前缀
   */
  public static final String CACHE_PREFIX_DOCUMENT_LIBRARY = "document_library:";

  private DocCacheConsts() {
  }

}
