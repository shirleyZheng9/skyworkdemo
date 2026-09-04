package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：API 平台 Mapper
 *
 * @author auto
 * @since 2024-09-17
 */
public interface ServicePlatformManageMapper {

  /**
   * 校验 API 平台的编码唯一性
   *
   * @param platform API 平台
   * @return 结果
   */
  boolean existsServicePlatformCode(@Param("dto") ServicePlatformDTO platform);

  /**
   * 新增 API 平台
   *
   * @param platform API 平台
   * @return 结果
   */
  int insertServicePlatform(@Param("dto") ServicePlatformDTO platform);

  /**
   * 更新服务平台
   *
   * @param platform 服务平台
   * @return 结果
   */
  int updateServicePlatform(@Param("dto") ServicePlatformDTO platform);

  /**
   * 根据主键获取 API 平台
   */
  ServicePlatformDTO getServicePlatform(@Param("tenantId") Long tenantId, @Param("id") Long platformId);

  /**
   * 获取 API 平台列表
   *
   * @param queryParams 查询条件
   * @return API 平台列表
   */
  List<ServicePlatformDTO> selectServicePlatformList(@Param("query") SkillQueryParams queryParams);

  /**
   * 删除 API 平台
   */
  int deleteServicePlatform(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId, @Param("updatorId") Long updatorId);

  /**
   * 获取 API 平台列表（分页）
   *
   * @param queryParams 查询条件
   * @return API 平台分页列表
   */
  Page<ServicePlatformDTO> selectServicePlatformPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据平台 ID 列表获取 API 平台
   *
   * @param platformIds 平台 ID 列表
   * @return API 平台列表
   */
  List<ServicePlatformDTO> selectServicePlatformByIds(@Param("tenantId") Long tenantId, @Param("platformIds") List<Long> platformIds);
}
