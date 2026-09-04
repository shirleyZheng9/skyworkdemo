package com.iwhalecloud.bote.service.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.organization.OrgUserRoleDTO;
import com.iwhalecloud.bote.dto.organization.query.OrgUserRoleQuery;

import java.util.List;

/**
 * 组织用户角色服务
 *
 * @author wangtingyun
 * @since 2025-10-30
 */
public interface IOrgUserRoleService {

  /**
   * 分页查询角色下的成员用户信息列表
   *
   * @param query 查询参数
   * @return 成员信息列表
   */
  PageInfo<OrgUserRoleDTO> queryOrgUserRolePage(OrgUserRoleQuery query);

  /**
   * 统计组织用户角色数量
   *
   * @param query 查询参数
   * @return 成员信息列表
   */
  int countOrgUserRole(OrgUserRoleQuery query);

  /**
   * 批量添加组织用户角色
   *
   * @param roleDTO 组织用户角色DTO
   */
  void batchAddOrgUserRole(OrgUserRoleDTO roleDTO);

  /**
   * 根据主键删除单个组织用户角色
   *
   * @param orgRoleId 组织用户角色主键ID
   */
  void delOrgUserRoleById(Long orgRoleId);

  /**
   * 根据空间ID和用户ID删除单个组织用户角色
   *
   * @param spaceId 空间ID
   * @param userId 用户ID
   */
  void delOrgUserRole(Long spaceId, Long userId);

  /**
   * 批量删除组织用户角色
   *
   * @param spaceId 空间ID
   * @param userIds 用户ID列表
   */
  void batchDelOrgUserRole(Long spaceId, List<Long> userIds);

  /**
   * 给用户添加组织成员角色
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @param orgRole 组织用户角色
   */
  void addUserOrgRole(Long spaceId, Long userId, String orgRole);

  /**
   * 修改用户的组织成员角色
   *
   * @param roleDTO 组织用户成员数据
   */
  void modifyUserOrgRole(OrgUserRoleDTO roleDTO);

}
