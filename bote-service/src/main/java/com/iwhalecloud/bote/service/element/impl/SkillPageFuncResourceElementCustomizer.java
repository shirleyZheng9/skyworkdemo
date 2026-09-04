package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPageFuncManageMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 页面函数
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_PAGE_FUNC)
public class SkillPageFuncResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillPageFuncManageMapper pageFuncManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_PAGE_FUNC.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long pageFuncId) {
    SkillPageFuncDTO func = pageFuncManageMapper.getSkillPageFunc(tenantId, pageFuncId);
    return createCatalogElement(tenantId, pageFuncId, func.getCatalogItemId());
  }
}
