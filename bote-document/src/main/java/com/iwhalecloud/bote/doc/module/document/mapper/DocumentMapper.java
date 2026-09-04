package com.iwhalecloud.bote.doc.module.document.mapper;

import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentReleasedDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface DocumentMapper {

  /**
   * 根据主键查询
   */
  DcDocumentEntity selectByDocumentId(@Param("documentId") String documentId);

  /**
   * 根据主键和租户ID查询
   */
  DcDocumentEntity selectByDocumentIdAndTenantId(@Param("documentId") String documentId, @Param("tenantId") Long tenantId, @Param("platform") String platform);

  /**
   * 插入记录
   */
  int insert(@Param("document") DcDocumentEntity document);

  /**
   * 批量插入记录
   *
   * @param documents 文档实体列表
   * @return 插入数量
   */
  int batchInsert(@Param("documents") List<DcDocumentEntity> documents);

  /**
   * 更新记录
   */
  int update(@Param("document") DcDocumentEntity document);

  /**
   * 移动文档
   */
  int updateMoveLibrary(@Param("document") DcDocumentEntity document);

  /**
   * 逻辑删除
   */
  int deleteByDocumentId(@Param("documentId") String documentId);

  /**
   * 删除文档
   *
   * @param documentId 文档ID
   * @param optUserId 操作用户
   * @return 更新数量
   */
  int rubbishByDocumentIds(@Param("libraryId") String libraryId,
    @Param("documentId") Collection<String> documentId,
    @Param("optUserId") Long optUserId);

  /**
   * 查询文档所属的文档库ID
   *
   * @param documentId 文档ID
   * @return 文档库ID
   */
  String selectLibraryIdByDocumentId(@Param("documentId") String documentId);

  /**
   * 查询文档的版本号
   *
   * @param documentId 文档ID
   * @return 版本号，如果文档不存在则返回null
   */
  Long selectRevisionByDocumentId(@Param("documentId") String documentId);

  /**
   * 查询整个文档库下的所有文档名称
   *
   * @param libraryId 文档库ID
   * @return 文档名称列表
   */
  List<String> selectDocumentNamesByLibraryId(@Param("libraryId") String libraryId);

  /**
   * 查询同级目录下的第一个文档ID
   *
   * @param parentId 父目录ID
   * @return 第一个文档ID，如果没有则返回null
   */
  String selectFirstSiblingDocumentId(@Param("parentId") String parentId);

  /**
   * 更新文档节点的前置节点
   *
   * @param documentId 文档ID
   * @param preDocumentId 前置文档ID
   * @param updatedTime 更新时间
   */
  void updateSiblingPreDocumentId(@Param("documentId") String documentId,
    @Param("preDocumentId") String preDocumentId,
    @Param("updatedTime") LocalDateTime updatedTime);

  /**
   * 更新文档名称
   *
   * @param documentId 文档ID
   * @param documentName 文档名称
   */
  void updateDocumentBasicInfo(@Param("documentId") String documentId,
    @Param("userId") Long userId,
    @Param("documentName") String documentName);

  /**
   * 更新文件名
   *
   * @param documentId 文档ID
   * @param userId 操作用户
   * @param documentName 文档名称
   * @param documentType 文档类型
   */
  void updateUploadDocumentName(@Param("documentId") String documentId,
    @Param("userId") Long userId,
    @Param("documentName") String documentName,
    @Param("documentType") String documentType);

  /**
   * 根据ID批量查询文档
   *
   * @param documentIds 文档ID集合
   * @return 文档信息
   */
  List<DcDocumentEntity> selectByLibraryIdAndDocumentIds(@Param("libraryId") String libraryId, @Param("documentIds") List<String> documentIds);

  /**
   * 根据ID批量查询文档
   *
   * @param documentIds 文档ID
   * @return 文档信息
   */
  List<DcDocumentDTO> findBatchByDocumentId(@Param("documentIds") List<String> documentIds);

  /**
   * 根据名称和父目录查找文档/文件夹
   *
   * @param documentName 文档名称
   * @param parentId 父目录ID
   * @param libraryId 文档库ID
   * @param tenantId 租户ID
   * @return 文档信息
   */
  DcDocumentDTO selectByNameAndParent(@Param("documentName") String documentName,
    @Param("parentId") String parentId,
    @Param("libraryId") String libraryId,
    @Param("tenantId") Long tenantId);

  /**
   * 根据文件名和文件大小查找已存在的文档
   *
   * @param fileName 文件名
   * @param fileSize 文件大小
   * @param tenantId 租户ID
   * @return 文档列表
   */
  List<DcDocumentEntity> selectByFileNameAndSize(@Param("fileName") String fileName,
    @Param("fileSize") Long fileSize,
    @Param("tenantId") Long tenantId);

  /**
   * 根据文档ID更新文档信息
   *
   * @param document 文档实体
   * @return 更新数量
   */
  int updateByDocumentId(@Param("document") DcDocumentEntity document);

  /**
   * 更新文档库根节点名称
   *
   * @param libraryId 文档库ID
   * @param libraryName 文档库名称
   */
  void updateLibraryRootNodeName(@Param("libraryId") String libraryId, @Param("libraryName") String libraryName);

  /**
   * 更新最近修改人
   *
   * @param documentId 文档ID
   * @param updatorId 最近修改人ID
   */
  void updateLastModify(@Param("documentId") String documentId, @Param("updatorId") Long updatorId);

  List<DcDocumentDTO> selectDocumentsByLibraryId(@Param("libraryId") String libraryId);

  /**
   * 根据文档库ID查询根节点文档
   *
   * @param libraryId 文档库ID
   * @return 根节点文档实体
   */
  DcDocumentEntity selectRootNodeByLibraryId(@Param("libraryId") String libraryId);

  /**
   * 更新文档权限模式
   *
   * @param documentId 文档ID
   * @param permissionMode 权限模式
   * @param updatorId 更新人ID
   */
  void updateDocumentPermissionMode(@Param("documentId") String documentId,
    @Param("permissionMode") Integer permissionMode,
    @Param("updatorId") Long updatorId);

  /**
   * 查询文档所属文档库ID
   *
   * @param documentIds 文档ID
   * @return 文档信息
   */
  List<DcDocumentDTO> findLibraryBatchByDocumentId(@Param("documentIds") List<String> documentIds);

  /**
   * 更新文档为在线文档（转换后）
   * 将本地上传的文档转换为在线文档后，更新相关字段
   *
   * @param documentId 文档ID
   * @param documentName 去掉后缀的文档名称
   * @param userId 操作用户ID
   * @return 更新数量
   */
  int updateConvertedToOnlineDocument(@Param("documentId") String documentId,
    @Param("documentName") String documentName,
    @Param("userId") Long userId);

  /**
   * 更新文档为在线表格（转换后）
   * 将本地上传的Excel文档转换为在线表格后，更新相关字段
   *
   * @param documentId 文档ID
   * @param documentName 去掉后缀的文档名称
   * @param userId 操作用户ID
   * @return 更新数量
   */
  int updateConvertedToOnlineWorkbook(@Param("documentId") String documentId,
    @Param("documentName") String documentName,
    @Param("userId") Long userId);

  void updateMoveLibraryBySubNodeIds(@Param("libraryId")String libraryId, @Param("documentIds")List<String> documentIds);

  int updateReleasedType(@Param("documentId") String documentId, @Param("releasedType") String releasedType);

  DocumentReleasedDTO queryDocumentReleased(@Param("documentId") String documentId);

  List<DcDocumentEntity> findFoldersByLibraryId(@Param("libraryId") String libraryId, @Param("tenantId")Long tenantId);

  int updatePreDocumentId(@Param("documentId")String documentId, @Param("prevDocumentId")String prevDocumentId);

  boolean existsDocumentByName(@Param("libraryId")String libraryId, @Param("parentId")String parentId, @Param("documentId")String documentId, @Param("fileName")String fileName, @Param("documentType")String documentType, @Param("contentSource")String contentSource);
}
