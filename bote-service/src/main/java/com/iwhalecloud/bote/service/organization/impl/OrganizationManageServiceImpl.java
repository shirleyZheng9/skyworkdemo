package com.iwhalecloud.bote.service.organization.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.OrganizationConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationTreeNodeDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationQueryParams;
import com.iwhalecloud.bote.dto.organization.request.OrganizationSearchRequest;
import com.iwhalecloud.bote.entity.organization.OrganizationEntity;
import com.iwhalecloud.bote.mapper.organization.OrgUserRoleMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationManageMapper;
import com.iwhalecloud.bote.mapper.organization.OrganizationMemberMapper;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 组织管理服务实现类
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Service
@RequiredArgsConstructor
public class OrganizationManageServiceImpl implements IOrganizationManageService {

  private final OrganizationManageMapper organizationManageMapper;
  private final OrganizationMemberMapper orgMemberMapper;
  private final OrgUserRoleMapper orgUserRoleMapper;

  @Override
  @Transactional
  public ResultVO<OrganizationDTO> saveOrganization(OrganizationDTO organization) {
    // 参数校验
    Assert.notNull(organization, "组织信息不能为空");
    Assert.notNull(organization.getSpaceId(), "企业空间ID不能为空");
    Assert.hasText(organization.getOrgCode(), "组织编码不能为空");
    Assert.hasText(organization.getOrgName(), "组织名称不能为空");
    // 检查组织编码是否重复
    if (organizationManageMapper.existsOrgCode(organization.getSpaceId(), organization.getOrgCode(), organization.getOrgId())) {
      throw new BssException("组织编码不唯一：" + organization.getOrgCode());
    }
    OrganizationDTO result;
    if (organization.getOrgId() == null) {
      // 禁止手动创建根组织（parent_org_id为null）
      if (organization.getParentOrgId() == null) {
        throw new BssException("不允许手动创建根组织，根组织只能在创建企业时自动创建");
      }
      // 新增组织
      result = insertOrganization(organization);
    }
    else {
      // 更新组织
      result = updateOrganization(organization);
    }
    return ResultVO.success(result);
  }

  /**
   * 创建企业的根组织（仅限系统内部调用）
   *
   * @param spaceId 企业空间ID
   * @param spaceName 企业名称
   * @param creatorId 创建人ID
   * @return 根组织信息
   */
  @Transactional
  public OrganizationDTO createRootOrgForWorkspace(Long spaceId, String spaceName, Long creatorId) {
    // 检查企业是否已有根组织
    if (hasRootOrganization(spaceId)) {
      throw new BssException("企业已存在根组织，不能重复创建");
    }
    // 创建根组织
    OrganizationDTO rootOrg = new OrganizationDTO();
    rootOrg.setSpaceId(spaceId);
    rootOrg.setOrgCode("ROOT_" + spaceId); // 根组织编码
    rootOrg.setOrgName(spaceName); // 根组织名称
    rootOrg.setOrgType("company"); // 根组织类型为公司
    rootOrg.setParentOrgId(null); // 根组织没有父组织
    rootOrg.setOrgDescription("企业根组织，系统自动创建");
    rootOrg.setStatusCd(BaseConsts.STATUS_CD_VALID);
    rootOrg.setSortOrder(0);
    // 设置默认值
    setDefaultValues(rootOrg);
    // 生成主键ID
    rootOrg.setOrgId(Sequences.ORGANIZATION_ID.next());
    // 转换为Entity进行插入
    OrganizationEntity entity = convertToEntity(rootOrg);
    entity.setCreatorId(creatorId);
    int result = organizationManageMapper.insertOrganization(entity);
    if (result <= 0) {
      throw new BssException("创建企业根组织失败");
    }
    // 设置生成的ID
    rootOrg.setOrgId(entity.getOrgId());
    // 计算组织层级和路径（根组织）
    rootOrg.setOrgLevel(OrganizationConsts.ROOT_ORG_LEVEL);
    rootOrg.setOrgPath(String.valueOf(rootOrg.getOrgId()));
    // 更新组织路径
    organizationManageMapper.updateOrganizationLevel(rootOrg.getOrgId(), rootOrg.getOrgLevel(), rootOrg.getOrgPath(), creatorId);
    return rootOrg;
  }

  @Override
  @Transactional
  public void updateRootOrgName(Long spaceId, String spaceName, Long updatorId) {
    organizationManageMapper.updateRootOrgName(spaceId, spaceName, updatorId);
  }

  /**
   * 检查企业是否已有根组织
   *
   * @param spaceId 企业空间ID
   * @return 是否已有根组织
   */
  private boolean hasRootOrganization(Long spaceId) {
    return organizationManageMapper.countRootOrganizations(spaceId) > 0;
  }

