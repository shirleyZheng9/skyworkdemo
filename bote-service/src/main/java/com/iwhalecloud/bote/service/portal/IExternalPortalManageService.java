package com.iwhalecloud.bote.service.portal;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.query.ExternalPortalQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 外部门户管理服务
 *
 * @author bianjp
 * @since 2025-02-24
 */
public interface IExternalPortalManageService {

  /**
   * 根据 ID 查询门户详情
   *
   * @param id 门户 ID
   * @return 门户详情。不存在时抛异常
   */
  ExternalPortalDTO getPortal(Long id);

  /**
   * 分页查询门户
   *
   * @param queryParams 查询条件
   * @return 门户分页数据
   */
  PageInfo<ExternalPortalDTO> queryPortalPage(ExternalPortalQueryParams queryParams);

  /**
   * 保存门户
   *
   * @param portal 门户
   * @return 门户 ID
   */
  ResultVO<Long> savePortal(ExternalPortalDTO portal);

  /**
   * 更新门户状态（启用、启用）
   *
   * @param id 门户状态
   * @param enabled 是否启用
   */
  ResultVO<Void> updatePortalStatus(Long id, boolean enabled);

  /**
   * 删除门户
   *
   * @param id 门户 ID
   */
  ResultVO<Void> deletePortal(Long id);

}
