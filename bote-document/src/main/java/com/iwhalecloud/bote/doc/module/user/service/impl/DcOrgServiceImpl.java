package com.iwhalecloud.bote.doc.module.user.service.impl;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.OrgTreeDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * @author Aiqing
 * @since 2025/9/8
 */
@Service
@RequiredArgsConstructor
public class DcOrgServiceImpl implements IDcOrgService {

  private final PortalOrgIntegration portalOrgIntegration;

  @Override
  public OrgDTO findById(Long orgId) {
    return portalOrgIntegration.findById(SpaceContextHolder.getRequiredSpaceId(), orgId);
  }

  @Override
  public List<OrgDTO> findBatchById(List<Long> orgIdList) {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    return portalOrgIntegration.findBatchById(spaceId, orgIdList);
  }

  @Override
  public List<OrgDTO> queryUserOrgList(Long userId) {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    return portalOrgIntegration.queryUserOrgList(spaceId, userId);
  }

  @Override
  public List<Long> queryUserOrgIdList(Long userId) {
    List<OrgDTO> orgList = this.queryUserOrgList(userId);
    if (CollectionUtils.isEmpty(orgList)) {
      return Collections.emptyList();
    }
    return orgList.stream().map(OrgDTO::getOrgId).collect(Collectors.toList());
  }

  @Override
  public List<OrgTreeDTO> queryAllOrgTreeList() {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    return portalOrgIntegration.queryAllOrgTreeList(spaceId);
  }

  @Override
  public List<OrgDTO> getSubOrgList(Long orgId) {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    return portalOrgIntegration.getSubOrgList(spaceId, orgId);
  }

  @Override
  public List<OrgDTO> searchOrgsByKeyword(String keyWord, Long spaceId) {
    return portalOrgIntegration.searchOrgsByKeyword(keyWord, spaceId);
  }

  @Override
  public List<OrgDTO> findOrgPathById(Long orgId) {
    if (orgId == null) {
      return Collections.emptyList();
    }
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    OrgDTO targetOrg = portalOrgIntegration.findById(spaceId, orgId);
    if (targetOrg == null || targetOrg.getOrgPath() == null) {
      return Collections.emptyList();
    }

    // 使用org_path字段一次性查询所有组织，避免循环调用和死循环风险
    return portalOrgIntegration.queryOrganizationsByPath(spaceId, targetOrg.getOrgPath());
  }
}
