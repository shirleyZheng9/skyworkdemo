package com.iwhalecloud.bote.mapper.portal;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.query.PrivQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 权限定义管理
 *
 * @author auto
 * @since 2024-10-14
 */
public interface PrivManageMapper {
  /**
   * 校验菜单组件的编码唯一性
   *
   * @param priv 权限
   * @return 结果
   */
  boolean existsPrivCode(@Param("dto") PrivDTO priv);

  /**
   * 根据主键获取菜单组件
   *
   * @param privId 主键
   * @return 菜单组件
   */
  PrivDTO getPriv(@Param("id") Long privId);

  /**
   * 新增菜单组件
   *
   * @param priv 菜单组件
   * @return 结果
   */
  int insertPriv(@Param("dto") PrivDTO priv);

  /**
   * 修改菜单组件
   *
   * @param priv 菜单组件
   * @return 结果
   */
  int updatePriv(@Param("dto") PrivDTO priv);

  /**
   * 删除菜单组件
   *
   * @param privId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePriv(@Param("privId") Long privId, @Param("updatorId") Long updatorId);

  /**
   * 获取菜单组件列表
   *
   * @param queryParams 查询条件
   * @return 菜单组件列表
   */
  List<PrivDTO> selectPrivList(@Param("query") PrivQueryParams queryParams);

  /**
   * 获取菜单组件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 菜单组件分页列表
   */
  Page<PrivDTO> selectPrivPage(@Param("query") PrivQueryParams queryParams, RowBounds rowBounds);
}
