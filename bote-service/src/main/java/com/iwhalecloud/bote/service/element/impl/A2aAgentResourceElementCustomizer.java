package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.A2aConsts;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aAgentMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - A2A 服务
 *
 * @author bianjp
 * @since 2025-10-29
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.A2A_AGENT)
public class A2aAgentResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final A2aAgentMapper agentMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.A2A_AGENT.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long resourceId) {
    Long platformId = agentMapper.selectPlatformIdByAgentId(tenantId, resourceId);
    if (platformId != null && !A2aConsts.DEFAULT_A2A_PLATFORM_ID.equals(platformId)) {
      return List.of(createElement(tenantId, resourceId, platformId, DataSyncCodeEnum.A2A_PLATFORM.getCode()));
    }
    return List.of();
  }
}
