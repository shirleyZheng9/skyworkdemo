package com.iwhalecloud.bote.common.diffc.persist.impl.a2a;

import com.iwhalecloud.bote.dto.a2a.A2aPlatformDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aPlatformMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：A2A 平台
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Component
public final class A2aPlatformDifferencePersistence extends BaseRootPersistence<A2aPlatformDTO> {

  public A2aPlatformDifferencePersistence(A2aPlatformMapper platformMapper) {
    setAddConsumer(platformMapper::insertPlatform);
    setModifyConsumer(platformMapper::updatePlatform);
  }

}
