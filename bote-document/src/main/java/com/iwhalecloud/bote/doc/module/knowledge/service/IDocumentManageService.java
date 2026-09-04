package com.iwhalecloud.bote.doc.module.knowledge.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocDeleteDocumentsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentAddParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;

/**
 * 文档管理服务
 *
 * @author auto
 * @since 2024-09-20
 */
public interface IDocumentManageService {

  /**
   * 查询单个文档
   *
   * @param tenantId 租户 ID
   * @param documentId 文档主键
   * @return 文档
   */
  DocumentDTO findDocument(Long tenantId, Long documentId);

  /**
   * 保存文档
   *
   * @param params 文档列表
   * @return 结果
   */
  ResultVO<Object> addDocument(DocumentAddParams params);

  /**
   * 保存文档
   *
   * @param params 文档列表
   * @return 结果
   */
  ResultVO<Object> addDocumentNew(DocumentAddParams params);

  /**
   * 删除文档
   *
   * @param tenantId 租户 ID
   * @param documentId 文档主键
   * @return 结果
   */
  ResultVO<Long> deleteDocument(Long tenantId, Long documentId);

  /**
   * 查询文档列表
   *
   * @param queryParams 查询条件
   * @return 文档列表
   */
  List<DocumentDTO> queryDocumentList(DocumentQueryParams queryParams);

  /**
   * 查询文档列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文档分页列表
   */
  ResultVO<PageInfo<DocumentDTO>> queryDocumentPage(DocumentQueryParams queryParams);

  /**
   * 更新 DocChain 类型的文档状态
   */
  void updateDocChainDocStatus();

  /**
   * 文档构建
   *
   * @param tenantId 租户 ID
   * @param documentId 文档ID
   * @return 结果
   */
  ResultVO<Void> buildDocument(Long tenantId, Long documentId, Boolean newDoc, String isExist);

  /**
   * 是否完成知识文档的构建
   *
   * <p>使用场景：将 API 封装成 groovy 脚本，在工作流中使用 </p>
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库 ID
   * @return 结果
   */
  boolean isFinishedBuildDocument(Long tenantId, Long knowledgeId);

  /**
   * 轮询等待文档的构建
   *
   * <p>使用场景：将 API 封装成 groovy 脚本，在工作流中使用 </p>
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库 ID
   * @param maxWaitTime 超时时间（毫秒）
   * @return 结果
   */
  boolean waitBuildDocumentFinish(Long tenantId, Long knowledgeId, int maxWaitTime);

  /**
   * 根据重新上传的文件，更新关联的知识库文档
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 文件信息 ID
   * @param fileId 文件 ID
   * @param fileName 文件名称
   */
  void rebuildDocuments(Long tenantId, Long fileInfoId, Long fileId, String fileName);

  /**
   * 通过文档库文档变动触发相关知识文档的重新构建（重新上传文档到docchain）
   * @param dcDocId 文档库文档id
   * @param tenantId 租户id
   * @return
   */
  void rebuildBtDocumentByDcDocDocId(String dcDocId, Long tenantId, Long currentLoginUserId, String onLineEditing);

  /**
   * 从fileInfo创建文档节点
   * 查询需要创建文档节点的fileInfo信息，并循环调用createUploadDocumentNode方法
   *
   * @return 结果
   */
  ResultVO<Void> createDocumentNodesFromFileInfo();

  /**
   * 查询文档信息，用于工作流知识节点展示文档信息
   *
   * @param tenantId 租户 ID
   * @param knowledgeIdExpr 知识库 ID 集合（支持引用变量，支持逗号分隔的多个 ID)
   * @return 分组文档信息
   */
  ResultVO<Map<String, Object>> queryDocumentInfo(Long tenantId, String knowledgeIdExpr);

  /**
   * 同步文档库文档被引用的知识库文档
   * @param documentId
   */
  void documentReleased(String documentId);

  ResultVO<Long> deleteDocuments(DocDeleteDocumentsDTO deleteDocumentsDTO);
}
