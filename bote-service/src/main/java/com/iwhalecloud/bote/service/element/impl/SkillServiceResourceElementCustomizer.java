package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.mapper.skill.SkillServiceManageMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 服务
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_SERVICE)
public class SkillServiceResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillServiceManageMapper serviceManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_SERVICE.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long serviceId) {
    SkillServiceDTO service = serviceManageMapper.getSkillService(tenantId, serviceId, BaseConsts.STATUS_CD_VALID);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, serviceId, service.getCatalogItemId()));
    elements.add(createElement(tenantId, serviceId, service.getPlatformId(), DataSyncCodeEnum.SERVICE_PLATFORM.getCode()));
    return elements;
  }
}
