package com.iwhalecloud.bote.doc.module.library.service;

/**
 * 文档库初始化服务
 *
 * @author yangran
 * @since 2025-08-18
 */
public interface IDocumentLibraryInitService {

  /**
   * 初始化用户个人文档库
   *
   * @param userId 用户ID
   * @param tenantId 租户ID
   */
  String initializeUserDocumentLibrary(Long userId, Long tenantId, Long spaceId);

  /**
   * 检查用户个人文档库是否存在
   *
   * @param userId 用户ID
   * @param tenantId 租户ID
   * @param spaceId 空间ID
   * @return 是否存在
   */
  String existsUserDocumentLibrary(Long userId, Long spaceId);

  /**
   * 检查用户的内置文件夹是否存在
   *
   * @param userId 用户ID
   * @param tenantId 租户ID
   * @param builtinType 内置类型
   * @return 是否存在
   */
  boolean existsBuiltinFolder(Long userId, Long tenantId, String builtinType, String libraryId);
}

