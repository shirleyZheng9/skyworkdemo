package com.iwhalecloud.bote.mapper.organization;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationTreeNodeDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationQueryParams;
import com.iwhalecloud.bote.entity.organization.OrganizationEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 组织管理 Mapper
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
public interface OrganizationManageMapper {

  /**
   * 校验组织编码是否已存在
   *
   * @param spaceId 企业空间ID
   * @param orgCode 组织编码
   * @param orgId 排除的组织ID
   * @return 是否存在
   */
  boolean existsOrgCode(@Param("spaceId") Long spaceId, @Param("orgCode") String orgCode, @Param("orgId") Long orgId);

  /**
   * 统计企业的根组织数量
   *
   * @param spaceId 企业空间ID
   * @return 根组织数量
   */
  int countRootOrganizations(@Param("spaceId") Long spaceId);

  /**
   * 根据主键获取组织
   *
   * @param spaceId 企业空间ID
   * @param orgId 组织ID
   * @return 组织信息
   */
  @Nullable
  OrganizationDTO getOrganization(@Param("spaceId") Long spaceId, @Param("orgId") Long orgId);

  /**
   * 根据组织编码获取组织
   *
   * @param spaceId 企业空间ID
   * @param orgCode 组织编码
   * @return 组织信息
   */
  @Nullable
  OrganizationDTO getOrganizationByCode(@Param("spaceId") Long spaceId, @Param("orgCode") String orgCode);

  /**
   * 根据主键批量获取组织
   *
   * @param spaceId 企业空间ID
   * @param orgIdList 组织ID列表
   * @return 组织信息列表
   */
  List<OrganizationDTO> getOrganizationBatchByIds(@Param("spaceId") Long spaceId, @Param("orgIdList") List<Long> orgIdList);

  /**
   * 新增组织
   *
   * @param organization 组织信息
   * @return 影响行数
   */
  int insertOrganization(@Param("dto") OrganizationEntity organization);

  /**
   * 修改组织
   *
   * @param organization 组织信息
   * @return 影响行数
   */
  int updateOrganization(@Param("dto") OrganizationDTO organization);

  /**
   * 更新组织层级和路径
   *
   * @param orgId 组织ID
   * @param orgLevel 组织层级
   * @param orgPath 组织路径
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int updateOrganizationLevel(@Param("orgId") Long orgId, @Param("orgLevel") Integer orgLevel,
                              @Param("orgPath") String orgPath, @Param("updatorId") Long updatorId);

  /**
   * 更新组织状态
   *
   * @param orgId 组织ID
   * @param statusCd 状态
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int updateOrganizationStatus(@Param("orgId") Long orgId, @Param("statusCd") String statusCd, @Param("updatorId") Long updatorId);

  /**
   * 更新根组织名称
   *
   * @param spaceId 企业空间ID
   * @param spaceName 企业名称
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int updateRootOrgName(@Param("spaceId") Long spaceId, @Param("spaceName") String spaceName, @Param("updatorId") Long updatorId);

  /**
   * 查询组织列表
   *
   * @param queryParams 查询参数
   * @return 组织列表
   */
  List<SimpleOrganizationDTO> selectOrganizationList(@Param("query") OrganizationQueryParams queryParams);

  /**
   * 分页查询组织列表
   *
   * @param queryParams 查询参数
   * @param rowBounds 分页参数
   * @return 组织分页列表
   */
  Page<OrganizationDTO> selectOrganizationPage(@Param("query") OrganizationQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询组织树结构
   *
   * @param spaceId 企业空间ID
   * @param rootOrgId 根组织ID
   * @param maxLevel 最大层级
   * @return 组织树节点列表
   */
  List<OrganizationTreeNodeDTO> selectOrganizationTree(@Param("spaceId") Long spaceId,
                                                        @Param("rootOrgId") Long rootOrgId,
                                                        @Param("maxLevel") Integer maxLevel);

  /**
   * 按名称搜索查询组织树结构
   *
   * @param spaceId 企业空间ID
   * @param orgName 组织名称
   * @param rootOrgId 根组织ID
   * @param maxLevel 最大层级
   * @return 组织树节点列表
   */
  List<OrganizationTreeNodeDTO> selectOrganizationTreeByName(@Param("spaceId") Long spaceId,
                                                              @Param("orgName") String orgName,
                                                              @Param("rootOrgId") Long rootOrgId,
                                                              @Param("maxLevel") Integer maxLevel);

  /**
   * 根据组织ID列表查询组织树结构
   *
   * @param spaceId 企业空间ID
   * @param orgIds 组织ID列表
   * @param rootOrgId 根组织ID
   * @param maxLevel 最大层级
   * @return 组织树节点列表
   */
  List<OrganizationTreeNodeDTO> selectOrganizationTreeByIds(@Param("spaceId") Long spaceId,
                                                             @Param("orgIds") List<Long> orgIds,
                                                             @Param("rootOrgId") Long rootOrgId,
                                                             @Param("maxLevel") Integer maxLevel);

  /**
   * 查询子组织列表
   *
   * @param spaceId 企业空间ID
   * @param parentOrgId 上级组织ID
   * @return 子组织列表
   */
  List<SimpleOrganizationDTO> selectChildOrganizations(@Param("spaceId") Long spaceId, @Param("parentOrgId") Long parentOrgId);

  /**
   * 统计组织成员数量
   *
   * @param spaceId 企业空间ID
   * @param orgIds 组织ID列表
   * @return 成员数量映射 orgId -> memberCount
   */
  List<OrganizationDTO> selectOrganizationMemberCounts(@Param("spaceId") Long spaceId, @Param("orgIds") List<Long> orgIds);

  /**
   * 查询企业的根组织
   *
   * @param spaceId 企业空间ID
   * @return 租户对应的根组织
   */
  OrganizationDTO selectSpaceRootOrg(@Param("spaceId") Long spaceId);

  /**
   * 根据组织路径获取组织
   *
   * @param spaceId 租户ID
   * @param orgIdList 组织ID列表
   * @return 组织信息列表
   */
  List<OrganizationDTO> getOrganizationBatchByPath(@Param("spaceId") Long spaceId, @Param("orgIdList") List<Long> orgIdList);

  /**
   * 根据组织ID列表获取组织基本信息
   *
   * @param orgIdList 组织ID列表
   * @return 组织信息列表
   */
  List<OrganizationDTO> getOrgBasicInfoByIds(@Param("orgIdList") List<Long> orgIdList);

}
