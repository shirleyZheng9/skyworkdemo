package com.iwhalecloud.bote.doc.module.person.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.FrequentLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.RecentFileDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.RecentFileQueryParams;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.SearchDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.entity.UserHomepagePinEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 首页数据访问层
 *
 * @author yangran
 * @since 2025-01-06
 */
public interface HomepageMapper {

  /**
   * 查询置顶文档列表
   *
   * @param userId 用户ID
   * @param spaceId 空间ID
   * @param statusCd 状态码
   * @param tenantId 租户ID
   * @return 置顶文档列表
   */
  List<PinnedDocumentDTO> selectPinnedDocuments(@Param("userId") Long userId, @Param("spaceId") Long spaceId,
    @Param("statusCd") String statusCd, @Param("tenantId") Long tenantId);

  /**
   * 查询置顶文档库列表
   *
   * @param userId 用户ID
   * @param spaceId 空间ID
   * @param statusCd 状态码
   * @return 置顶文档库列表
   */
  List<PinnedLibraryDTO> selectPinnedLibraries(@Param("userId") Long userId, @Param("spaceId") Long spaceId,
    @Param("statusCd") String statusCd, @Param("tenantId") Long tenantId);

  /**
   * 查询置顶知识库列表
   *
   * @param userId 用户ID
   * @param spaceId 空间ID
   * @param statusCd 状态码
   * @param tenantId 租户ID
   * @return 置顶知识库列表
   */
  List<PinnedKnowledgeDTO> selectPinnedKnowledge(@Param("userId") Long userId, @Param("spaceId") Long spaceId,
    @Param("statusCd") String statusCd, @Param("tenantId") Long tenantId);

