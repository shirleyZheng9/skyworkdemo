package com.iwhalecloud.bote.doc.module.library.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryDetailDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryListDTO;
import com.iwhalecloud.bote.doc.module.library.dto.DocumentLibraryDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.ExitShareResponseDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryBatchPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsRequestDTO;
import com.iwhalecloud.bote.doc.module.library.dto.LibrarySettingsResponseDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 文档库服务
 *
 * @author auto
 * @since 2025-08-13
 */
public interface DocumentLibraryService {

  /**
   * 根据文档库ID查询名称
   *
   * @param libraryId 文档库 ID
   * @return 文档库名称
   */
  String getNameById(String libraryId);

  /**
   * 根据文档库ID查询基本信息
   *
   * @param libraryId 文档库 ID
   * @return 文档库信息
   */
  DocumentLibraryDTO findByLibraryId(String libraryId);

  /**
   * 根据文档库ID和空间ID查询基本信息
   *
   * @param libraryId 文档库 ID
   * @param spaceId 空间ID
   * @return 文档库信息
   */
  DocumentLibraryDTO findByLibraryIdAndSpaceId(String libraryId, Long spaceId, Long tenantId);

  /**
   * 根据文档库ID和租户ID查询基本信息
   *
   * @param libraryId 文档库 ID
   * @param tenantId 租户ID
   * @return 文档库信息
   */
  DocumentLibraryDTO findByLibraryIdAndTenantId(String libraryId, Long tenantId, String platform);

  /**
   * 创建文档库
   *
   * @param request 创建请求
   * @param creatorId 创建人用户ID
   * @return 新建的文档库
   */
  DocumentLibraryDTO createLibrary(DocumentLibraryDTO request, Long creatorId);

  /**
   * 获取"我可见"的文档库分页列表
   *
   * @param pageNum 页码
   * @param pageSize 每页大小
   * @param sortBy 排序字段
   * @param sortOrder 排序顺序
   * @param keyword 关键词
   * @param spaceId 空间ID
   * @return 文档库分页列表
   */
  PageInfo<LibraryListDTO> getVisibleLibraryPage(Integer pageNum, Integer pageSize, String sortBy, String sortOrder,
                                                 String keyword, Long spaceId, Long tenantId, Long envTenantId);

  /**
   * 获取文档库详情
   *
   * @param libraryId 文档库 ID
   * @param spaceId 空间ID
   * @return 文档库详情
   */
  LibraryDetailDTO getLibraryDetail(String libraryId, Long spaceId, Long tenantId);

  /**
   * 更新文档库设置
   *
   * @param libraryId 文档库ID
   * @param request 设置请求
   * @param userId 操作用户ID
   * @return 设置结果
   */
  LibrarySettingsResponseDTO updateLibrarySettings(String libraryId, LibrarySettingsRequestDTO request, Long userId);

  /**
   * 更新文档库权限设置
   *
   * @param libraryId 文档库ID
   * @param request 权限设置请求
   * @param userId 操作用户ID
   * @return 权限设置结果
   */
  ResultVO<Void> updateLibraryPermissions(String libraryId, LibraryPermissionRequestDTO request, Long userId);

  /**
   * 查询用户个人文档库
   *
   * @param userId 用户ID
   * @return 文档库ID
   */
  String findUserPrivateLibraryId(Long userId, Long spaceId);

  /**
   * 批量更新文档库权限设置
   *
   * @param libraryId 文档库ID
   * @param request 批量权限设置请求
   * @param userId 操作用户ID
   * @return 批量权限设置结果
   */
  ResultVO<Void> batchLibraryPermissions(String libraryId, LibraryBatchPermissionRequestDTO request, Long userId);

  /**
   * 删除文档库（逻辑删除）
   *
   * @param libraryId 文档库ID
   * @param userId 操作用户ID
   * @return 删除结果
   */
  ResultVO<Void> deleteLibrary(String libraryId, Long userId);

  /**
   * 退出共享文档库
   *
   * @param request 退出共享请求
   * @param userId 当前用户ID
   * @return 退出共享结果
   */
  ResultVO<ExitShareResponseDTO> exitShare(ExitShareRequestDTO request, Long userId);

  String findUserPrivateMyLibraryById(Long userId, Long spaceId, Long tenantId);

  DocumentLibraryDTO queryLibraryByDocumentId(String documentId);
}


