package com.iwhalecloud.bote.doc.extend.impl;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.OrgTreeDTO;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
public class MockPortalOrgIntegration implements PortalOrgIntegration {
  @Override
  public OrgDTO findById(Long spaceId, Long orgId) {
    return null;
  }

  @Override
  public List<OrgDTO> findBatchById(Long spaceId, List<Long> orgIdList) {
    return List.of();
  }

  @Override
  public List<OrgDTO> queryUserOrgList(Long spaceId, Long userId) {
    return List.of();
  }

  @Override
  public Map<Long, List<OrgDTO>> queryUserOrgListBatch(Long spaceId, List<Long> userIds) {
    return Map.of();
  }

  @Override
  public List<OrgTreeDTO> queryAllOrgTreeList(Long spaceId) {
    return List.of();
  }

  @Override
  public List<OrgDTO> getSubOrgList(Long spaceId, Long orgId) {
    return List.of();
  }

  @Override
  public List<OrgDTO> searchOrgsByKeyword(String keyWord, Long spaceId) {
    return List.of();
  }

  @Override
  public List<OrgDTO> queryOrganizationsByPath(Long spaceId, String orgPath) {
    return List.of();
  }

  @Override
  public Set<Long> queryUserIdsByOrgIdsWithSubOrgs(Long spaceId, List<Long> orgIds) {
    return Set.of();
  }
}
