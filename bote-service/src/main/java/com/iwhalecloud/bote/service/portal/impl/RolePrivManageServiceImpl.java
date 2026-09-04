package com.iwhalecloud.bote.service.portal.impl;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.portal.RolePrivDTO;
import com.iwhalecloud.bote.dto.portal.SimplePortalMenuDTO;
import com.iwhalecloud.bote.dto.portal.SimpleRolePrivDTO;
import com.iwhalecloud.bote.dto.portal.query.RolePrivQueryParams;
import com.iwhalecloud.bote.mapper.portal.RolePrivManageMapper;
import com.iwhalecloud.bote.service.portal.IMenuManageService;
import com.iwhalecloud.bote.service.portal.IRolePrivManageService;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 角色权限管理服务实现
 *
 * @author chen.linfa
 * @since 2025-10-09
 */
@Service
@RequiredArgsConstructor
public class RolePrivManageServiceImpl implements IRolePrivManageService {

  private final RolePrivManageMapper rolePrivManageMapper;
  private final IMenuManageService menuManageService;
  private final ITenantManageService tenantManageService;
  private final TenantSettingInfoCache tenantSettingInfoCache;

  @Override
  @Transactional
  public ResultVO<Void> saveRolePriv(RolePrivQueryParams params) {
    List<Long> privIds = rolePrivManageMapper.selectPrivIdsByRoleCode(params.getRoleCode());
    List<RolePrivDTO> rolePrivs = new ArrayList<>();
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (Long privId : params.getPrivIds()) {
      if (privIds.contains(privId)) {
        continue;
      }
      RolePrivDTO rolePriv = new RolePrivDTO();
      rolePriv.setRelaId(Sequences.ROLE_PRIV_ID.next());
      rolePriv.setPrivId(privId);
      rolePriv.setRoleCode(params.getRoleCode());
      rolePriv.setStatusCd(BaseConsts.STATUS_CD_VALID);
      rolePriv.setCreatorId(userId);
      rolePriv.setUpdatorId(userId);
      rolePrivs.add(rolePriv);
    }

    if (CollectionUtils.isNotEmpty(rolePrivs)) {
      rolePrivManageMapper.batchInsertRolePriv(rolePrivs);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteRolePriv(RolePrivQueryParams params) {
    rolePrivManageMapper.deleteRolePriv(params.getRoleCode(), params.getPrivIds(), SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<SimpleRolePrivDTO> queryRolePrivList(RolePrivQueryParams queryParams) {
    return rolePrivManageMapper.selectRolePrivList(queryParams.getRoleCode(), queryParams.getPrivType());
  }

  @Override
  public Map<String, Object> queryPortal(Long spaceId, @Nullable Long tenantId) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    String roleCode = tenantManageService.getUserRole(spaceId, tenantId, userId);
    Assert.hasText(roleCode, "当前用户无权访问");
    Map<String, Object> result = new HashMap<>();
    // 组件
    List<String> components = CollectionUtils.emptyIfNull(rolePrivManageMapper.selectRolePrivList(roleCode, PrivConsts.PRIV_TYPE_COMPONENT)).stream()
      .map(SimpleRolePrivDTO::getPrivCode).toList();
    result.put(PrivConsts.PRIV_TYPE_COMPONENT, components);
    // 菜单
    List<SimplePortalMenuDTO> menus = new ArrayList<>();
    List<Long> privIds = rolePrivManageMapper.selectRolePrivList(roleCode, PrivConsts.PRIV_TYPE_MENU).stream().map(SimpleRolePrivDTO::getPrivId)
      .toList();
    if (CollectionUtils.isNotEmpty(privIds)) {
      menus.addAll(menuManageService.querySimpleMenuTree(privIds));
      // 调整菜单数据
      if (tenantId == null) {
        // 如果前端没有送 tenantId，根据空间 ID 获取第一个
        tenantId = tenantManageService.getFirstTenantId(spaceId, userId);
      }
      for (SimplePortalMenuDTO menu : menus) {
        wrapMenu(menu, tenantId, privIds);
      }
    }
    result.put(PrivConsts.PRIV_TYPE_MENU, menus);
    result.put("roleCode", roleCode);
    return result;
  }

  /**
   * 微调菜单数据
   * <p>1.移除空菜单</p>
   * <p>2.新窗口打开方式的链接，补全链接</p>
   * <p>3.高阶功能，按需控制显隐</p>
   */
  private void wrapMenu(SimplePortalMenuDTO menu, @Nullable Long tenantId, List<Long> privIds) {
    // 目录上挂菜单，权限过滤
    if (menu.getMenuId() != null && !privIds.contains(menu.getMenuId())) {
      menu.setMenuId(null);
      menu.setMenuUrl(null);
      menu.setIsHidden(true);
    }
    // 调整文档中心菜单链接
    setKnowledgeMenuUrl(menu, tenantId);
    // 调整菜单链接
    setMenuUrl(menu, tenantId);
    // 特殊菜单权限控制
    setIsHidden(menu);

    for (SimplePortalMenuDTO child : CollectionUtils.emptyIfNull(menu.getChildren())) {
      wrapMenu(child, tenantId, privIds);
    }

    // 隐藏空白目录
    if (menu.getMenuId() == null && (CollectionUtils.isEmpty(menu.getChildren()) || IterableUtils.matchesAll(menu.getChildren(),
      p -> BooleanUtils.isTrue(p.getIsHidden())))) {
      menu.setIsHidden(true);
    }
  }

  private void setIsHidden(SimplePortalMenuDTO menu) {
    // 高阶功能，按需控制显隐
    if (PrivConsts.ADVANCED_MENU_IDS.contains(menu.getMenuId())) {
      boolean isHidden = false;
      if (Objects.equals(menu.getMenuId(), PrivConsts.MENU_ID_FINETUNE)) {
        isHidden = BooleanUtils.isNotTrue(SystemParameter.MODEL_FINETUNE_ENABLED.getBooleanValueFromDb());
      }
      else if (Objects.equals(menu.getMenuId(), PrivConsts.MENU_ID_CORPUS)) {
        isHidden = BooleanUtils.isNotTrue(SystemParameter.CORPUS_ENABLED.getBooleanValueFromDb());
      }
      else if (Objects.equals(menu.getMenuId(), PrivConsts.MENU_ID_SUGGESTIONTERM)) {
        isHidden = BooleanUtils.isNotTrue(SystemParameter.SUGGESTION_TERM_ENABLE.getBooleanValueFromDb());
      }
      menu.setIsHidden(isHidden);
    }

    // 启用插件市场，调整相关菜单
    if (BooleanUtils.isTrue(SystemParameter.PLUGIN_ENABLED.getBooleanValueFromDb())) {
      if (Objects.equals(menu.getMenuId(), PrivConsts.MENU_ID_PLUGIN)) {
        // 屏蔽插件管理菜单
        menu.setIsHidden(true);
      }
      if (Objects.equals(menu.getMenuId(), PrivConsts.MENU_ID_PLUGIN_SQUARE)) {
        // 广场菜单，调整路由地址
        menu.setMenuUrl(PrivConsts.MENU_URL_PLUGIN_SQUARE);
      }
    }
  }

  private void setMenuUrl(SimplePortalMenuDTO menu, @Nullable Long tenantId) {
    if (PrivConsts.PORTAL_MENU_TYPE_OPEN.equals(menu.getMenuType()) && StringUtils.isNotEmpty(menu.getMenuUrl())) {
      // 新窗口打开方式的菜单，拼接动态参数
      String url = menu.getMenuUrl();
      String target = tenantId == null ? "" : tenantId.toString();
      url = url.replace("${tenantId}", target);
      menu.setMenuUrl(url);
    }
  }

  /**
   * 特殊处理文档中心菜单
   * <p>1. 默认使用旧菜单链接 /knowledge </p>
   * <p>2. 新增文档中心，目前由前端特殊处理 </p>
   * <p>3. 知识中台，访问地址维护在租户设置中 </p>
   * <p>4. 百应，屏蔽菜单 </p>
   */
  private void setKnowledgeMenuUrl(SimplePortalMenuDTO menu, @Nullable Long tenantId) {
    boolean skip = tenantId == null || menu.getMenuId() == null || !Objects.equals(PrivConsts.MENU_ID_KNOWLEDGE, menu.getMenuId());
    if (skip) {
      return;
    }
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(tenantId);
    if (KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM.equals(knowledgeInfo.getKnowledgeType())) {
      menu.setMenuUrl(knowledgeInfo.getWebUrl());
      menu.setMenuType(PrivConsts.PORTAL_MENU_TYPE_OPEN);
    }
    else if (KnowledgeConsts.KNOWLEDGE_TYPE_BEYOND.equals(knowledgeInfo.getKnowledgeType())) {
      menu.setIsHidden(true);
    }
  }
}
