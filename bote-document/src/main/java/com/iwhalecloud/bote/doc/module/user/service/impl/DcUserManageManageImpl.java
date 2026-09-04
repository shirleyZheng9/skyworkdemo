package com.iwhalecloud.bote.doc.module.user.service.impl;

import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserSearchResponse;
import com.iwhalecloud.bote.doc.module.user.service.IDcOrgService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserManageService;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author lizuyin
 * @since 2025-09-08
 */
@Service
@RequiredArgsConstructor
public class DcUserManageManageImpl implements IDcUserManageService {

  private final IDcUserService dcUserService;

  private final IDcOrgService dcOrgService;

  @Override
  public OrgUserDTO listUserByOrgWithDefaultOrg(Long orgId, @NonNull Supplier<Long> defaultOrg) {
    OrgUserDTO result = new OrgUserDTO();

    // 处理orgId，取提供的默认组织
    if (orgId == null) {
      orgId = defaultOrg.get();
    }

    if (orgId == null) {
      return result;
    }

    // 获取用户信息
    List<PortalUserDTO> userInfos = dcUserService.getUserListByOrgId(orgId);
    result.setUserInfos(userInfos);

    // 获取组织信息
    List<OrgDTO> orgInfos = dcOrgService.getSubOrgList(orgId);
    result.setOrgInfos(orgInfos);

    // 获取当前组织信息
    List<OrgDTO> pathOrgs = dcOrgService.findOrgPathById(orgId);
    result.setPathOrgs(pathOrgs);

    return result;
  }

  @Override
  public List<PortalUserDTO> searchUsers(String keyword, Long spaceId) {
    return dcUserService.searchUsersByKeyword(keyword, spaceId);
  }

  @Override
  public OrgUserSearchResponse searchOrgUsers(String keyword, Long spaceId) {
    OrgUserSearchResponse result = new OrgUserSearchResponse();

    // 搜索用户
    List<PortalUserDTO> users = dcUserService.searchUsersByKeyword(keyword, spaceId);
    result.setUsers(users);

    // 搜索组织
    List<OrgDTO> orgs = dcOrgService.searchOrgsByKeyword(keyword, spaceId);
    result.setOrgs(orgs);

    return result;
  }
}
