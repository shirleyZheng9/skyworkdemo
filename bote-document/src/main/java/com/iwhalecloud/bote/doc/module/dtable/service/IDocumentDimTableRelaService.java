package com.iwhalecloud.bote.doc.module.dtable.service;

import com.iwhalecloud.bote.doc.module.dtable.entity.DocumentDimTableRelaEntity;

/**
 * 文档与多维表格关联关系服务接口
 *
 * @author auto
 * @since 2026-01-09
 */
public interface IDocumentDimTableRelaService {


  /**
   * 创建文档关联多维表格
   * 1. 调用多维表格服务创建多维表格空间+默认的多维表格
   * 2. 保存文档关联关系
   *
   * @param documentId 文档ID
   * @param userId 当前操作用户ID
   */
  void createDimTable(String documentId, Long userId);

  /**
   * 根据文档ID查询
   *
   * @param documentId 文档唯一编码
   * @return 关联关系实体
   */
  DocumentDimTableRelaEntity findByDocumentId(String documentId);

  /**
   * 根据文档ID逻辑删除
   *
   * @param documentId 文档唯一编码
   * @return 是否删除成功
   */
  boolean deleteByDocumentId(String documentId);

}
