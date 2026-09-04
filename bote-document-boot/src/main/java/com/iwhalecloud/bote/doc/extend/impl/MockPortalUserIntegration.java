package com.iwhalecloud.bote.doc.extend.impl;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.integration.PortalUserIntegration;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
@RequiredArgsConstructor
public class MockPortalUserIntegration implements PortalUserIntegration {

  private final PortalOrgIntegration portalOrgIntegration;

  @Override
  public PortalUserDTO getCurrentSessionUser() {
    LoginInfo loginInfo = SessionUtil.getLoginInfo();
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    List<OrgDTO> orgList = portalOrgIntegration.queryUserOrgList(spaceId, loginInfo.getUserId());
    PortalUserDTO portalUserDTO = buildPortalUserDTO(loginInfo.getUserId(), loginInfo.getUserName(), loginInfo.getRealName());
    portalUserDTO.setOrgList(orgList);
    return portalUserDTO;
  }

  @Override
  public PortalUserDTO findUserById(Long userId, Long spaceId) {
    return null;
  }

  @Override
  public String findUserNameById(Long userId) {
    return "";
  }

  @Override
  public List<PortalUserDTO> findUserBatchByIds(List<Long> userIdList, Long spaceId) {
    return List.of();
  }

  @Override
  public Map<Long, PortalUserDTO> findUserMapBatchByIds(List<Long> userIdList, Long spaceId) {
    return Map.of();
  }

  @Override
  public List<PortalUserDTO> getUserListByOrgId(Long orgId, Long spaceId) {
    return List.of();
  }

  @Override
  public List<PortalUserDTO> searchUsersByKeyword(String keyWord, Long spaceId) {
    return List.of();
  }

  @Override
  public boolean isSuperAdmin(Long userId) {
    return false;
  }

  @NotNull
  private PortalUserDTO buildPortalUserDTO(Long userId, String userName, String realName) {
    PortalUserDTO portalUserDTO = new PortalUserDTO();
    portalUserDTO.setUserId(userId);
    portalUserDTO.setUserCode(userName);
    portalUserDTO.setUserName(realName != null ? realName : userName);
    return portalUserDTO;
  }
}
