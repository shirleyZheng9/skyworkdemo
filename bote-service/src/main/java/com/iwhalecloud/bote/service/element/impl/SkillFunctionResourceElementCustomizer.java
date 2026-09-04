package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.mapper.skill.SkillFunctionManageMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 服务函数
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_FUNCTION)
public class SkillFunctionResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillFunctionManageMapper functionManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_FUNCTION.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long funcId) {
    SkillFunctionDTO func = functionManageMapper.getSkillFunction(tenantId, funcId);
    return createCatalogElement(tenantId, funcId, func.getCatalogItemId());
  }
}
