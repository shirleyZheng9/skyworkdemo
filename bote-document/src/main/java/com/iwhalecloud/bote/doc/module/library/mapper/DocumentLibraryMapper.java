package com.iwhalecloud.bote.doc.module.library.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryDetailDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryListDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPageQueryParam;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.entity.DocumentLibraryEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface DocumentLibraryMapper {

  DocumentLibraryEntity selectByLibrary(@Param("libraryId") String libraryId);

  DocumentLibraryEntity selectByLibraryAndTenantId(@Param("libraryId") String libraryId, @Param("tenantId") Long tenantId,  @Param("platform") String platform);

  DocumentLibraryEntity selectByLibraryAndSpaceId(@Param("libraryId") String libraryId, @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId);

  int insert(@Param("entity") DocumentLibraryDTO entity);

  int updateByPrimaryKey(@Param("entity") DocumentLibraryDTO entity);

  int deleteByPrimaryKey(@Param("libraryId") String libraryId);

  String selectNameById(@Param("libraryId") String libraryId);

  // ===== 合并自 LibraryListMapper =====
  Page<LibraryListDTO> selectVisibleLibraries(@Param("param") LibraryPageQueryParam param, RowBounds rowBounds);

  Page<LibraryListDTO> selectAllLibraries(@Param("param") LibraryPageQueryParam param, RowBounds rowBounds);


  // ===== 合并自 LibraryDetailMapper =====
  LibraryDetailDTO selectLibraryBase(@Param("libraryId") String libraryId, @Param("statusCd") String statusCd,
    @Param("userId") Long userId, @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId);

  List<LibraryDetailDTO.MemberInfo> selectLibraryMembers(@Param("libraryId") String libraryId,
    @Param("statusCd") String statusCd, @Param("creatorId") Long creatorId, @Param("spaceId") Long spaceId);

  /**
   * 更新文档库基础信息
   *
   * @param libraryId 文档库ID
   * @param libraryName 文档库名称
   * @param description 描述
   * @param libraryIcon 图标
   * @param updatorId 更新人ID
   * @return 更新行数
   */
  int updateLibraryInfo(@Param("libraryId") String libraryId, @Param("libraryName") String libraryName,
    @Param("description") String description, @Param("libraryIcon") String libraryIcon, @Param("color") String color,
    @Param("updatorId") Long updatorId, @Param("updatedTime") LocalDateTime updatedTime, @Param("spaceId") Long spaceId);

  /**
   * 更新文档库可见范围
   *
   * @param libraryId 文档库ID
   * @param visibilityScope 可见范围
   * @param updatorId 更新人ID
   * @return 更新行数
   */
  int updateLibraryVisibility(@Param("libraryId") String libraryId, @Param("visibilityScope") String visibilityScope,
    @Param("updatorId") Long updatorId, @Param("updatedTime") LocalDateTime updatedTime, @Param("spaceId") Long spaceId);

  /**
   * 查询个人文档库的ID
   *
   * @param userId 用户ID
   * @return 文档库ID
   */
  List<String> findUserPrivateLibraryId(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 逻辑删除文档库（放入回收站）
   *
   * @param libraryId 文档库ID
   * @param optUserId 操作用户ID
   * @return 更新行数
   */
  int rubbishByLibraryId(@Param("libraryId") String libraryId, @Param("optUserId") Long optUserId, @Param("spaceId") Long spaceId);

  DocumentLibraryDTO queryByPrimaryKey(@Param("id") Long id, @Param("tenantId") Long tenantId);

  List<String> findUserPrivateMyLibraryById(@Param("userId")Long userId, @Param("spaceId")Long spaceId);

  DocumentLibraryDTO queryLibraryByDocumentId(@Param("documentId")String documentId);

  /**
   * 校验文档库名的唯一性
   *
   * @param libraryDTO 文档库
   * @return 结果
   */
  boolean existsLibraryName(@Param("dto") DocumentLibraryDTO libraryDTO);
}
