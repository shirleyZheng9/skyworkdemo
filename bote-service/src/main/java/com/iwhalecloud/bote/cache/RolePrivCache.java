package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.dto.portal.SimpleRolePrivDTO;
import com.iwhalecloud.bote.dto.portal.SimpleRolePrivInfoDTO;
import com.iwhalecloud.bote.mapper.portal.RolePrivManageMapper;
import com.iwhalecloud.bss.litchi.cache.refresh.Refreshable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 用户角色权限缓存
 *
 * @author chen.linfa
 * @since 2024-10-14
 */
@Component
@RequiredArgsConstructor
public class RolePrivCache implements Refreshable {
  private final RolePrivManageMapper rolePrivManageMapper;
  /** 角色和权限映射，key 为角色编码 */
  private volatile Map<String, SimpleRolePrivInfoDTO> rolePrivMap;

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_ROLE_PRIV;
  }

  /**
   * 获取角色和权限映射
   */
  public Map<String, SimpleRolePrivInfoDTO> getRolePrivMap() {
    if (rolePrivMap == null) {
      updateRolePrivMap();
    }
    return rolePrivMap;
  }

  @Override
  public boolean isDistributedCacheEnabled() {
    return false;
  }

  @Override
  public void refreshLocalCache() {
    updateRolePrivMap();
  }

  @Override
  public void refreshLocalCache(List<String> keys) {
    updateRolePrivMap();
  }

  @Override
  public void refresh() {
    updateRolePrivMap();
  }

  @Override
  public void refresh(List<String> keys) {
    updateRolePrivMap();
  }

  @Override
  public Object getLocalCache(String key) {
    return rolePrivMap;
  }

  /**
   * 更新角色和权限映射
   */
  private void updateRolePrivMap() {
    List<SimpleRolePrivDTO> rolePrivList = rolePrivManageMapper.selectAllPrivList();
    // 先构建临时映射，避免并发问题
    Map<String, SimpleRolePrivInfoDTO> tmpMap = new HashMap<>();
    Set<String> roles = rolePrivList.stream().map(SimpleRolePrivDTO::getRoleCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
    for (String role : roles) {
      List<String> menus = new ArrayList<>();
      List<String> components = new ArrayList<>();
      for (SimpleRolePrivDTO priv : rolePrivList) {
        if (!role.equals(priv.getRoleCode())) {
          continue;
        }
        if (PrivConsts.PRIV_TYPE_MENU.equals(priv.getPrivType())) {
          menus.add(priv.getPrivUrl());
        }
        else if (PrivConsts.PRIV_TYPE_COMPONENT.equals(priv.getPrivType())) {
          components.add(priv.getPrivCode());
        }
      }
      tmpMap.put(role, new SimpleRolePrivInfoDTO(menus, components));
    }
    this.rolePrivMap = tmpMap;
  }

}
