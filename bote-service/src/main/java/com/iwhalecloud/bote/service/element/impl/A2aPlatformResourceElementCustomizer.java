package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.mapper.a2a.A2aPlatformMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - A2A 平台
 *
 * @author bianjp
 * @since 2025-10-29
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.A2A_PLATFORM)
public class A2aPlatformResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final A2aPlatformMapper a2aPlatformMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.A2A_PLATFORM.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long resourceId) {
    Long funcId = a2aPlatformMapper.selectAuthExtFuncIdByPlatformId(tenantId, resourceId);
    if (funcId != null) {
      return List.of(createElement(tenantId, resourceId, funcId, DataSyncCodeEnum.SKILL_FUNCTION.getCode()));
    }
    return List.of();
  }
}
