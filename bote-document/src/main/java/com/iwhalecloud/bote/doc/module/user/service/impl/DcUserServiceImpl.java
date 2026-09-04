package com.iwhalecloud.bote.doc.module.user.service.impl;

import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.integration.PortalUserIntegration;
import com.iwhalecloud.bote.doc.module.user.service.IDcUserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 提供对 PortalUserIntegration 的统一调用实现
 *
 * @author lizuyin
 * @since 2025/09/08
 */
@Service
@RequiredArgsConstructor
public class DcUserServiceImpl implements IDcUserService {
  private final PortalUserIntegration portalUserIntegration;

  @Override
  public Long getCurrentSessionUserId() {
    return portalUserIntegration.getCurrentSessionUserId();
  }

  @Override
  public PortalUserDTO getCurrentSessionUser() {
    return portalUserIntegration.getCurrentSessionUser();
  }

  @Override
  public PortalUserDTO findUserById(Long userId) {
    return portalUserIntegration.findUserById(userId, SpaceContextHolder.getRequiredSpaceId());
  }

  @Override
  public String findUserNameById(Long userId) {
    return portalUserIntegration.findUserNameById(userId);
  }

  @Override
  public List<PortalUserDTO> findUserBatchByIds(List<Long> userIdList) {
    return portalUserIntegration.findUserBatchByIds(userIdList, SpaceContextHolder.getRequiredSpaceId());
  }

  @Override
  public Map<Long, PortalUserDTO> findUserMapBatchByIds(List<Long> userIdList) {
    Long spaceId = SpaceContextHolder.getRequiredSpaceId();
    return portalUserIntegration.findUserMapBatchByIds(userIdList, spaceId);
  }

  @Override
  public List<PortalUserDTO> getUserListByOrgId(Long orgId) {
    return portalUserIntegration.getUserListByOrgId(orgId, SpaceContextHolder.getRequiredSpaceId());
  }

  @Override
  public List<PortalUserDTO> searchUsersByKeyword(String keyWord, Long spaceId) {
    return portalUserIntegration.searchUsersByKeyword(keyWord, spaceId);
  }

  /**
   * 判断是否超级管理员
   */
  @Override
  public boolean isSuperAdmin(Long userId) {
   return portalUserIntegration.isSuperAdmin(userId);
  }

  @Override
  public String getCurrentSessionId() {
    return portalUserIntegration.getCurrentSessionId();
  }
}