  private OrganizationDTO insertOrganization(OrganizationDTO organization) {
    // 设置默认值
    setDefaultValues(organization);
    // 生成主键ID
    if (organization.getOrgId() == null) {
      organization.setOrgId(Sequences.ORGANIZATION_ID.next());
    }
    // 转换为Entity进行插入
    OrganizationEntity entity = convertToEntity(organization);
    entity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    int result = organizationManageMapper.insertOrganization(entity);
    if (result <= 0) {
      throw new BssException("插入组织失败");
    }
    // 设置生成的ID
    organization.setOrgId(entity.getOrgId());
    // 计算组织层级和路径
    calculateOrgLevelAndPath(organization);
    // 更新组织路径
    if (organization.getParentOrgId() == null) {
      // 根组织，更新路径为自身ID
      organization.setOrgPath(String.valueOf(organization.getOrgId()));
      organizationManageMapper.updateOrganizationLevel(organization.getOrgId(), organization.getOrgLevel(), organization.getOrgPath(), SessionUtil.getLoginInfo().getUserId());
    }
    else {
      // 子组织，更新路径
      organizationManageMapper.updateOrganizationLevel(organization.getOrgId(), organization.getOrgLevel(), organization.getOrgPath(), SessionUtil.getLoginInfo().getUserId());
    }
    return organization;
  }

  private OrganizationDTO updateOrganization(OrganizationDTO organization) {
    // 查询原始数据
    OrganizationDTO originalOrg = findOrganization(organization.getSpaceId(), organization.getOrgId());
    if (originalOrg == null) {
      throw new BssException(OrganizationConsts.ERROR_ORG_NOT_EXISTS);
    }
    // 检查是否需要重新计算层级和路径
    if (!Objects.equals(originalOrg.getParentOrgId(), organization.getParentOrgId())) {
      calculateOrgLevelAndPath(organization);
    }
    organization.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    int result = organizationManageMapper.updateOrganization(organization);
    if (result <= 0) {
      throw new BssException("更新组织失败");
    }
    return organization;
  }

