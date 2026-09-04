package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentInfoDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocContentEntity;
import java.util.List;

/**
 * 在线文档内容Service
 *
 * @author Aiqing
 * @since 2025/8/30
 */
public interface IDocContentService {


  /**
   * 查询最后一个版本历史
   *
   * @param documentId 文档ID
   * @return 版本历史
   */
  DocContentHistoryDTO selectLastDocHistory(String documentId);

  /**
   * 保存在线文档的历史
   *
   * @param documentId 文档ID
   * @param content 内容
   * @param updatorId 更新人
   */
  void saveContentHistory(String documentId, String content, Long updatorId);

  /**
   * 保存在线文档的内容
   *
   * @param documentId 文档ID
   * @param contentDTO 内容信息
   * @return 是否保存成功
   */
  boolean saveDocContent(String documentId, DocumentContentDTO contentDTO);

  /**
   * 查询在线文档内容
   *
   * @param documentId 文档ID
   * @return 文档信息
   */
  OnlineDocumentInfoDTO findContentByDocumentId(String documentId);

  /**
   * 初始化在线文档内容
   *
   * @param documentId 文档ID
   * @return 文档内容实体
   */
  DcDocContentEntity initDocContentEntity(String documentId);

  /**
   * 获取在线文档历史版本列表
   * @param documentId 文档id
   * @return 返回历史版本列表
   */
  List<DocContentHistoryInfoDTO> getDocumentContentVersions(String documentId);

  /**
   * 使用指定版本列表
   * @param documentId 文档id
   * @param id 历史id
   */
  void restoreContentVersion(String documentId, Long id);

  /**
   * 查询历史版本详情
   * @param documentId 文档id
   * @param id 历史id
   * @return 返回详情
   */
  DocContentHistoryDTO getDocumentContentVersion(String documentId, Long id);
}
