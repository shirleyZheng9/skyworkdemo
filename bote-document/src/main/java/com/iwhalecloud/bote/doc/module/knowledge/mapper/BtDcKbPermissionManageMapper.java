package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcKbPermissionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcKbPermissionQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 知识库权限表管理
 *
 * @author linmengfan
 * @since 2025-08-23
 */
public interface BtDcKbPermissionManageMapper {
  /**
   * 校验知识库权限表的编码唯一性
   *
   * @param btDcKbPermission 知识库权限表
   * @return 结果
   */
  boolean existsBtDcKbPermissionCode(@Param("dto") BtDcKbPermissionDTO btDcKbPermission);

  /**
   * 根据主键获取知识库权限表
   *
   * @param permissionId 知识库权限表主键
   * @return 知识库权限表
   */
  BtDcKbPermissionDTO getBtDcKbPermission(@Param("id") Long permissionId);

  /**
   * 新增知识库权限表
   *
   * @param btDcKbPermission 知识库权限表
   * @return 结果
   */
  int insertBtDcKbPermission(@Param("dto") BtDcKbPermissionDTO btDcKbPermission);

  /**
   * 批量新增知识库权限表
   *
   * @param btDcKbPermissions 知识库权限表列表
   * @return 结果
   */
  int batchInsertBtDcKbPermission(@Param("list") List<BtDcKbPermissionDTO> btDcKbPermissions);

  /**
   * 修改知识库权限表
   *
   * @param btDcKbPermission 知识库权限表
   * @return 结果
   */
  int updateBtDcKbPermission(@Param("dto") BtDcKbPermissionDTO btDcKbPermission);

  /**
   * 获取知识库权限表列表
   *
   * @param queryParams 查询条件
   * @return 知识库权限表列表
   */
  List<BtDcKbPermissionDTO> selectBtDcKbPermissionList(@Param("query") BtDcKbPermissionQueryParams queryParams);

  /**
   * 获取知识库权限表列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库权限表分页列表
   */
  Page<BtDcKbPermissionDTO> selectBtDcKbPermissionPage(@Param("query") BtDcKbPermissionQueryParams queryParams, RowBounds rowBounds);

  /**
   * 知识库删除的时候失效有效的权限
   */
  int deleteBtDcKbPermissionByKnowledgeId(@Param("knowledgeId") Long knowledgeId, @Param("updatorId") Long updatorId);

  BtDcKbPermissionDTO selectBtDcKbPermissionDTO(@Param("knowledgeId") Long knowledgeId, @Param("subjectId") Long subjectId, @Param("subjectType") String subjectType);

  List<BtDcKbPermissionDTO> selectBtDcKbPermissionDTOs(@Param("knowledgeId") Long knowledgeId, @Param("list") List<Long> deptIds);

  /**
   * 批量查询知识库权限（用户权限）
   *
   * @param knowledgeIds 知识库ID列表
   * @param userId 用户ID
   * @return 权限列表
   */
  List<BtDcKbPermissionDTO> batchSelectUserPermissions(@Param("knowledgeIds") List<Long> knowledgeIds, @Param("userId") Long userId);

  /**
   * 批量查询知识库权限（部门权限）
   *
   * @param knowledgeIds 知识库ID列表
   * @param deptIds 部门ID列表
   * @return 权限列表
   */
  List<BtDcKbPermissionDTO> batchSelectDeptPermissions(@Param("knowledgeIds") List<Long> knowledgeIds, @Param("deptIds") List<Long> deptIds);

  int deleteByPrimaryKey(BtDcKbPermissionDTO permission);

  int batchDeletePermissions(@Param("list") List<Long> permissionIdsToDelete, @Param("updatorId") Long updatorId);
}
