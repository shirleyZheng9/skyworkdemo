package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.skill.ServiceMockDTO;
import com.iwhalecloud.bote.mapper.skill.ServiceMockManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：模拟响应报文
 *
 * @author auto
 * @since 2024-12-17
 */
@Component
public final class ServiceMockDifferencePersistence extends BaseRootPersistence<ServiceMockDTO> {

  public ServiceMockDifferencePersistence(ServiceMockManageMapper serviceMockManageMapper) {
    setAddConsumer(serviceMockManageMapper::insertServiceMock);
    setBatchAddConsumer(serviceMockManageMapper::batchInsertServiceMock);
    setModifyConsumer(serviceMockManageMapper::updateServiceMock);
  }

}
