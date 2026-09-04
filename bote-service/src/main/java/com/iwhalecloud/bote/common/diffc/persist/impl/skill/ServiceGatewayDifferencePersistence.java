package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：API 网关
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class ServiceGatewayDifferencePersistence extends BaseRootPersistence<ServiceGatewayDTO> {
  public ServiceGatewayDifferencePersistence(ServiceGatewayManageMapper serviceGatewayManageMapper) {
    // 新增情况
    setBatchAddConsumer(serviceGatewayManageMapper::batchInsertServiceGateway);
    // 修改情况
    setModifyConsumer(serviceGatewayManageMapper::updateServiceGateway);
  }
}
