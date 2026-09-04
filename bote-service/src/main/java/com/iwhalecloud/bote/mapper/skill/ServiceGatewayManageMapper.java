package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceGatewayDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 技能：API 网关 Mapper
 *
 * @author auto
 * @since 2024-09-17
 */
public interface ServiceGatewayManageMapper {

  /**
   * 批量新增 API 网关
   *
   * @param gateways API 网关列表
   * @return 结果
   */
  int batchInsertServiceGateway(@Param("list") List<ServiceGatewayDTO> gateways);

  /**
   * 修改服务网关
   *
   * @param gateway 服务网关
   * @return 结果
   */
  int updateServiceGateway(@Param("dto") ServiceGatewayDTO gateway);

  /**
   * 根据平台 ID 和环境编码查找网关信息
   *
   * @param tenantId 租户 ID
   * @param platformId API 平台 ID
   * @param envCode 环境编码
   */
  SimpleServiceGatewayDTO selectSimpleGatewayByPlatformIdAndEnvCode(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId,
    @Param("envCode") String envCode);

  /**
   * 查找服务网关列表
   *
   * @param platformId API 平台 ID
   * @return 服务网关列表
   */
  List<ServiceGatewayDTO> selectServiceGatewayById(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId);

  /**
   * 查找服务网关列表
   *
   * @param platformIds API 平台 ID
   * @return 服务网关列表
   */
  List<ServiceGatewayDTO> selectServiceGatewayByIds(@Param("tenantId") Long tenantId, @Param("platformIds") List<Long> platformIds);

  /**
   * 根据环境编码查询网关信息
   *
   * @param tenantId 租户 ID 列表
   * @param envCode 环境编码
   * @return 网关信息
   */
  List<ServiceGatewayDTO> selectGatewayByEnvCode(@Param("tenantId") Long tenantId, @Param("envCode") String envCode);

  /**
   * 根据环境编码、链接查询网关信息
   */
  ServiceGatewayDTO selectGatewayByUrl(@Param("tenantId") Long tenantId, @Param("envCode") String envCode, @Param("url") String url);
}
