package com.iwhalecloud.bote.service.portal.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.PrivConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.PortalDirDTO;
import com.iwhalecloud.bote.dto.portal.PortalDirMenuDTO;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.SimplePortalMenuDTO;
import com.iwhalecloud.bote.dto.portal.query.PortalDirParams;
import com.iwhalecloud.bote.dto.portal.query.PrivQueryParams;
import com.iwhalecloud.bote.mapper.portal.MenuManageMapper;
import com.iwhalecloud.bote.mapper.portal.PrivManageMapper;
import com.iwhalecloud.bote.service.portal.IMenuManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 门户菜单管理服务实现
 *
 * @author chen.linfa
 * @since 2025-10-09
 */
@Service
@RequiredArgsConstructor
public class MenuManageServiceImpl implements IMenuManageService {

  private final PrivManageMapper privManageMapper;

  private final MenuManageMapper menuManageMapper;

  @Override
  public PortalDirDTO getPortalDir(Long dirId) {
    return menuManageMapper.getPortalDir(dirId);
  }

  @Override
  @Transactional
  public ResultVO<PortalDirDTO> savePortalDir(PortalDirDTO dir) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    dir.setUpdatorId(userId);
    if (dir.getDirId() == null) {
      dir.setDirId(IDUtils.nextId());
      dir.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dir.setCreatorId(userId);
      if (dir.getSortby() == null) {
        dir.setSortby(1);
      }
      menuManageMapper.insertPortalDir(dir);
    }
    else {
      menuManageMapper.updatePortalDir(dir);
    }
    return ResultVO.success(dir);
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePortalDir(Long dirId) {
    menuManageMapper.deletePortalDir(dirId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PortalDirMenuDTO getDirMenu(Long relId) {
    return menuManageMapper.getDirMenu(relId);
  }

  @Override
  @Transactional
  public ResultVO<PortalDirMenuDTO> saveDirMenu(PortalDirMenuDTO menu) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    menu.setUpdatorId(userId);
    if (menu.getSortby() == null) {
      menu.setSortby(1);
    }
    if (menu.getRelId() == null) {
      menu.setRelId(IDUtils.nextId());
      menu.setStatusCd(BaseConsts.STATUS_CD_VALID);
      menu.setCreatorId(userId);
      menuManageMapper.insertDirMenu(menu);
    }
    else {
      menuManageMapper.updateDirMenu(menu);
    }
    return ResultVO.success(menu);
  }

  @Override
  @Transactional
  public ResultVO<Void> batchSaveDirMenu(PortalDirParams params) {
    List<PortalDirMenuDTO> menus = new ArrayList<>();
    PrivQueryParams queryParams = new PrivQueryParams();
    queryParams.setPrivIds(params.getMenuIds());
    List<PrivDTO> privs = privManageMapper.selectPrivList(queryParams);
    if (CollectionUtils.isEmpty(privs)) {
      return ResultVO.success();
    }
    List<PortalDirMenuDTO> exists = menuManageMapper.selectDirMenuByDirId(params.getDirId());
    int sortby = CollectionUtils.isEmpty(exists) ? 1 : exists.getLast().getSortby() + 1;
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (PrivDTO priv : privs) {
      if (IterableUtils.matchesAny(CollectionUtils.emptyIfNull(exists), p -> Objects.equals(p.getMenuId(), priv.getPrivId()))) {
        continue;
      }
      PortalDirMenuDTO menu = new PortalDirMenuDTO();
      menu.setRelId(IDUtils.nextId());
      menu.setDirId(params.getDirId());
      menu.setMenuId(priv.getPrivId());
      menu.setMenuName(priv.getPrivName());
      menu.setMenuType(PrivConsts.PORTAL_MENU_TYPE_PAGE);
      menu.setSortby(sortby++);
      menu.setStatusCd(BaseConsts.STATUS_CD_VALID);
      menu.setCreatorId(userId);
      menu.setUpdatorId(userId);
      menus.add(menu);
    }
    if (CollectionUtils.isNotEmpty(menus)) {
      menuManageMapper.batchInsertDirMenu(menus);
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteDirMenu(Long relId) {
    menuManageMapper.deleteDirMenu(relId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<SimplePortalMenuDTO> querySimpleMenuTree(List<Long> privIds) {
    // AI 门户定位调整，配置态也可使用
    List<PortalDirDTO> dirs = menuManageMapper.selectDirList();
    List<PortalDirMenuDTO> menus = menuManageMapper.selectDirMenuList(privIds);
    List<PortalDirDTO> roots = dirs.stream().filter(p -> Objects.equals(-1L, p.getParentId())).toList();
    List<SimplePortalMenuDTO> list = new ArrayList<>();
    for (PortalDirDTO root : roots) {
      SimplePortalMenuDTO dir = root.toProtalMenu();
      treeSimpleDir(dir, dirs, menus);
      list.add(dir);
    }
    list.sort(Comparator.comparing(SimplePortalMenuDTO::getSortby));
    return list;
  }

  private void treeSimpleDir(SimplePortalMenuDTO dir, List<PortalDirDTO> dirs, List<PortalDirMenuDTO> menus) {
    List<SimplePortalMenuDTO> children = new ArrayList<>();
    // 关联的菜单作为子目录
    children.addAll(menus.stream().filter(p -> Objects.equals(p.getDirId(), dir.getId())).map(PortalDirMenuDTO::toProtalMenu).toList());
    // 处理子目录
    children.addAll(dirs.stream().filter(p -> Objects.equals(dir.getId(), p.getParentId())).map(PortalDirDTO::toProtalMenu).toList());
    // 子目录排序
    children.sort(Comparator.comparing(p -> ObjectUtils.getIfNull(p.getSortby(), Integer.MAX_VALUE)));
    dir.setChildren(children);
    for (SimplePortalMenuDTO child : CollectionUtils.emptyIfNull(children)) {
      if (BooleanUtils.isNotTrue(child.getIsMenu())) {
        treeSimpleDir(child, dirs, menus);
      }
    }
  }
}
