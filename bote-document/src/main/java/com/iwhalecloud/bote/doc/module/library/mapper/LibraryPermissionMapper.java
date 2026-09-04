package com.iwhalecloud.bote.doc.module.library.mapper;

import com.iwhalecloud.bote.doc.module.library.dto.LibraryPermissionDTO;
import com.iwhalecloud.bote.doc.module.document.entity.LibraryPermissionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 文档库权限相关数据库操作
 *
 * @author yangran
 * @since 2025-08-13
 */
public interface LibraryPermissionMapper {

  /**
   * 根据主键查询
   */
  LibraryPermissionEntity selectByPrimaryKey(@Param("permissionId") Long permissionId);

  /**
   * 插入记录
   */
  int insert(@Param("entity") LibraryPermissionEntity entity);

  /**
   * 更新记录
   */
  int updateByPrimaryKey(@Param("entity") LibraryPermissionEntity entity);

  /**
   * 逻辑删除
   */
  int deleteByPrimaryKey(@Param("permissionId") Long permissionId);

  /**
   * 根据文档库ID和主体信息查询权限
   */
  LibraryPermissionEntity selectByLibraryAndSubject(@Param("libraryId") String libraryId,
                                                    @Param("subjectType") String subjectType,
                                                    @Param("subjectId") Long subjectId);

  /**
   * 查询授权对象的授权记录
   *
   * @param libraryId 文档库ID
   * @param subjectIdList 授权对象ID
   * @param subjectType 授权对象类型
   * @return 记录
   */
  List<LibraryPermissionDTO> querySubjectLibraryPermissionBatch(@Param("libraryId") String libraryId,
                                                                @Param("subjectIdList") List<Long> subjectIdList,
                                                                @Param("subjectType") String subjectType);

  /**
   * 查询授权对象的授权记录
   *
   * @param libraryId 文档库ID
   * @param subjectIdList 授权对象ID
   * @param subjectType 授权对象类型
   * @return 记录
   */
  List<String> querySubjectLibraryPermissionTypesBatch(@Param("libraryId") String libraryId,
                                                       @Param("subjectIdList") List<Long> subjectIdList,
                                                       @Param("subjectType") String subjectType);

  /**
   * 批量插入权限记录
   */
  int batchInsertPermissions(@Param("list") List<LibraryPermissionEntity> permissions);

  /**
   * 批量删除权限记录
   */
  int batchDeletePermissions(@Param("permissionIds") List<Long> permissionIds);

  /**
   * 查询文档库配置的所有授权记录
   *
   * @param libraryId 文档库ID
   * @return 记录
   */
  List<LibraryPermissionDTO> selectByLibraryId(@Param("libraryId") String libraryId);
}