  /**
   * 检查是否存在置顶记录
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 置顶记录实体，如果不存在则返回null
   */
  UserHomepagePinEntity findPinRecord(@Param("userId") Long userId, @Param("targetId") String targetId,
    @Param("targetType") String targetType, @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

  /**
   * 统计置顶记录数量
   *
   * @param userId 用户ID
   * @param targetType 目标类型
   * @return 记录数量
   */
  int countPinRecords(@Param("userId") Long userId, @Param("targetType") String targetType);

  /**
   * 查询最后一个置顶记录ID
   *
   * @param userId 用户ID
   * @param targetType 目标类型
   * @return 最后一个置顶记录ID
   */
  Long selectLastPinId(@Param("userId") Long userId, @Param("targetType") String targetType);

  /**
   * 插入置顶记录
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 置顶记录ID
   */
  Long insertPinRecord(@Param("userId") Long userId, @Param("targetId") String targetId,
    @Param("targetType") String targetType);

  /**
   * 检查置顶记录是否存在
   *
   * @param pinId 置顶记录ID
   * @param userId 用户ID
   * @return 是否存在
   */
  boolean existsPinRecordById(@Param("pinId") Long pinId, @Param("userId") Long userId);

  /**
   * 删除置顶记录
   *
   * @param pinId 置顶记录ID
   * @return 影响行数
   */
  int deletePinRecord(@Param("pinId") Long pinId);

  /**
   * 更新置顶排序
   *
   * @param pinId 置顶记录ID
   * @param prevPinId 前一个置顶记录ID
   * @return 影响行数
   */
  int updatePinOrder(@Param("pinId") Long pinId, @Param("prevPinId") Long prevPinId);

  /**
   * 获取当前置顶项的前驱ID
   *
   * @param pinId 置顶记录ID
   * @param userId 用户ID
   * @return 前驱ID
   */
  Long getCurrentPrevPinId(@Param("pinId") Long pinId, @Param("userId") Long userId);

  /**
   * 更新被移动节点的后继节点的前驱指针 将以指定pinId为prevId的记录的prevId更新为指定的新前驱ID
   *
   * @param pinId 被移动的置顶项ID
   * @param newPrevPinId 新的前驱ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int updatePrevPinIdForMovedNode(@Param("pinId") Long pinId, @Param("newPrevPinId") Long newPrevPinId,
    @Param("userId") Long userId);

  /**
   * 更新目标位置的前驱指针 将以指定prevPinId为prevId的记录的prevId更新为指定的新前驱ID
   *
   * @param prevPinId 目标前驱ID
   * @param newPrevPinId 新的前驱ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int updatePrevPinIdForTargetPosition(@Param("prevPinId") Long prevPinId, @Param("newPrevPinId") Long newPrevPinId,
    @Param("userId") Long userId);

  /**
   * 批量更新同类型置顶项的前驱指针 将指定用户和类型的置顶项中，prev_pin_id为null且status_cd为00A的记录 的prev_pin_id更新为指定的新置顶项ID
   *
   * @param userId 用户ID
   * @param targetType 目标类型
   * @param newPinId 新置顶项ID
   * @return 影响行数
   */
  int updatePrevPinIdForNewHead(@Param("userId") Long userId, @Param("targetType") String targetType,
    @Param("newPinId") Long newPinId);

  /**
   * 更新置顶记录状态 将置顶记录的状态从00X更新为00A，并更新相关字段
   *
   * @param pinId 置顶记录ID
   * @param userId 用户ID
   * @param tenantId 租户ID
   * @return 影响行数
   */
  int reactivatePinRecord(@Param("pinId") Long pinId, @Param("userId") Long userId, @Param("tenantId") Long tenantId);

  /**
   * 查询常用文档库列表
   *
   * @param userId 用户ID
   * @param statusCd 状态码
   * @return 常用文档库列表
   */
  List<FrequentLibraryDTO> selectFrequentLibraries(@Param("userId") Long userId, @Param("statusCd") String statusCd,
    @Param("tenantId") Long tenantId, @Param("spaceId")Long spaceId);

  /**
   * 查询"[我的文档]库"信息
   *
   * @param userId 用户ID
   * @param statusCd 状态码
   * @return "[我的文档]库"信息，如果不存在则返回null
   */
  FrequentLibraryDTO selectMyDocumentsLibrary(@Param("userId") Long userId, @Param("statusCd") String statusCd, @Param("spaceId") Long spaceId);

  /**
   * 查询最近访问文件列表
   *
   * @param queryParams 查询参数
   * @param rowBounds 分页参数
   * @return 最近访问文件分页数据
   */
  Page<RecentFileDTO> selectRecentFiles(@Param("queryParams") RecentFileQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除最近访问文件记录
   *
   * @param userId 用户ID
   * @param documentId 文档ID
   * @return 影响行数
   */
  int deleteRecentFileRecord(@Param("userId") Long userId, @Param("documentId") String documentId);

  /**
   * 搜索文档（不分页）
   *
   * @param queryParams 搜索参数
   * @return 搜索结果列表
   */
  List<SearchDocumentDTO> searchDocumentsWithoutPaging(@Param("queryParams") SearchDocumentQueryParams queryParams);

  /**
   * 查询最近浏览记录
   *
   * @param documentIds 文档ID列表
   * @return 访问记录列表
   */
  List<DocumentActivityDTO> selectLastViewActivities(@Param("documentIds") List<String> documentIds);

  /**
   * 查询最近编辑记录
   *
   * @param documentIds 文档ID列表
   * @return 访问记录列表
   */
  List<DocumentActivityDTO> selectLastEditActivities(@Param("documentIds") List<String> documentIds);

  /**
   * 批量查询置顶记录，用于重排序操作 查询pinId in [pinId, prevPinId] or prevPinId = pinId的数据
   *
   * @param userId 用户ID
   * @param pinId 被拖拽的置顶项ID
   * @param prevPinId 目标前驱项ID
   * @return 置顶记录列表
   */
  List<UserHomepagePinEntity> selectPinRecordsForReorder(@Param("userId") Long userId, @Param("pinId") Long pinId,
    @Param("prevPinId") Long prevPinId);

  /**
   * 查询指定用户和类型的有效置顶记录
   *
   * @param userId 用户ID
   * @param targetType 目标类型
   * @param tenantId 租户ID
   * @param spaceId 空间ID
   * @return 置顶记录列表
   */
  List<UserHomepagePinEntity> selectActivePinsByUserAndType(@Param("userId") Long userId,
    @Param("targetType") String targetType, @Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId);

  /**
   * 查询租户下指定资源的所有有效置顶记录
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 置顶记录列表
   */
  List<UserHomepagePinEntity> selectActivePinsByTenantAndResource(@Param("tenantId") Long tenantId,
    @Param("targetId") String targetId, @Param("targetType") String targetType, @Param("spaceId") Long spaceId);

  /**
   * 租户维度批量更新链表关系 将租户下所有prevPinId为目标节点的记录的prevPinId更新为目标节点的prevPinId
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 影响行数
   */
  int updatePrevPinIdsForTenantUnpin(@Param("tenantId") Long tenantId, @Param("targetId") String targetId,
    @Param("targetType") String targetType);

  /**
   * 租户维度批量更新置顶记录状态为无效
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 影响行数
   */
  int updatePinStatusToInvalidByTenant(@Param("tenantId") Long tenantId, @Param("targetId") String targetId,
    @Param("targetType") String targetType);

  /**
   * 查询最近操作的文档
   *
   * @param userId 用户ID
   * @param type 文档类型（可选）
   * @param libraryId 文档库ID（可选）
   * @return 最近操作的文档列表
   */
  List<SearchDocumentDTO> selectRecentOperationDocuments(@Param("userId") Long userId, @Param("type") String type,
    @Param("libraryId") String libraryId, @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId);
}
