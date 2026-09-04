package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.mapper.skill.SkillAttrManageMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 静态数据
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_ATTR)
public class SkillAttrResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillAttrManageMapper attrManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_ATTR.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long attrId) {
    SkillAttrSpecDTO attr = attrManageMapper.getAttrSpec(tenantId, attrId);
    return createCatalogElement(tenantId, attrId, attr.getCatalogItemId());
  }
}