  private void setDefaultValues(OrganizationDTO organization) {
    if (StringUtils.isBlank(organization.getStatusCd())) {
      organization.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    if (organization.getSortOrder() == null) {
      organization.setSortOrder(0);
    }
  }

  private void calculateOrgLevelAndPath(OrganizationDTO organization) {
    if (organization.getParentOrgId() == null) {
      // 根组织
      organization.setOrgLevel(OrganizationConsts.ROOT_ORG_LEVEL);
      organization.setOrgPath(String.valueOf(organization.getOrgId()));
    }
    else {
      // 子组织
      OrganizationDTO parentOrg = findOrganization(organization.getSpaceId(), organization.getParentOrgId());
      if (parentOrg == null) {
        throw new BssException("上级组织不存在");
      }
      organization.setOrgLevel(parentOrg.getOrgLevel() + 1);
      organization.setOrgPath(parentOrg.getOrgPath() + OrganizationConsts.ORG_PATH_SEPARATOR + organization.getOrgId());
    }
  }

  private OrganizationEntity convertToEntity(OrganizationDTO dto) {
    OrganizationEntity entity = new OrganizationEntity();
    entity.setOrgId(dto.getOrgId());
    entity.setSpaceId(dto.getSpaceId());
    entity.setOrgCode(dto.getOrgCode());
    entity.setOrgName(dto.getOrgName());
    entity.setOrgType(dto.getOrgType());
    entity.setParentOrgId(dto.getParentOrgId());
    entity.setOrgLevel(dto.getOrgLevel());
    entity.setOrgPath(dto.getOrgPath());
    entity.setStatusCd(dto.getStatusCd());
    entity.setOrgDescription(dto.getOrgDescription());
    entity.setSortOrder(dto.getSortOrder());
    // 处理扩展字段
    if (dto.getExtFieldsMap() != null && !dto.getExtFieldsMap().isEmpty()) {
      entity.setExtFields(JsonUtil.toJsonString(dto.getExtFieldsMap()));
    }
    return entity;
  }

  @Override
  @Nullable
  public OrganizationDTO findOrganization(Long spaceId, Long orgId) {
    return organizationManageMapper.getOrganization(spaceId, orgId);
  }

  @Override
  public List<OrganizationDTO> queryOrganizationList(Long spaceId, List<Long> orgIdList) {
    if (CollectionUtils.isEmpty(orgIdList)) {
      return new ArrayList<>();
    }
    return organizationManageMapper.getOrganizationBatchByIds(spaceId, orgIdList);
  }

  @Override
  @Nullable
  public OrganizationDTO findOrganizationByCode(Long spaceId, String orgCode) {
    return organizationManageMapper.getOrganizationByCode(spaceId, orgCode);
  }

  @Override
  public List<SimpleOrganizationDTO> queryOrganizationList(OrganizationQueryParams queryParams) {
    return organizationManageMapper.selectOrganizationList(queryParams);
  }

  @Override
  public PageInfo<OrganizationDTO> queryOrganizationPage(OrganizationQueryParams queryParams) {
    //noinspection resource
    PageHelper.startPage(queryParams.getPageNum(), queryParams.getPageSize());
    List<SimpleOrganizationDTO> simpleResult = organizationManageMapper.selectOrganizationList(queryParams);
    // 转换为 OrganizationDTO
    List<OrganizationDTO> result = new ArrayList<>();
    for (SimpleOrganizationDTO simple : simpleResult) {
      OrganizationDTO dto = new OrganizationDTO();
      dto.setOrgId(simple.getOrgId());
      dto.setSpaceId(simple.getSpaceId());
      dto.setOrgCode(simple.getOrgCode());
      dto.setOrgName(simple.getOrgName());
      dto.setOrgType(simple.getOrgType());
      dto.setParentOrgId(simple.getParentOrgId());
      dto.setOrgLevel(simple.getOrgLevel());
      dto.setOrgPath(simple.getOrgPath());
      dto.setStatusCd(simple.getStatusCd());
      dto.setSortOrder(simple.getSortOrder());
      result.add(dto);
    }
    return new PageInfo<>(result);
  }

  @Override
  public PageInfo<OrganizationDTO> searchOrganizations(OrganizationSearchRequest searchRequest) {
    OrganizationQueryParams queryParams = new OrganizationQueryParams();
    queryParams.setSpaceId(searchRequest.getSpaceId());
    queryParams.setOrgType(searchRequest.getOrgType());
    queryParams.setParentOrgId(searchRequest.getParentOrgId());
    queryParams.setStatusCd(searchRequest.getStatusCd());
    queryParams.setPageNum(searchRequest.getPage() != null ? searchRequest.getPage() : 1);
    queryParams.setPageSize(searchRequest.getSize() != null ? searchRequest.getSize() : 10);
    return queryOrganizationPage(queryParams);
  }

  @Override
  public List<OrganizationTreeNodeDTO> queryOrganizationTree(Long spaceId, @Nullable Long rootOrgId, @Nullable Integer maxLevel) {
    Assert.notNull(spaceId, "企业ID不能为空");
    if (maxLevel == null || maxLevel <= 0) {
      maxLevel = OrganizationConsts.MAX_ORG_LEVEL;
    }
    return organizationManageMapper.selectOrganizationTree(spaceId, rootOrgId, maxLevel);
  }

  @Override
  public List<OrganizationTreeNodeDTO> queryOrganizationTreeByName(Long spaceId, String orgName, Long rootOrgId, Integer maxLevel) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    if (maxLevel <= 0) {
      maxLevel = OrganizationConsts.MAX_ORG_LEVEL;
    }
    // 1. 先根据名称搜索匹配的组织
    List<OrganizationTreeNodeDTO> matchedOrgs = organizationManageMapper.selectOrganizationTreeByName(spaceId, orgName, rootOrgId, maxLevel);
    if (CollectionUtils.isEmpty(matchedOrgs)) {
      return matchedOrgs;
    }
    // 2. 收集所有需要显示的组织ID（包括匹配的组织及其路径上的所有父级组织）
    Set<Long> allOrgIds = new HashSet<>();
    for (OrganizationTreeNodeDTO org : matchedOrgs) {
      allOrgIds.add(org.getOrgId());
      if (StringUtils.isNotEmpty(org.getOrgPath())) {
        List<Long> pathIds = Arrays.stream(org.getOrgPath().split("\\" + OrganizationConsts.ORG_PATH_SEPARATOR))
          .map(Long::valueOf)
          .toList();
        allOrgIds.addAll(pathIds);
      }
    }
    // 3. 查询所有相关的组织数据（包括父级组织）
    List<OrganizationTreeNodeDTO> allOrgs = organizationManageMapper.selectOrganizationTreeByIds(spaceId, new ArrayList<>(allOrgIds), rootOrgId, maxLevel);
    // 4. 找出真正的根组织（没有父级的组织）
    Set<Long> rootOrgIds = new HashSet<>();
    for (OrganizationTreeNodeDTO org : allOrgs) {
      if (org.getParentOrgId() == null) {
        rootOrgIds.add(org.getOrgId());
      }
    }
    // 5. 构造组织树
    List<OrganizationTreeNodeDTO> result = new ArrayList<>();
    for (OrganizationTreeNodeDTO org : allOrgs) {
      if (rootOrgIds.contains(org.getOrgId())) {
        result.add(org);
        buildOrganizationTree(org, allOrgs);
      }
    }
    return result;
  }

