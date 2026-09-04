package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDetailDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCreateRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentNodeUpdateRO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPathDTO;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aiqing
 * @since 2025/8/15
 */
public interface IDocumentService {

  /**
   * 初始化文档库文档根节点
   *
   * @return 根节点ID
   */
  String initLibraryRootDocumentNode(String libraryId, String libraryName, Long tenantId, Long spaceId);

  /**
   * 查询文档路径
   *
   * @param documentId 文档ID
   * @return 文档路径信息封装
   */
  DocumentPathDTO getDocumentPath(String documentId);


  /**
   * 查询文档路径
   *
   * @param documentIdList 文档ID列表
   * @return 文档ID -> 文档路径, 可能未返回给出文档ID参数对应的路径
   */
  Map<String, DocumentPathDTO> getDocumentPathBatch(List<String> documentIdList);

  /**
   * 查询文档所属文档库ID
   *
   * @param documentId 文档ID
   * @return 文档库ID
   */
  String getLibraryIdByDocument(String documentId);

  /**
   * 根据文档ID查询文档节点信息
   *
   * @return 文档节点信息
   */
  DcDocumentDTO findByDocumentId(String documentId);

  /**
   * 根据文档ID和租户ID查询文档节点信息
   *
   * @param documentId 文档ID
   * @param tenantId 租户ID
   * @return 文档节点信息
   */
  DcDocumentDTO findByDocumentIdAndTenantId(String documentId, Long tenantId, String platform);

  /**
   * 查询文档详情
   *
   * @param documentId 文档ID
   * @return 文档详情
   */
  DcDocumentDetailDTO findDetailByDocumentId(String documentId, Long userId);

  /**
   * 创建文档节点
   *
   * @param userId 用户ID
   * @param documentCreateRequest 请求参数
   * @return 文档ID
   */
  String createOnlineDocumentNode(Long userId, DocumentCreateRequestDTO documentCreateRequest);

  /**
   * 创建内置文件夹
   *
   * @param userId 用户ID
   * @param spaceId 租户ID
   * @param builtinType 内置类型
   * @param folderName 文件夹名称
   * @param libraryId 文档库ID
   * @param rootNodeId 根节点ID
   */
  void createBuiltinFolder(Long userId, Long spaceId, Long tenantId, String builtinType,
                           String folderName, String libraryId, String rootNodeId);

  /**
   * 更新文档基本信息
   *
   * @param existDocument 文档
   * @param updateRO 参数
   */
  void updateDocumentBasicInfo(DcDocumentDTO existDocument, Long userId, DocumentNodeUpdateRO updateRO);

  /**
   * 更新上传文档名称, 文件重新上传时使用
   *
   * @param documentId 文档ID
   * @param documentName 文档名称
   * @param userId 用户ID
   * @param documentType 文档类型
   */
  void updateUploadDocumentNameWhenReUp(String documentId, String documentName, Long userId, String documentType);

  /**
   * 检查文档是否存在
   *
   * @param libraryId 文档库ID
   * @param documentId 文档ID
   */
  DcDocumentDTO checkDocumentIfExist(String libraryId, String documentId);

  /**
   * 批量查询文档
   *
   * @param nodeIds 文档ID
   */
  List<DcDocumentDTO> findBatchByDocumentId(List<String> nodeIds);

  /**
   * 获取文档所属文档库
   *
   * @param nodeIds 文档ID
   * @return 文档ID -> 文档库ID 映射
   */
  Map<String, String> findDocumentLibraryMapping(List<String> nodeIds);

  /**
   * 创建上传的文档节点
   *
   * @param userId 用户ID
   * @param documentCreateRequest 文档创建请求参数
   * @return 文档ID
   */
  String createUploadDocumentNode(Long userId, DocumentCreateRequestDTO documentCreateRequest);

  /**
   * 批量创建上传的文档节点
   *
   * @param userId 用户ID
   * @param documentCreateRequests 文档创建请求参数列表
   * @return 文档ID列表
   */
  List<String> batchCreateUploadDocumentNodes(Long userId, List<DocumentCreateRequestDTO> documentCreateRequests);

  /**
   * 根据名称和父节点查找文档
   *
   * @param documentName 文档名称
   * @param parentId 父节点ID
   * @param libraryId 文档库ID
   * @param tenantId 租户ID
   * @return 文档信息，如果不存在返回null
   */
  DcDocumentDTO findByNameAndParent(String documentName, String parentId, String libraryId, Long tenantId);

  /**
   * 更新文档库根节点名称
   *
   * @param libraryId 文档库ID
   * @param libraryName 新文档库名称
   */
  void updateLibraryRootNodeName(String libraryId, String libraryName);

  /**
   * 更新最近修改人
   *
   * @param documentId 文档ID
   * @param updatorId 最近修改人ID
   */
  void updateLastModify(String documentId, Long updatorId);

  /**
   * 更新文档权限模式
   *
   * @param documentId 文档ID
   * @param permissionMode 权限模式
   * @param updatorId 更新人ID
   */
  void updateDocumentPermissionMode(String documentId, Integer permissionMode, Long updatorId);

  void convertToOnlineIfNeeded(String aTrue, DcDocumentDTO documentDTO, Long userId);

  /**
   * 设置文档待发布的状态
   * @param documentId
   */
  void updateDocumentReleased(String documentId, String released);

  Map<String, String> findFoldersByLibraryId(String libraryId, Long tenantId);

  boolean existsDocumentByName(String libraryId, String parentId, String documentId, String fileName, String documentType, String contentSource);

  /**
   * 爬取网页并创建在线文档
   *
   * @param url 网页URL
   * @param libraryId 文档库ID
   * @param parentId 父节点ID（可选，为空则在根节点下创建）
   * @param documentName 文档名称（可选，为空则使用网页标题）
   * @param userId 用户ID
   * @param tenantId 租户ID
   * @param spaceId 空间ID
   * @return 创建的文档ID
   */
  String crawlWebPageAndCreateDocument(String url, String libraryId, String parentId,
                                       String documentName, Long userId, Long tenantId, Long spaceId);

  /**
   * 设置是否在文件夹上传场景下跳过单次创建节点时的库元素重算（由调用方在流程结束时统一重算一次，避免死锁）。
   *
   * @param skip true 表示跳过，false 表示恢复并清理线程上下文
   */
  void setSkipLibraryComputeForFolderUpload(boolean skip);

  /**
   * 按文档库重算库元素（bt_resource_element），用于文件夹批量上传结束后统一执行一次。
   *
   * @param libraryId 文档库ID
   */
  void recomputeLibraryElements(String libraryId);
}
