package com.iwhalecloud.bote.doc.module.library.service;

import com.iwhalecloud.bote.doc.consts.PermissionTypeConstant.LibraryRoleEnum;
import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionDTO;
import java.util.List;

/**
 * 文档库权限service
 *
 * @author Aiqing
 * @since 2025/8/19
 */
public interface DocumentLibraryPermissionService {


  /**
   * 检查是否有文档库的编辑权限
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   */
  boolean checkLibraryEditPermission(String libraryId, Long userId);

  /**
   * 检查是否有文档库的访问权限
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   */
  boolean checkLibraryAccessPermission(String libraryId, Long userId);

  /**
   * 是否是文档库的管理者
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @return 是否有管理权限
   */
  boolean isLibraryManager(String libraryId, Long userId);

  /**
   * 查询文档库的权限记录
   *
   * @param libraryId 文档库ID
   * @return 记录
   */
  List<LibraryPermissionDTO> selectByLibraryId(String libraryId);

  /**
   * 查询用户文档库的最大权限角色
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @return 授权角色枚举
   */
  LibraryRoleEnum queryUserLibraryMaxRole(String libraryId, Long userId);

  /**
   * 查询授权主体的文档库权限
   *
   * @param libraryId 文档库ID
   * @param subjectId 授权主体ID
   * @param subjectType 授权主体类型
   * @return 权限信息
   */
  List<LibraryPermissionDTO> querySubjectLibraryPermissionBatch(String libraryId, List<Long> subjectId, String subjectType);
}
