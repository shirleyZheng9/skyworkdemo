package com.iwhalecloud.bote.mapper.portal;

import com.iwhalecloud.bote.dto.portal.PortalDirDTO;
import com.iwhalecloud.bote.dto.portal.PortalDirMenuDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 门户菜单管理
 *
 * @author chen.linfa
 * @since 2025-09-28
 */
public interface MenuManageMapper {

  /**
   * 根据主键获取门户目录
   *
   * @param dirId 目录 ID
   * @return 门户目录
   */
  PortalDirDTO getPortalDir(@Param("id") Long dirId);

  /**
   * 新增门户目录
   *
   * @param dir 门户目录
   * @return 结果
   */
  int insertPortalDir(@Param("dto") PortalDirDTO dir);

  /**
   * 修改门户目录
   *
   * @param dir 门户目录
   * @return 结果
   */
  int updatePortalDir(@Param("dto") PortalDirDTO dir);

  /**
   * 删除门户目录
   *
   * @param dirId 目录 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePortalDir(@Param("dirId") Long dirId, @Param("updatorId") Long updatorId);

  /**
   * 根据主键获取目录菜单
   *
   * @param relId 主键 ID
   * @return 目录菜单
   */
  PortalDirMenuDTO getDirMenu(@Param("id") Long relId);

  /**
   * 新增目录菜单
   *
   * @param menu 目录菜单
   * @return 结果
   */
  int insertDirMenu(@Param("dto") PortalDirMenuDTO menu);

  /**
   * 批量新增目录菜单
   *
   * @param menus 目录菜单列表
   * @return 结果
   */
  int batchInsertDirMenu(@Param("list") List<PortalDirMenuDTO> menus);

  /**
   * 修改目录菜单
   *
   * @param menu 目录菜单
   * @return 结果
   */
  int updateDirMenu(@Param("dto") PortalDirMenuDTO menu);

  /**
   * 删除目录菜单
   *
   * @param relId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteDirMenu(@Param("relId") Long relId, @Param("updatorId") Long updatorId);

  /**
   * 查询所有门户目录
   */
  List<PortalDirDTO> selectDirList();

  /**
   * 查询所有门户目录关联的菜单
   */
  List<PortalDirMenuDTO> selectDirMenuList(@Param("menuIds") List<Long> menuIds);

  /**
   * 查询目录关联的菜单
   */
  List<PortalDirMenuDTO> selectDirMenuByDirId(@Param("dirId") Long dirId);
}
