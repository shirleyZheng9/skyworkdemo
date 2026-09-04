package com.iwhalecloud.bote.service.portal;

import com.iwhalecloud.bote.dto.portal.PortalDirDTO;
import com.iwhalecloud.bote.dto.portal.PortalDirMenuDTO;
import com.iwhalecloud.bote.dto.portal.SimplePortalMenuDTO;
import com.iwhalecloud.bote.dto.portal.query.PortalDirParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 门户菜单管理服务
 *
 * @author chen.linfa
 * @since 2025-10-09
 */
public interface IMenuManageService {

  /**
   * 查询单个门户目录
   *
   * @param dirId 目录 ID
   * @return 门户目录
   */
  PortalDirDTO getPortalDir(Long dirId);

  /**
   * 保存门户目录
   *
   * @param dir 门户目录
   * @return 结果
   */
  ResultVO<PortalDirDTO> savePortalDir(PortalDirDTO dir);

  /**
   * 删除门户目录
   *
   * @param dirId 目录 ID
   * @return 结果
   */
  ResultVO<Void> deletePortalDir(Long dirId);

  /**
   * 查询单个目录菜单
   *
   * @param relId 主键 ID
   * @return 目录菜单
   */
  PortalDirMenuDTO getDirMenu(Long relId);

  /**
   * 保存目录菜单
   *
   * @param menu 目录菜单
   * @return 结果
   */
  ResultVO<PortalDirMenuDTO> saveDirMenu(PortalDirMenuDTO menu);

  /**
   * 批量保存目录下的菜单
   *
   * @param params 目录菜单
   * @return 结果
   */
  ResultVO<Void> batchSaveDirMenu(PortalDirParams params);

  /**
   * 删除目录菜单
   *
   * @param relId 主键 ID
   * @return 结果
   */
  ResultVO<Void> deleteDirMenu(Long relId);

  /**
   * 根据门户目录、关联菜单，构造目录树
   * <p>区分配置态、运行态</p>
   *
   * @param privIds 指定的权限 ID
   * @return 门户目录树
   */
  List<SimplePortalMenuDTO> querySimpleMenuTree(List<Long> privIds);
}