  /**
   * 构建组织树结构
   *
   * @param parentOrg 父组织
   * @param allOrgs 所有组织列表
   */
  private void buildOrganizationTree(OrganizationTreeNodeDTO parentOrg, List<OrganizationTreeNodeDTO> allOrgs) {
    List<OrganizationTreeNodeDTO> children = allOrgs.stream()
      .filter(org -> Objects.equals(parentOrg.getOrgId(), org.getParentOrgId()))
      .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(children)) {
      return;
    }
    parentOrg.setChildren(children);
    for (OrganizationTreeNodeDTO child : children) {
      buildOrganizationTree(child, allOrgs);
    }
  }

  @Override
  public List<SimpleOrganizationDTO> queryChildOrganizations(Long spaceId, Long parentOrgId) {
    return organizationManageMapper.selectChildOrganizations(spaceId, parentOrgId);
  }

  @Override
  @Transactional
  public ResultVO<Void> updateOrganizationStatus(Long spaceId, Long orgId, String statusCd) {
    int result = organizationManageMapper.updateOrganizationStatus(orgId, statusCd, SessionUtil.getLoginInfo().getUserId());
    if (result <= 0) {
      throw new BssException("更新组织状态失败");
    }
    return ResultVO.success();
  }

  @Override
  public boolean checkOrgCodeExists(Long spaceId, String orgCode, Long excludeOrgId) {
    return organizationManageMapper.existsOrgCode(spaceId, orgCode, excludeOrgId);
  }


  @Override
  public List<OrganizationDTO> countOrganizationMembers(Long spaceId, List<Long> orgIds) {
    if (CollectionUtils.isEmpty(orgIds)) {
      return new ArrayList<>();
    }
    return organizationManageMapper.selectOrganizationMemberCounts(spaceId, orgIds);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteOrganization(Long spaceId, Long orgId) {
    // 检查组织是否存在
    OrganizationDTO organization = findOrganization(spaceId, orgId);
    if (organization == null) {
      throw new BssException("组织不存在");
    }
    // 检查是否有子组织
    List<SimpleOrganizationDTO> children = queryChildOrganizations(spaceId, orgId);
    if (!CollectionUtils.isEmpty(children)) {
      throw new BssException("组织下存在子组织，无法删除");
    }
    // 逻辑删除：将状态改为无效
    int result = organizationManageMapper.updateOrganizationStatus(orgId, BaseConsts.STATUS_CD_INVALID, SessionUtil.getLoginInfo().getUserId());
    if (result <= 0) {
      throw new BssException("删除组织失败");
    }
    // 同步删除组织下的成员
    deleteOrgMember(spaceId, orgId);
    return ResultVO.success();
  }

  /**
   * 删除组织下的成员
   */
  private void deleteOrgMember(Long spaceId, Long orgId) {
    // 查询组织下的成员
    List<OrganizationMemberDTO> memberList = orgMemberMapper.selectMemberListByOrgId(spaceId, orgId);
    if (memberList.isEmpty()) {
      return;
    }
    // 删除组织下的成员
    List<Long> memberIds = memberList.stream().map(OrganizationMemberDTO::getMemberId).toList();
    orgMemberMapper.batchDeleteOrganizationMembers(spaceId, memberIds, SessionUtil.getLoginInfo().getUserId());
    // 查询用户是否已经还在当前企业下的其它组织
    List<Long> notExistOrgUserList = new ArrayList<>();
    Set<Long> userIdSet = memberList.stream().map(OrganizationMemberDTO::getUserId).collect(Collectors.toSet());
    for (Long userId : userIdSet) {
      if (!orgMemberMapper.existUserOrgMember(spaceId, userId)) {
        notExistOrgUserList.add(userId);
      }
    }
    // 删除不在当前企业下其它组织的用户组织角色
    if (!notExistOrgUserList.isEmpty()) {
      orgUserRoleMapper.batchDeleteOrgUserRole(spaceId, notExistOrgUserList, SessionUtil.getLoginInfo().getUserId());
    }
  }

  @Override
  public List<OrganizationDTO> queryOrganizationsByPath(Long spaceId, String orgPath) {
    if (StringUtils.isBlank(orgPath)) {
      return new ArrayList<>();
    }

    // 解析组织路径，分割成组织ID列表
    String[] orgIdStrs = orgPath.split("\\.");
    List<Long> orgIdList = Arrays.stream(orgIdStrs)
        .filter(StringUtils::isNotBlank)
        .map(Long::valueOf)
        .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(orgIdList)) {
      return new ArrayList<>();
    }

    // 查询组织路径上的所有组织
    return organizationManageMapper.getOrganizationBatchByPath(spaceId, orgIdList);
  }
}
