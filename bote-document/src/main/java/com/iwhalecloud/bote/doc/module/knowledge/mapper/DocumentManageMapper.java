package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DoKbIDAndDocIdDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleDocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleLibDocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateDocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档管理
 *
 * @author auto
 * @since 2024-09-20
 */
public interface DocumentManageMapper {

  /**
   * 根据主键获取文档
   */
  DocumentDTO getDocument(@Param("tenantId") Long tenantId, @Param("id") Long documentId);

  /**
   * 新增文档
   *
   * @param document 文档
   * @return 结果
   */
  int insertDocument(@Param("dto") DocumentDTO document);

  /**
   * 批量新增文档
   *
   * @param documents 文档列表
   * @return 结果
   */
  int batchInsertDocument(@Param("list") List<DocumentDTO> documents);

  /**
   * 修改文档
   *
   * @param document 文档
   * @return 结果
   */
  int updateDocument(@Param("dto") DocumentDTO document);

  /**
   * 删除文档
   *
   * @param tenantId 租户 ID
   * @param documentId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteDocument(@Param("tenantId") Long tenantId, @Param("documentId") Long documentId, @Param("updatorId") Long updatorId);

  /**
   * 统计文档数量
   */
  int countDocuments(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 获取文档列表
   *
   * @param queryParams 查询条件
   * @return 文档列表
   */
  List<DocumentDTO> selectDocumentList(@Param("query") DocumentQueryParams queryParams);

  /**
   * 获取文档列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文档分页列表
   */
  Page<DocumentDTO> selectDocumentPage(@Param("query") DocumentQueryParams queryParams, RowBounds rowBounds);

  /**
   * 更新文档状态和处理时间
   *
   * <p>RAG 构建文档功能使用</p>
   */
  int updateDocStatusAndProcessTime(@Param("tenantId") Long tenantId, @Param("doc") UpdateDocumentDTO document);

  /**
   * 更新知识库下所有文档状态
   */
  int updateDocStatusByKnowledgeId(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId, @Param("docStatus") String docStatus,
    @Param("updatorId") Long updatorId);

  /**
   * 查询 DocChain 类型处于解析中状态的文档
   *
   * @param minUpdatedTime 最小更新时间，避免重复处理太久没更新的文档
   * @return 文档列表
   */
  List<DocumentDTO> selectAnalyzingDocumentsOfDocChain(@Param("minUpdatedTime") Date minUpdatedTime);

  /**
   * 根据文件ID查询文档列表
   *
   * @param fileInfoId 文件ID
   * @return 文档列表
   */
  List<DocumentDTO> selectDocumentsByFileInfoId(@Param("tenantId") Long tenantId, @Param("fileInfoId") Long fileInfoId);

  /**
   * 查询临时的文档，用于清理
   *
   * @param date 截止时间
   * @return 文档列表
   */
  List<DocumentDTO> selectDocumentForClear(@Param("date") Date date);

  /**
   * 根据条件查询文档列表
   *
   * @param documentId 文档ID，用于指定特定文档
   * @param tenantId 租户ID，用于限定查询范围至特定租户
   * @param params 查询参数，包含查询条件的键值对
   * @return 返回一个列表，其中包含符合查询条件的文档信息映射
   */
  List<Map<String, Object>> queryDocumentListByCondition(
    @Param("documentId") Long documentId,
    @Param("tenantId") Long tenantId,
    @Param("params") Map<String, Object> params);

  /**
   * 根据内容ID、文档ID、租户ID更新文档内容
   * write by zyt 2025.5.20 11465479
   *
   * @param documentId 文档ID，用于标识需要更新的文档
   * @param tenantId 租户ID，用于区分不同租户的数据
   * @param contentId 内容ID，用于定位文档中的具体内容部分
   * @param content 更新的内容，以键值对形式提供，其中键是内容的字段名，值是更新后的字段值
   * @return 更新影响的行数，通常为1表示成功，0表示失败或未找到对应内容
   */
  int updateDocumentContentByContentId(
    @Param("documentId") Long documentId,
    @Param("tenantId") Long tenantId,
    @Param("contentId") Long contentId,
    @Param("content") Map<String, Object> content);

  /**
   * 插入文档内容
   * write by zyt 2025.5.20 11465479
   *
   * @param documentId 文档ID，用于标识特定的文档
   * @param tenantId 租户ID，用于区分不同租户的数据
   * @param contentId 内容ID，用于指定文档的内容部分
   * @param content 文档内容，以键值对形式提供，允许灵活地插入各种类型的数据
   * @return 插入操作影响的行数，通常为1表示成功，0表示失败或无变化
   */
  int insertDocumentContentByContentId(
    @Param("documentId") Long documentId,
    @Param("tenantId") Long tenantId,
    @Param("contentId") Long contentId,
    @Param("content") Map<String, Object> content);

  /**
   * 根据知识库 ID 获取文档列表
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 知识库 ID
   * @return 文档列表
   */
  List<SimpleDocumentDTO> selectSimpleDocumentList(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  List<DocumentDTO> selectDocumentListBydcDocCodes(@Param("knowledgeId") Long knowledgeId, @Param("list") List<String> documentCodes, @Param("tenantId") Long tenantId);

  boolean checkDocument(@Param("dcDocumentId") String documentId, @Param("knowledgeId") Long knowledgeId, @Param("documentName") String documentName, @Param("tenantId") Long tenantId);

  int updateDocumentExtSystemId(DocumentDTO item);

  List<SimpleLibDocumentDTO> selectSimpleLibDocumentDTOByKnowledgeIds(@Param("tenantId") Long tenantId, @Param("knowledgeIds") List<Long> knowledgeIds);

  List<DoKbIDAndDocIdDTO> selectDocumentListBydcId(@Param("knowledgeIds") List<Long> knowledgeIds, @Param("docIds") List<Long> docIds, @Param("tenantId") Long tenantId);

  List<DoKbIDAndDocIdDTO> selectDocumentListByDocumentIds(@Param("dcDocumentIds") List<Long> dcDocumentIds, @Param("tenantId") Long tenantId);

  /**
   * 通过文档库文档编码查询知识库文档
   * @param documentId
   * @param tenantId
   * @return
   */
  List<DocumentDTO> selectDocumentListByDcDcumentId(@Param("dcDocumentId") String documentId, @Param("tenantId") Long tenantId);
  /**
   * 查询需要创建文档节点的fileInfo信息 查询bt_file_info中busi_type='document'且status_cd='00A'且在bt_dc_document中不存在对应记录的数据
   *
   * @return 文件信息列表
   */
  Page<FileInfoDTO> selectFileInfoForDocumentNodeCreation(RowBounds rowBounds);

  /**
   * 查询租户的全员文档库
   *
   * @param tenantId 租户ID
   * @param libraryName 文档库名称
   * @param visibilityScope 可见范围
   * @return 文档库信息
   */
  DocumentLibraryEntity selectPublicLibraryByTenantId(@Param("tenantId") Long tenantId,
    @Param("libraryName") String libraryName, @Param("visibilityScope") String visibilityScope);

  /**
   * 更新知识库文档hasupdate为1
   * @param documentId
   * @param tenantId
   * @param currentLoginUserId
   * @return
   */
  int updateDocumentHasUpdate(@Param("documentId")Long documentId, @Param("tenantId")Long tenantId, @Param("currentLoginUserId")Long currentLoginUserId, @Param("docName")String docName);

  /**
   * 更新bt_document表的dc_document_id字段，通过file_info_id关联bt_dc_document
   *
   * @param fileInfoId 需要更新的file_info_id
   * @return 更新的记录数
   */
  int updateDcDocumentIdByFileInfoId(@Param("fileInfoId") Long fileInfoId);

  List<DocumentDTO> selectSimpleDocumentByKnowledgeIds(@Param("tenantId") Long tenantId, @Param("knowledgeIds") List<Long> knowledgeIds);

  int updateDocumentDcDocumentId(@Param("fileInfoId")Long fileInfoId, @Param("tenantId")Long tenantId, @Param("dcDocumentId")String dcDocumentId);

  /**
   * 批量删除文档
   *
   * @param tenantId 租户 ID
   * @param documentIds 文档列表id
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteDocuments(@Param("tenantId") Long tenantId, @Param("documentIds") List<Long> documentIds, @Param("updatorId") Long updatorId);
}
