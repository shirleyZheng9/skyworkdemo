package com.iwhalecloud.bote.common.diffc.persist.impl.a2a;

import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aAgentMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：A2A 服务
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Component
public final class A2aAgentDifferencePersistence extends BaseRootPersistence<A2aAgentDTO> {

  public A2aAgentDifferencePersistence(A2aAgentMapper agentMapper) {
    setAddConsumer(agentMapper::insertAgent);
    setModifyConsumer(agentMapper::updateAgent);
  }

}
