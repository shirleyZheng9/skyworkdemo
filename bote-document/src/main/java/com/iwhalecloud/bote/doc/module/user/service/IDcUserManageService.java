package com.iwhalecloud.bote.doc.module.user.service;

import com.iwhalecloud.bote.doc.common.model.PortalUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserDTO;
import com.iwhalecloud.bote.doc.module.user.dto.OrgUserSearchResponse;
import java.util.List;
import java.util.function.Supplier;

/**
 * 用户服务接口
 *
 * @author lizuyin
 * @since 2025-09-06
 */
public interface IDcUserManageService {

  /**
   * 获取组织成员列表
   *
   * @param orgId 组织ID，如果为空则获取当前用户所在组织的成员
   * @return 组织成员信息
   */
  OrgUserDTO listUserByOrgWithDefaultOrg(Long orgId, Supplier<Long> defaultOrg);

  /**
   * 根据关键词搜索用户
   *
   * @param keyword 关键词
   * @param spaceId 企业空间ID
   * @return 用户信息
   */
  List<PortalUserDTO> searchUsers(String keyword, Long spaceId);

  /**
   * 根据关键词同时搜索组织和用户
   *
   * @param keyword 关键词
   * @param spaceId 企业空间ID
   * @return 组织和用户搜索结果
   */
  OrgUserSearchResponse searchOrgUsers(String keyword, Long spaceId);
}
