package com.iwhalecloud.bote.service.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.query.PrivQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 权限定义管理服务
 *
 * @author auto
 * @since 2024-10-14
 */
public interface IPrivManageService {

  /**
   * 查询单个菜单组件
   *
   * @param privId 主键
   * @return 权限
   */
  PrivDTO getPriv(Long privId);

  /**
   * 保存菜单组件
   *
   * @param priv 菜单组件
   * @return 结果
   */
  ResultVO<PrivDTO> savePriv(PrivDTO priv);

  /**
   * 删除菜单组件
   *
   * @param privId 主键
   * @return 结果
   */
  ResultVO<Void> deletePriv(Long privId);

  /**
   * 查询菜单组件列表
   *
   * @param queryParams 查询条件
   * @return 菜单组件列表
   */
  List<PrivDTO> queryPrivList(PrivQueryParams queryParams);

  /**
   * 查询菜单组件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 菜单组件分页列表
   */
  PageInfo<PrivDTO> queryPrivPage(PrivQueryParams queryParams);
}
