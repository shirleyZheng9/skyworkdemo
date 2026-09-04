package com.iwhalecloud.bote.document.service;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.OrgTreeDTO;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.dto.organization.OrganizationDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationTreeNodeDTO;
import com.iwhalecloud.bote.dto.organization.SimpleOrganizationDTO;
import com.iwhalecloud.bote.dto.organization.query.OrganizationQueryParams;
import com.iwhalecloud.bote.service.organization.IOrganizationManageService;
import com.iwhalecloud.bote.service.organization.IOrganizationMemberService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 门户组织集成实现类
 *
 * @author lizuyin
 * @since 2025/9/6
 */
@Primary
@Service
@RequiredArgsConstructor
public class BoteOrgIntegrationImpl implements PortalOrgIntegration {
  private final IOrganizationManageService organizationManageService;
  private final IOrganizationMemberService organizationMemberService;

  /**
   * 将OrganizationDTO转换为OrgDTO
   */
  private OrgDTO convertToOrgDTO(OrganizationDTO organizationDTO) {
    if (organizationDTO == null) {
      return null;
    }
    OrgDTO orgDTO = new OrgDTO();
    orgDTO.setOrgId(organizationDTO.getOrgId());
    orgDTO.setOrgName(organizationDTO.getOrgName());
    orgDTO.setOrgLevel(organizationDTO.getOrgLevel());
    orgDTO.setParentOrgId(organizationDTO.getParentOrgId());
    orgDTO.setOrgPath(organizationDTO.getOrgPath());
    return orgDTO;
  }

  /**
   * 将SimpleOrganizationDTO转换为OrgDTO
   */
  private OrgDTO convertToOrgDTO(SimpleOrganizationDTO simpleOrgDTO) {
    if (simpleOrgDTO == null) {
      return null;
    }
    OrgDTO orgDTO = new OrgDTO();
    orgDTO.setOrgId(simpleOrgDTO.getOrgId());
    orgDTO.setOrgName(simpleOrgDTO.getOrgName());
    orgDTO.setOrgLevel(simpleOrgDTO.getOrgLevel());
    return orgDTO;
  }

  /**
   * 将OrganizationTreeNodeDTO转换为OrgTreeDTO
   */
  private OrgTreeDTO convertToOrgTreeDTO(OrganizationTreeNodeDTO treeNodeDTO) {
    if (treeNodeDTO == null) {
      return null;
    }
    OrgTreeDTO orgTreeDTO = new OrgTreeDTO();
    orgTreeDTO.setOrgId(treeNodeDTO.getOrgId());
    orgTreeDTO.setOrgName(treeNodeDTO.getOrgName());
    orgTreeDTO.setOrgLevel(treeNodeDTO.getOrgLevel());
    if (treeNodeDTO.getChildren() != null && !treeNodeDTO.getChildren().isEmpty()) {
      orgTreeDTO.setChildren(
        treeNodeDTO.getChildren().stream().map(this::convertToOrgTreeDTO).collect(Collectors.toList()));
    }
    return orgTreeDTO;
  }

  @Override
  public OrgDTO findById(Long spaceId, Long orgId) {
    if (orgId == null) {
      return null;
    }
    OrganizationDTO organizationDTO = organizationManageService.findOrganization(spaceId, orgId);
    return convertToOrgDTO(organizationDTO);
  }

  @Override
  public List<OrgDTO> findBatchById(Long spaceId, List<Long> orgIdList) {
    if (CollectionUtils.isEmpty(orgIdList)) {
      return Collections.emptyList();
    }
    List<OrganizationDTO> organizationDTOs = organizationManageService.queryOrganizationList(spaceId, orgIdList);
    return organizationDTOs.stream().map(this::convertToOrgDTO).collect(Collectors.toList());
  }

  @Override
  public List<OrgDTO> queryUserOrgList(Long spaceId, Long userId) {
    if (userId == null) {
      return Collections.emptyList();
    }
    List<OrganizationMemberDTO> memberDTOs = organizationMemberService.queryUserOrganizations(spaceId, userId);
    return memberDTOs.stream().filter(member -> member.getOrganizationInfo() != null)
      .peek(member -> member.getOrganizationInfo().setOrgId(member.getOrgId()))
      .map(member -> convertToOrgDTO(member.getOrganizationInfo())).collect(Collectors.toList());
  }

  @Override
  public Map<Long, List<OrgDTO>> queryUserOrgListBatch(Long spaceId, List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return Collections.emptyMap();
    }

    // 批量查询所有用户的组织信息
    List<OrganizationMemberDTO> memberDTOs = organizationMemberService.queryUserOrganizationsBatch(spaceId, userIds);
    if (CollectionUtils.isEmpty(memberDTOs)) {
      return Collections.emptyMap();
    }

