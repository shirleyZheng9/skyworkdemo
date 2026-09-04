package com.iwhalecloud.bote.mapper.portal;

import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 租户设置信息管理
 *
 * @author auto
 * @since 2024-12-18
 */
public interface TenantQueryMapper {

  /**
   * 查询租户编码
   */
  String getTenantCode(@Param("tenantId") Long tenantId);

  /**
   * 查询简单租户
   */
  SimpleTenantDTO getSimpleTenant(@Param("tenantId") Long tenantId);

  /**
   * 查询空间的项目列表
   * @param spaceId
   * @return
   */
  List<TenantDTO> queryTenantList(@Param("spaceId") Long spaceId);

  /**
   * 根据租户 ID 查询归属的空间 ID
   */
  Long getSpaceId(@Param("tenantId") Long tenantId);

  /**
   * 新增租户
   *
   * @param tenant 租户
   * @return 结果
   */
  int insertTenant(@Param("dto") TenantDTO tenant);

  /**
   * 通过租户id列表查询租户列表
   */
  List<SimpleTenantDTO> getSimpleTenants(@Param("tenantIds") List<Long> tenantIds);

  /**
   * 根据外系统项目ID查询灵犀租户ID
   *
   * @param extTenantId 外系统项目ID
   * @param spaceId 空间ID
   * @return 灵犀租户ID，不存在返回 null
   */
  Long getTenantIdByExtTenantId(@Param("extTenantId") Long extTenantId, @Param("spaceId") Long spaceId);
}
