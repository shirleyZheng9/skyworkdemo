package com.iwhalecloud.bote.mapper.organization;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationMemberQueryParams;
import com.iwhalecloud.bote.entity.organization.OrganizationMemberEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 组织成员管理 Mapper
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface OrganizationMemberMapper {

  /**
   * 检查用户是否已是组织成员
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param userId 用户ID
   * @return 是否已是成员
   */
  boolean existsOrganizationMember(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId, @Param("userId") Long userId);

  /**
   * 根据主键获取组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @return 成员信息
   */
  @Nullable
  OrganizationMemberDTO getOrganizationMember(@Param("spaceId") Long spaceId, @Param("memberId") Long memberId);

  /**
   * 根据组织和用户获取成员信息
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param userId 用户ID
   * @return 成员信息
   */
  @Nullable
  OrganizationMemberDTO getOrganizationMemberByOrgAndUser(@Param("spaceId") Long spaceId,
                                                           @Param("orgId") Long orgId,
                                                           @Param("userId") Long userId);

  /**
   * 新增组织成员
   *
   * @param member 成员信息
   * @return 影响行数
   */
  int insertOrganizationMember(@Param("dto") OrganizationMemberEntity member);

  /**
   * 批量新增组织成员
   *
   * @param members 成员信息列表
   * @return 影响行数
   */
  int batchInsertOrganizationMembers(@Param("list") List<OrganizationMemberEntity> members);

  /**
   * 修改组织成员
   *
   * @param member 成员信息
   * @return 影响行数
   */
  int updateOrganizationMember(@Param("dto") OrganizationMemberDTO member);

  /**
   * 更新成员状态
   *
   * @param memberId 成员ID
   * @param statusCd 状态
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int updateMemberStatus(@Param("memberId") Long memberId, @Param("statusCd") String statusCd, @Param("updatorId") Long updatorId);

  /**
   * 删除组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteOrganizationMember(@Param("spaceId") Long spaceId, @Param("memberId") Long memberId, @Param("updatorId") Long updatorId);

  /**
   * 批量删除组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberIds 成员ID列表
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int batchDeleteOrganizationMembers(@Param("spaceId") Long spaceId, @Param("memberIds") List<Long> memberIds, @Param("updatorId") Long updatorId);

  /**
   * 查询组织成员列表
   *
   * @param queryParams 查询参数
   * @return 成员列表
   */
  List<OrganizationMemberDTO> selectOrganizationMemberList(@Param("query") OrganizationMemberQueryParams queryParams);

  /**
   * 分页查询组织成员列表
   *
   * @param queryParams 查询参数
   * @param rowBounds 分页参数
   * @return 成员分页列表
   */
  Page<OrganizationMemberDTO> selectOrganizationMemberPage(@Param("query") OrganizationMemberQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询用户所属的组织列表
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 用户的组织成员信息列表
   */
  List<OrganizationMemberDTO> selectUserOrganizations(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 批量查询用户所属的组织列表
   *
   * @param spaceId 企业空间ID
   * @param userIds 用户ID列表
   * @return 用户的组织成员信息列表（按用户ID分组）
   */
  List<OrganizationMemberDTO> selectUserOrganizationsBatch(@Param("spaceId") Long spaceId, @Param("userIds") List<Long> userIds);

  /**
   * 查询组织的管理员列表
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 管理员列表
   */
  List<OrganizationMemberDTO> selectOrganizationAdmins(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId);

  /**
   * 统计组织成员数量
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 成员数量
   */
  int countOrganizationMembers(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId);

  /**
   * 查询组织成员的用户信息列表
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param excludeRoleUser 是否过滤已授权角色用户
   * @return 成员用户信息列表
   */
  List<OrganizationUserDTO> selectOrgUserList(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId,
                                              @Param("excludeRoleUser") Boolean excludeRoleUser);

  /**
   * 统计组织成员用户数量
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param excludeRoleUser 是否过滤已授权角色用户
   * @return 成员数量
   */
  int countOrgUsers(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId, @Param("excludeRoleUser") Boolean excludeRoleUser);

  /**
   * 查找企业空间下的非组织成员列表
   *
   * @param tenantId 企业空间ID
   * @return 非组织成员列表
   */
  List<OrganizationUserDTO> selectTenantUserListForOrg(@Param("tenantId") Long tenantId);

  /**
   * 批量查询用户所属的组织列表信息
   *
   * @param spaceId 企业空间ID
   * @param userIdList 用户ID列表
   * @return 用户所属的组织信息列表
   */
  List<OrganizationUserDTO> batchSelectUserOrgInfoList(@Param("spaceId") Long spaceId, @Param("list") List<Long> userIdList);

  /**
   * 根据主键获取组织成员基本信息
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @return 成员信息
   */
  @Nullable
  OrganizationMemberDTO selectOrgMemberBasicInfo(@Param("spaceId") Long spaceId, @Param("memberId") Long memberId);

  /**
   * 检查用户在当前企业组织下是否存在组织成员
   *
   * @param spaceId spaceId 企业空间ID
   * @param userId userId 用户ID
   * @return 检查结果
   */
  boolean existUserOrgMember(@Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 删除用户在指定企业下的的全部组织成员
   *
   * @param spaceId 企业空间ID
   * @param userId 成员ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteUserAllOrgMember(@Param("spaceId") Long spaceId, @Param("userId") Long userId, @Param("updatorId") Long updatorId);

  /**
   * 查询组织下的全部成员列表
   *
   * @param spaceId 空间ID
   * @param orgId 组织ID
   * @return 成员列表
   */
  List<OrganizationMemberDTO> selectMemberListByOrgId(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId);

  /**
   * 根据组织ID列表查询组织下的全部成员列表
   *
   * @param spaceId 空间ID
   * @param orgIds 组织ID列表
   * @return 成员列表
   */
  List<OrganizationMemberDTO> selectMemberListByOrgIds(@Param("spaceId") Long spaceId, @Param("orgIds") List<Long> orgIds);

  /**
   * 根据用户ID查询用户所属的组织列表
   *
   * @param userId 用户ID
   * @return 用户所属的组织列表
   */
  List<OrganizationUserDTO> selectUserOrgByUserId(@Param("spaceId") Long spaceId, @Param("userId") Long userId);
}
