package com.iwhalecloud.bote.doc.module.user.service;

import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import java.util.List;
import java.util.Map;

/**
 * 门户用户信息查询
 *
 * @author Aiqing
 * @since 2025/8/18
 */
public interface IDcUserService {

  /**
   * 获取当前session用户
   *
   * @return 用户信息
   */
  PortalUserDTO getCurrentSessionUser();

  /**
   * 获取当前session用户
   *
   * @return 用户信息
   */
  Long getCurrentSessionUserId();

  /**
   * 根据用户ID查询用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  PortalUserDTO findUserById(Long userId);

  /**
   * 查询用户名称
   *
   * @param userId 用户ID
   * @return 用户名称
   */
  String findUserNameById(Long userId);

  /**
   * 批量查询用户信息
   *
   * @param userIdList 用户ID集合
   * @return 用户信息集合
   */
  List<PortalUserDTO> findUserBatchByIds(List<Long> userIdList);

  /**
   * 批量查询用户信息并转换为Map
   *
   * @param userIdList 用户ID集合
   * @return 用户信息map
   */
  Map<Long, PortalUserDTO> findUserMapBatchByIds(List<Long> userIdList);

  /**
   * 根据组织ID查询组织成员列表
   *
   * @param orgId 组织ID
   * @return 组织成员列表
   */
  List<PortalUserDTO> getUserListByOrgId(Long orgId);

  /**
   * 根据关键词模糊搜索用户
   *
   * @param keyWord 搜索关键词（用户名或真实姓名）
   * @param spaceId 企业空间ID
   * @return 用户信息列表
   */
  List<PortalUserDTO> searchUsersByKeyword(String keyWord, Long spaceId);

  /**
   * 判断是否超级管理员
   *
   * @param userId 用户ID
   */
  boolean isSuperAdmin(Long userId);

  /**
   * 获取当前登录用户sessionId
   *
   * @return sessionId
   */
  String getCurrentSessionId();

}
