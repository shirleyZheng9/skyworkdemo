package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.mapper.skill.SkillPluginManageMapper;
import com.iwhalecloud.bote.service.element.helper.EntityRelationParseUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 插件
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_PLUGIN)
public class SkillPluginResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillPluginManageMapper pluginManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_PLUGIN.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long pluginId) {
    SkillPluginDTO plugin = pluginManageMapper.getSkillPlugin(tenantId, pluginId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, pluginId, plugin.getCatalogItemId()));
    // 处理模型元素
    List<ResourceElementDTO> list = new ArrayList<>();
    EntityRelationParseUtil.handleModelElement(list, plugin.getModelId(), tenantId);
    for (ResourceElementDTO dto : list) {
      elements.add(createElement(tenantId, pluginId, dto.getElementId(), dto.getElementType()));
    }
    if (plugin.getPromptId() != null) {
      elements.add(createElement(tenantId, pluginId, plugin.getPromptId(), DataSyncCodeEnum.PROMPT.getCode()));
    }
    return elements;
  }
}
