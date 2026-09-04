package com.iwhalecloud.bote.mapper.skill;

import com.iwhalecloud.bote.dto.skill.ServiceMockDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 模拟响应报文管理
 *
 * @author auto
 * @since 2024-12-17
 */
public interface ServiceMockManageMapper {

  /**
   * 新增模拟响应报文
   *
   * @param serviceMock 模拟响应报文
   * @return 结果
   */
  int insertServiceMock(@Param("dto") ServiceMockDTO serviceMock);

  /**
   * 批量新增模拟响应报文
   *
   * @param serviceMocks 模拟响应报文列表
   * @return 结果
   */
  int batchInsertServiceMock(@Param("list") List<ServiceMockDTO> serviceMocks);

  /**
   * 修改模拟响应报文
   *
   * @param serviceMock 模拟响应报文
   * @return 结果
   */
  int updateServiceMock(@Param("dto") ServiceMockDTO serviceMock);

  /**
   * 获取模拟响应报文列表
   *
   * @param tenantId 租户 ID
   * @param serviceId 服务ID
   * @return 模拟响应报文列表
   */
  List<ServiceMockDTO> selectServiceMockList(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId);

  /**
   * 查询模拟响应报文的简单信息
   */
  List<ServiceMockDTO> selectSimpleServiceMockList(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId);

  /**
   * 批量查询模拟响应报文的简单信息
   */
  List<ServiceMockDTO> selectSimpleServiceMockListByIds(@Param("tenantId") Long tenantId, @Param("serviceIds") List<Long> serviceIds);
}
