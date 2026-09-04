package com.iwhalecloud.bote.mapper.organization;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.organization.OrgUserRoleDTO;
import com.iwhalecloud.bote.dto.organization.query.OrgUserRoleQuery;
import com.iwhalecloud.bote.entity.organization.OrgUserRoleEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 组织用户角色 Mapper
 *
 * @author tingyun.wang
 * @since 2025-10-30
 */
public interface OrgUserRoleMapper {

  /**
   * 批量插入组织用户角色
   *
   * @param entityList 组织用户角色实体列表
   * @return 批量插入结果
   */
  int insertOrgUserRoleBatch(@Param("list") List<OrgUserRoleEntity> entityList);

  /**
   * 分页查询角色下的用户信息列表
   *
   * @param query 查询参数
   * @return 成员信息列表
   */
  Page<OrgUserRoleDTO> selectOrgUserRolePage(@Param("query") OrgUserRoleQuery query, RowBounds rowBounds);

  /**
   * 统计组织用户角色数量
   *
   * @param query 查询参数
   * @return 成员信息列表
   */
  int countOrgUserRole(@Param("query") OrgUserRoleQuery query);

  /**
   * 检查用户是否已在企业空间的组织下授权
   *
   * @param spaceId 空间ID
   * @param userId 用户ID
   * @return 检查结果
   */
  OrgUserRoleDTO selectOrgUserRole(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 根据主键查询组织用户角色
   *
   * @param orgRoleId 主键ID
   * @return 组织用户角色
   */
  OrgUserRoleDTO selectOrgUserRoleById(@Param("orgRoleId") Long orgRoleId);

  /**
   * 根据主键删除组织用户角色
   *
   * @param orgRoleId 主键ID
   * @return 删除结果
   */
  int deleteOrgUserRoleById(@Param("orgRoleId") Long orgRoleId);

  /**
   * 根据空间ID和用户ID删除单个组织用户角色
   *
   * @param spaceId 空间ID
   * @param userId 用户ID
   * @param updatorId 操作人ID
   * @return 删除结果
   */
  int deleteBySpaceIdAndUserId(@Param("spaceId") Long spaceId, @Param("userId") Long userId,
                               @Param("updatorId") Long updatorId);

  /**
   * 批量删除组织用户角色
   *
   * @param spaceId 空间ID
   * @param userIds 用户ID列表
   * @param updatorId 操作人ID
   * @return 删除结果
   */
  int batchDeleteOrgUserRole(@Param("spaceId") Long spaceId, @Param("userIds") List<Long> userIds,
                             @Param("updatorId") Long updatorId);

  /**
   * 检查是否企业组织管理员
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 是否企业组织管理员
   */
  boolean checkIsOrgAdmin(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 修改用户的组织角色
   *
   * @param roleDTO 组织用户成员数据
   */
  int updateUserOrgRole(@Param("dto") OrgUserRoleDTO roleDTO);

}
