package com.iwhalecloud.bote.service.organization;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationTreeNodeDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationQueryParams;
import com.iwhalecloud.bote.dto.organization.request.OrganizationSearchRequest;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 组织管理服务接口
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface IOrganizationManageService {

  /**
   * 保存组织
   *
   * @param organization 组织信息
   * @return 保存结果
   */
  ResultVO<OrganizationDTO> saveOrganization(OrganizationDTO organization);

  /**
   * 创建企业的根组织（仅限系统内部调用）
   *
   * @param spaceId 企业空间ID
   * @param spaceName 企业名称
   * @param creatorId 创建人ID
   * @return 根组织信息
   */
  OrganizationDTO createRootOrgForWorkspace(Long spaceId, String spaceName, Long creatorId);

  /**
   * 保存企业的根组织（仅限系统内部调用）
   *
   * @param spaceId 企业空间ID
   * @param spaceName 企业名称
   * @param updatorId 更新人ID
   */
  void updateRootOrgName(Long spaceId, String spaceName, Long updatorId);

  /**
   * 查询单个组织
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 组织信息
   */
  @Nullable
  OrganizationDTO findOrganization(Long spaceId, Long orgId);

  /**
   * 根据组织编码查询组织
   *
   * @param spaceId 企业空间ID
   * @param orgCode 组织编码
   * @return 组织信息
   */
  @Nullable
  OrganizationDTO findOrganizationByCode(Long spaceId, String orgCode);

  /**
   * 根据主键批量查询组织
   *
   * @param spaceId 企业空间ID
   * @param orgIdList 组织ID列表
   * @return 组织信息列表
   */
  List<OrganizationDTO> queryOrganizationList(Long spaceId, List<Long> orgIdList);

  /**
   * 查询组织列表
   *
   * @param queryParams 查询参数
   * @return 组织列表
   */
  List<SimpleOrganizationDTO> queryOrganizationList(OrganizationQueryParams queryParams);

  /**
   * 分页查询组织列表
   *
   * @param queryParams 查询参数
   * @return 组织分页列表
   */
  PageInfo<OrganizationDTO> queryOrganizationPage(OrganizationQueryParams queryParams);

  /**
   * 高级搜索组织
   *
   * @param searchRequest 搜索请求
   * @return 组织分页列表
   */
  PageInfo<OrganizationDTO> searchOrganizations(OrganizationSearchRequest searchRequest);

  /**
   * 查询组织树结构
   *
   * @param spaceId 企业空间ID
   * @param rootOrgId 根组织ID（为空时查询所有顶级组织）
   * @param maxLevel 最大层级
   * @return 组织树节点列表
   */
  List<OrganizationTreeNodeDTO> queryOrganizationTree(Long spaceId, @Nullable Long rootOrgId, @Nullable Integer maxLevel);

  /**
   * 按名称搜索查询组织树结构
   *
   * @param spaceId 企业空间ID
   * @param orgName 组织名称（支持模糊搜索）
   * @param rootOrgId 根组织ID（为空时查询所有顶级组织）
   * @param maxLevel 最大层级
   * @return 组织树节点列表
   */
  List<OrganizationTreeNodeDTO> queryOrganizationTreeByName(Long spaceId, String orgName, Long rootOrgId, Integer maxLevel);

  /**
   * 查询子组织列表
   *
   * @param spaceId 企业空间ID
   * @param parentOrgId 上级组织ID
   * @return 子组织列表
   */
  List<SimpleOrganizationDTO> queryChildOrganizations(Long spaceId, Long parentOrgId);

  /**
   * 更新组织状态
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @param statusCd 状态
   * @return 更新结果
   */
  ResultVO<Void> updateOrganizationStatus(Long spaceId, Long orgId, String statusCd);

  /**
   * 检查组织编码是否存在
   *
   * @param spaceId 企业空间ID
   * @param orgCode 组织编码
   * @param excludeOrgId 排除的组织ID
   * @return 是否存在
   */
  boolean checkOrgCodeExists(Long spaceId, String orgCode, Long excludeOrgId);

  /**
   * 统计组织成员数量
   *
   * @param spaceId 企业空间ID
   * @param orgIds 组织ID列表
   * @return 成员数量映射
   */
  List<OrganizationDTO> countOrganizationMembers(Long spaceId, List<Long> orgIds);

  /**
   * 逻辑删除组织
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 删除结果
   */
  ResultVO<Void> deleteOrganization(Long spaceId, Long orgId);

  /**
   * 根据组织路径查询组织路径上的所有组织
   *
   * @param spaceId 租户ID
   * @param orgPath 组织路径（用.分割的组织ID路径）
   * @return 组织列表（按路径顺序排序，从根到叶子）
   */
  List<OrganizationDTO> queryOrganizationsByPath(Long spaceId, String orgPath);
}