    // 按用户ID分组，并转换为OrgDTO列表
    Map<Long, List<OrgDTO>> result = new HashMap<>();
    Map<Long, List<OrganizationMemberDTO>> memberMap = memberDTOs.stream()
      .collect(Collectors.groupingBy(OrganizationMemberDTO::getUserId));

    for (Map.Entry<Long, List<OrganizationMemberDTO>> entry : memberMap.entrySet()) {
      List<OrgDTO> orgList = entry.getValue().stream()
        .filter(member -> member.getOrganizationInfo() != null)
        .peek(member -> member.getOrganizationInfo().setOrgId(member.getOrgId()))
        .map(member -> convertToOrgDTO(member.getOrganizationInfo()))
        .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(orgList)) {
        result.put(entry.getKey(), orgList);
      }
    }
    return result;
  }

  @Override
  public List<OrgTreeDTO> queryAllOrgTreeList(Long spaceId) {
    List<OrganizationTreeNodeDTO> treeNodeDTOs = organizationManageService.queryOrganizationTree(spaceId, null, null);
    return treeNodeDTOs.stream().map(this::convertToOrgTreeDTO).collect(Collectors.toList());
  }

  @Override
  public List<OrgDTO> getSubOrgList(Long spaceId, Long orgId) {
    if (orgId == null) {
      return Collections.emptyList();
    }
    List<SimpleOrganizationDTO> childOrgs = organizationManageService.queryChildOrganizations(spaceId, orgId);
    return childOrgs.stream().map(this::convertToOrgDTO).collect(Collectors.toList());
  }

  @Override
  public List<OrgDTO> searchOrgsByKeyword(String keyWord, Long spaceId) {
    if (StringUtils.isBlank(keyWord)) {
      return Collections.emptyList();
    }

    // 构造查询参数，通过组织名称模糊搜索
    OrganizationQueryParams queryParams = new OrganizationQueryParams();
    queryParams.setOrgName(keyWord.trim());
    queryParams.setStatusCd(BaseConsts.STATUS_CD_VALID);
    queryParams.setSpaceId(spaceId);

    // 调用组织管理服务的查询方法
    List<SimpleOrganizationDTO> orgList = organizationManageService.queryOrganizationList(queryParams);
    if (CollectionUtils.isEmpty(orgList)) {
      return Collections.emptyList();
    }

    return orgList.stream().map(this::convertToOrgDTO).collect(Collectors.toList());
  }

  @Override
  public List<OrgDTO> queryOrganizationsByPath(Long spaceId, String orgPath) {
    List<OrganizationDTO> organizationDTOs = organizationManageService.queryOrganizationsByPath(spaceId, orgPath);
    return organizationDTOs.stream()
      .map(this::convertToOrgDTO)
      .sorted((o1, o2) -> {
        if (o1.getOrgLevel() == null || o2.getOrgLevel() == null) {
          return 0;
        }
        if (o1.getOrgLevel() > o2.getOrgLevel()) {
          return 1;
        }
        if (o1.getOrgLevel().equals(o2.getOrgLevel())) {
          Integer o1PathLevel = Optional.ofNullable(o1.getOrgPath())
            .map(item -> item.split("\\.").length)
            .orElse(99);
          Integer o2PathLevel = Optional.ofNullable(o2.getOrgPath())
            .map(item -> item.split("\\.").length)
            .orElse(99);
          return o1PathLevel - o2PathLevel;
        }
        return -1;
      })
      .collect(Collectors.toList());
  }

  @Override
  public Set<Long> queryUserIdsByOrgIdsWithSubOrgs(Long spaceId, List<Long> orgIds) {
    if (CollectionUtils.isEmpty(orgIds)) {
      return Collections.emptySet();
    }
    Set<Long> allOrgIds = new HashSet<>(orgIds);
    collectSubOrgIds(spaceId, orgIds, allOrgIds);
    return organizationMemberService.queryMemberListByOrgIds(spaceId, new ArrayList<>(allOrgIds))
      .stream()
      .map(OrganizationMemberDTO::getUserId)
      .collect(Collectors.toSet());
  }

  /**
   * 递归收集子组织ID
   */
  private void collectSubOrgIds(Long spaceId, List<Long> orgIds, Set<Long> allOrgIds) {
    for (Long orgId : orgIds) {
      List<OrgDTO> subOrgs = getSubOrgList(spaceId, orgId);
      if (CollectionUtils.isEmpty(subOrgs)) {
        continue;
      }
      List<Long> newSubOrgIds = subOrgs.stream()
        .map(OrgDTO::getOrgId)
        .filter(allOrgIds::add)
        .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(newSubOrgIds)) {
        collectSubOrgIds(spaceId, newSubOrgIds, allOrgIds);
      }
    }
  }
}
