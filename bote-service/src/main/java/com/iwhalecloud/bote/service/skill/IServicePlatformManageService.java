package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：API 平台 服务
 *
 * @author auto
 * @since 2024-09-17
 */
public interface IServicePlatformManageService {

  /**
   * 保存 API 平台
   *
   * @param platform API 平台
   * @return 结果
   */
  ResultVO<ServicePlatformDTO> saveServicePlatform(ServicePlatformDTO platform);

  /**
   * 查询 API 平台列表
   *
   * @param queryParams 查询条件
   * @return API 平台列表
   */
  List<ServicePlatformDTO> queryServicePlatformList(SkillQueryParams queryParams);

  /**
   * 查询单个 API 平台
   *
   * @param tenantId 租户 ID
   * @param platformId API 平台主键
   * @return API 平台
   */
  @Nullable
  ServicePlatformDTO findServicePlatform(Long tenantId, Long platformId);

  /**
   * 删除 API 平台
   *
   * @param tenantId 租户 ID
   * @param platformId API 平台主键
   * @return 结果
   */
  ResultVO<Void> deleteServicePlatform(Long tenantId, Long platformId);

  /**
   * 查询 API 平台列表（分页）
   *
   * @param queryParams 查询条件
   * @return API 平台分页列表
   */
  PageInfo<ServicePlatformDTO> queryServicePlatformPage(SkillQueryParams queryParams);

  /**
   * 根据平台 ID 列表获取平台列表
   *
   * @param tenantId 租户 ID
   * @param platformIds 平台 ID 列表
   * @return 平台列表
   */
  List<ServicePlatformDTO> queryServicePlatformByIds(Long tenantId, List<Long> platformIds);
}
