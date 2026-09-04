package com.iwhalecloud.bote.document.service;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.model.OrgDTO;
import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.common.space.SpaceContextHolder;
import com.iwhalecloud.bote.doc.integration.PortalOrgIntegration;
import com.iwhalecloud.bote.doc.integration.PortalUserIntegration;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.dto.portal.UserDTO;
import com.iwhalecloud.bote.dto.portal.query.UserQueryParams;
import com.iwhalecloud.bote.mapper.portal.UserManageMapper;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Portal用户集成实现类
 *
 * @author lizuyin
 * @since 2025/9/5
 */
@Primary
@Service
@RequiredArgsConstructor
public class BoteUserIntegrationImpl implements PortalUserIntegration {

  private final UserManageMapper userManageMapper;
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
    // 通过 UserManageMapper 获取用户信息
    UserDTO userDTO = userManageMapper.getUser(userId);
    if (userDTO == null) {
      return null;
    }
    List<OrgDTO> orgList = null;
    if (spaceId != null) {
      orgList = portalOrgIntegration.queryUserOrgList(spaceId, userId);
    }
    PortalUserDTO portalUserDTO = buildPortalUserDTO(userDTO.getUserId(), userDTO.getUserName(), userDTO.getRealName());
    portalUserDTO.setOrgList(orgList);
    return portalUserDTO;
  }

  @Override
  public String findUserNameById(Long userId) {
    UserDTO userDTO = userManageMapper.getUser(userId);
    if (userDTO == null) {
      return null;
    }
    return userDTO.getRealName();
  }

  @Override
  public List<PortalUserDTO> findUserBatchByIds(List<Long> userIdList, Long spaceId) {
    if (userIdList == null || userIdList.isEmpty()) {
      return Collections.emptyList();
    }

    // 通过 UserManageMapper 批量获取用户信息
    List<UserDTO> userDTOList = userManageMapper.selectUserDTOListByUserIds(userIdList);
    if (userDTOList == null || userDTOList.isEmpty()) {
      return Collections.emptyList();
    }

    // 批量获取用户组织信息
    Map<Long, List<OrgDTO>> userOrgMap;
    if (spaceId != null) {
      userOrgMap = portalOrgIntegration.queryUserOrgListBatch(spaceId, userIdList);
    }
    else {
      userOrgMap = Collections.emptyMap();
    }

    // 转换为 PortalUserDTO 列表，并设置组织信息
    return userDTOList.stream().map(userDTO -> {
      PortalUserDTO portalUserDTO = buildPortalUserDTO(userDTO.getUserId(), userDTO.getUserName(), userDTO.getRealName());
      // 设置组织列表，如果没有则为空列表
      portalUserDTO.setOrgList(userOrgMap.getOrDefault(userDTO.getUserId(), Collections.emptyList()));
      return portalUserDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public Map<Long, PortalUserDTO> findUserMapBatchByIds(List<Long> userIdList, Long spaceId) {
    List<PortalUserDTO> userBatchByIds = this.findUserBatchByIds(userIdList, spaceId);
    return userBatchByIds.stream().collect(Collectors.toMap(PortalUserDTO::getUserId, Function.identity()));
  }

  @Override
  public List<PortalUserDTO> getUserListByOrgId(Long orgId, Long spaceId) {
    if (orgId == null) {
      return Collections.emptyList();
    }
    return userManageMapper.selectUserListByOrgId(orgId);
  }

  @Override
  public List<PortalUserDTO> searchUsersByKeyword(String keyWord, Long spaceId) {
    if (keyWord == null || keyWord.trim().isEmpty()) {
      return Collections.emptyList();
    }

    // 创建查询参数
    UserQueryParams queryParams = new UserQueryParams();
    queryParams.setUserName(keyWord.trim());
    queryParams.setSpaceId(spaceId);

    // 调用 mapper 方法进行模糊搜索
    List<UserDTO> userDTOList = userManageMapper.selectUserDTOList(queryParams);
    if (userDTOList == null || userDTOList.isEmpty()) {
      return Collections.emptyList();
    }

    // 转换为 PortalUserDTO 列表，并为每个用户填充组织列表
    return userDTOList.stream().map(userDTO -> {
      List<OrgDTO> orgList = null;
      if (spaceId != null) {
        orgList = portalOrgIntegration.queryUserOrgList(spaceId, userDTO.getUserId());
      }
      PortalUserDTO portalUserDTO = buildPortalUserDTO(userDTO.getUserId(), userDTO.getUserName(), userDTO.getRealName());
      portalUserDTO.setOrgList(orgList);
      return portalUserDTO;
    }).collect(Collectors.toList());
  }

  @NotNull
  private PortalUserDTO buildPortalUserDTO(Long userId, String userName, String realName) {
    PortalUserDTO portalUserDTO = new PortalUserDTO();
    portalUserDTO.setUserId(userId);
    portalUserDTO.setUserCode(userName);
    portalUserDTO.setUserName(realName != null ? realName : userName);
    return portalUserDTO;
  }

  /**
   * 判断是否超级管理员
   *
   * @param userId 用户ID
   */
  @Override
  public boolean isSuperAdmin(Long userId) {
    return SessionUtil.isSuperAdmin(userId);
  }

  @Override
  public String getCurrentSessionId() {
    return SessionUtil.getSessionId();
  }

  @Override
  public Long getCurrentSessionUserId() {
    LoginInfo loginInfo = SessionUtil.getOptionalLoginInfo();
    if (loginInfo == null) {
      return null;
    }
    return loginInfo.getUserId();
  }
}
