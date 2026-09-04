package com.iwhalecloud.bote.service.a2a;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aPlatformQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * A2A 平台管理服务
 *
 * @author bianjp
 * @since 2025-09-08
 */
public interface IA2aPlatformManageService {

  /**
   * 查询单个 A2A 平台
   *
   * @param tenantId 租户 ID
   * @param platformId A2A 平台 ID
   * @return A2A 平台
   */
  A2aPlatformDTO findA2aPlatform(Long tenantId, Long platformId);

  /**
   * 保存 A2A 平台
   *
   * @param platform A2A 平台
   */
  ResultVO<A2aPlatformDTO> saveA2aPlatform(A2aPlatformDTO platform);

  /**
   * 重置发布密钥
   *
   * @param tenantId 租户 ID
   * @param platformId A2A 平台 ID
   * @return 新的发布密钥
   */
  String resetPublishKey(Long tenantId, Long platformId);

  /**
   * 删除 A2A 平台
   *
   * @param tenantId 租户 ID
   * @param platformId A2A 平台 ID
   */
  ResultVO<Void> deleteA2aPlatform(Long tenantId, Long platformId);

  /**
   * 查询A2A 平台列表
   *
   * @param queryParams 查询条件
   * @return A2A 平台列表
   */
  List<A2aPlatformDTO> queryA2aPlatformList(A2aPlatformQueryParams queryParams);

  /**
   * 查询A2A 平台列表（分页）
   *
   * @param queryParams 查询条件
   * @return A2A 平台分页列表
   */
  PageInfo<A2aPlatformDTO> queryA2aPlatformPage(A2aPlatformQueryParams queryParams);

}
