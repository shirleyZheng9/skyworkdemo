package com.iwhalecloud.bote.doc.module.document.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.control.model.ControlRoleInfo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentPermissionDTO;
import com.iwhalecloud.bote.doc.module.document.entity.DocumentPermissionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 文档权限相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface DocumentPermissionMapper {

  /**
   * 根据主键查询
   */
  DocumentPermissionEntity selectByPermissionId(@Param("permissionId") Long permissionId);

  /**
   * 插入记录
   */
  int insert(@Param("permission") DocumentPermissionEntity permission);

  /**
   * 更新记录
   */
  int update(@Param("permission") DocumentPermissionEntity permission);

  /**
   * 逻辑删除
   */
  int deleteByPermissionId(@Param("permissionId") Long permissionId);

  List<ControlRoleInfo> selectControlDocumentRoleInfoByControlIds(@Param("controlIds") List<String> controlIds);

  /**
   * 查询文档的权限配置
   *
   * @param documentIdList 文档ID
   * @return 权限记录
   */
  List<DocumentPermissionDTO> selectByDocumentId(@Param("documentIdList") List<String> documentIdList);

  /**
   * 根据可选参数查询
   *
   * @param documentId 文档ID
   * @param param 查询参数
   * @return 记录
   */
  List<DocumentPermissionDTO> selectByParam(@Param("documentId") String documentId, @Param("param") DocumentPermissionDTO param);

  /**
   * 查询分配过权限的文档ID
   *
   * @param documentIds 文档ID
   * @return 存在的文档ID
   */
  List<String> selectExistedControlDocumentId(@Param("documentIds") List<String> documentIds);

  /**
   * 移除权限配置
   *
   * @param documentId 文档ID
   * @param subjectId 授权对象ID
   * @param subjectType 授权对象类型
   * @param optUser 操作用户
   */
  void removePermission(@Param("documentId") String documentId,
                        @Param("subjectId") Long subjectId,
                        @Param("subjectType") String subjectType,
                        @Param("optUser") Long optUser);

  /**
   * 更新角色
   *
   * @param permissionId 主键ID
   * @param permissionType 权限角色
   */
  void updateSubjectPermission(@Param("permissionId") Long permissionId,
                               @Param("permissionType") String permissionType,
                               @Param("optUserId") Long optUserId);

  /**
   * 查询用户是否被配置文档权限
   *
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param orgIdList 组织ID
   */
  Page<Long> selectUserPermissionByLibraryId(@Param("libraryId") String libraryId,
                                             @Param("userId") Long userId,
                                             @Param("orgIdList") List<Long> orgIdList,
                                             RowBounds rowBounds);
  /**
   * 查询用户拥有文档的所有权限列表
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @param orgIdList 组织ID
   */
  List<String> selectUserPermissionByDocumentId(@Param("documentId") String documentId, @Param("userId") Long userId, @Param("orgIdList") List<Long> orgIdList);

  /**
   * 移除无权限的授权记录
   *
   * @param documentId 文档ID
   * @param subjectId 授权对象ID
   * @param subjectType 授权对象类型
   * @param optUser 操作用户
   */
  void removeAnnoPermission(@Param("documentId") String documentId,
                            @Param("subjectId") Long subjectId,
                            @Param("subjectType") String subjectType,
                            @Param("optUser") Long optUser);

  /**
   * 删除继承权限转换的独立权限记录
   *
   * @param documentId 文档ID
   * @param optUser 操作用户
   */
  void deleteInheritConvertPermissions(@Param("documentId") String documentId, @Param("optUser") Long optUser);

  /**
   *
   * @param documentIds
   * @param userId
   * @param orgs
   * @return
   */
  List<DocumentPermissionDTO> selectUserPermissionByDocumentIds(@Param("documentIds") List<String> documentIds, @Param("userId")Long userId, @Param("orgIdList") List<Long> orgs);
}
