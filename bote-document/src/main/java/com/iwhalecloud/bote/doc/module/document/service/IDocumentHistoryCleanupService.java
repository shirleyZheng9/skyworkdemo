package com.iwhalecloud.bote.doc.module.document.service;

/**
 * 文档历史记录清理服务接口
 *
 * @author lizuyin
 * @since 2025-10-16
 */
public interface IDocumentHistoryCleanupService {

  /**
   * 清理文档历史记录
   * 包括文档内容历史、工作簿内容历史、文档导出快照
   */
  void cleanupDocumentHistory();
}
