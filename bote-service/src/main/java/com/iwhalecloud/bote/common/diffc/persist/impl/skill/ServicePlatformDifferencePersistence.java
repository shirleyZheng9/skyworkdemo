package com.iwhalecloud.bote.common.diffc.persist.impl.skill;

import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.mapper.skill.ServicePlatformManageMapper;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：技能：API 平台
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public final class ServicePlatformDifferencePersistence extends BaseRootPersistence<ServicePlatformDTO> {
  public ServicePlatformDifferencePersistence(ServicePlatformManageMapper platformManageMapper) {
    // 新增情况
    setAddConsumer(platformManageMapper::insertServicePlatform);
    // 修改情况
    setModifyConsumer(platformManageMapper::updateServicePlatform);
  }
}
