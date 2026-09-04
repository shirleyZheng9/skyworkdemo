package com.iwhalecloud.bote.service.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.organization.BatchAddOrganizationMembersResult;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberImportDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationMemberQueryParams;
import com.iwhalecloud.bote.dto.organization.request.BatchAddOrganizationMembersRequest;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 组织成员管理服务接口
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface IOrganizationMemberService {

  /**
   * 添加组织成员
   *
   * @param member 成员信息
   * @return 添加结果
   */
  ResultVO<OrganizationMemberDTO> addOrganizationMember(OrganizationMemberDTO member);

  /**
   * 批量添加组织成员
   *
   * @param request 批量添加请求
   * @return 批量添加结果
   */
  ResultVO<BatchAddOrganizationMembersResult> batchAddOrganizationMembers(BatchAddOrganizationMembersRequest request);

  /**
   * 修改组织成员
   *
   * @param member 成员信息
   * @return 修改结果
   */
  ResultVO<OrganizationMemberDTO> updateOrganizationMember(OrganizationMemberDTO member);

  /**
   * 查询单个组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @return 成员信息
   */
  @Nullable
  OrganizationMemberDTO findOrganizationMember(Long spaceId, Long memberId);

  /**
   * 根据组织和用户查询成员信息
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param userId 用户ID
   * @return 成员信息
   */
  @Nullable
  OrganizationMemberDTO findMemberByOrgAndUser(Long spaceId, Long orgId, Long userId);

  /**
   * 查询组织成员列表
   *
   * @param queryParams 查询参数
   * @return 成员列表
   */
  List<OrganizationMemberDTO> queryOrganizationMemberList(OrganizationMemberQueryParams queryParams);

  /**
   * 分页查询组织成员列表
   *
   * @param queryParams 查询参数
   * @return 成员分页列表
   */
  PageInfo<OrganizationMemberDTO> queryOrganizationMemberPage(OrganizationMemberQueryParams queryParams);

  /**
   * 查询用户所属的组织列表
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 用户的组织成员信息列表
   */
  List<OrganizationMemberDTO> queryUserOrganizations(Long spaceId, Long userId);

  /**
   * 批量查询用户所属的组织列表
   *
   * @param spaceId 企业空间ID
   * @param userIds 用户ID列表
   * @return 用户的组织成员信息列表（按用户ID分组）
   */
  List<OrganizationMemberDTO> queryUserOrganizationsBatch(Long spaceId, List<Long> userIds);

  /**
   * 查询组织的管理员列表
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 管理员列表
   */
  List<OrganizationMemberDTO> queryOrganizationAdmins(Long spaceId, Long orgId);

  /**
   * 更新成员状态
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @param statusCd 状态
   * @return 更新结果
   */
  ResultVO<Void> updateMemberStatus(Long spaceId, Long memberId, String statusCd);

  /**
   * 移除组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberId 成员ID
   * @return 移除结果
   */
  ResultVO<Void> removeOrganizationMember(Long spaceId, Long memberId);

  /**
   * 批量移除组织成员
   *
   * @param spaceId 企业空间ID
   * @param memberIds 成员ID列表
   * @return 移除结果
   */
  ResultVO<Void> batchRemoveOrganizationMembers(Long spaceId, List<Long> memberIds);

  /**
   * 批量导入成员
   *
   * @param file Excel文件
   * @param orgId 组织ID
   * @param spaceId 企业空间ID
   * @param defaultMemberRole 默认成员角色
   * @param defaultMemberType 默认成员类型
   * @return 导入结果
   */
  ResultVO<OrganizationMemberImportDTO> batchImportMembers(MultipartFile file, Long orgId, Long spaceId, String defaultMemberRole, String defaultMemberType);

  /**
   * 检查用户是否已是组织成员
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param userId 用户ID
   * @return 是否已是成员
   */
  boolean checkMemberExists(Long spaceId, Long orgId, Long userId);

  /**
   * 统计组织成员数量
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 成员数量
   */
  int countOrganizationMembers(Long spaceId, Long orgId);

  /**
   * 查询组织下的子组织列表和用户成员列表
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param excludeRoleUser 是否过滤已授权角色用户
   * @return 目标组织下的子组织列表和用户成员列表
   */
  OrganizationUserDTO queryOrgAndUserList(Long spaceId, Long orgId, Boolean excludeRoleUser);

  /**
   * 新增组织成员
   *
   * @param spaceId 空间ID
   * @param orgId 组织ID
   * @param userId 用户ID
   * @return 添加结果
   */
  ResultVO<Void> createOrganizationMember(Long spaceId, Long orgId, Long userId);

  /**
   * 添加用户到对应空间的根组织成员
   *
   * @param spaceId 空间ID
   * @param userId 用户ID
   */
  void addUserToRootOrg(Long spaceId, Long userId);

  /**
   * 查询用户所属的组织及其父组织ID列表
   *
   * @param spaceId 企业空间ID
   * @param userId 用户ID
   * @return 用户所属的组织及其全部父组织ID列表
   */
  List<Long> queryUserOrgAndParentOrgIds(Long spaceId, Long userId);

  /**
   * 根据组织ID查询成员列表
   *
   * @param spaceId 空间ID
   * @param orgIds 组织ID列表
   * @return 成员列表
   */
  List<OrganizationMemberDTO> queryMemberListByOrgIds(Long spaceId, List<Long> orgIds);
}
