package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.mapper.skill.SkillFlowManageMapper;
import com.iwhalecloud.bote.service.element.helper.EntityRelationParseUtil;
import com.iwhalecloud.bote.service.element.helper.StepElementConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 技能 - 工作流
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SKILL_FLOW)
public class SkillFlowResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final SkillFlowManageMapper flowManageMapper;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SKILL_FLOW.getCode();
  }

  /**
   * <p>1.目录 </p>
   * <p>2.关联的技能 </p>
   */
  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long flowId) {
    SkillFlowDTO flow = flowManageMapper.getSkillFlow(tenantId, flowId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, flowId, flow.getCatalogItemId()));
    if (StringUtils.isNotEmpty(flow.getFlowDsl())) {
      SceneDslDTO dsl = JsonUtil.parseJson(flow.getFlowDsl(), SceneDslDTO.class);
      if (dsl != null && CollectionUtils.isNotEmpty(dsl.getSteps())) {
        for (AbstractStep step : dsl.getSteps()) {
          parseElement(tenantId, flowId, step, elements);
        }
      }
    }
    // 解析节点参数，计算关联关系，如环境变量
    if (StringUtils.isNotEmpty(flow.getFlowGraphJson())) {
      SceneGraphDTO sceneGraph = JsonUtil.parseJson(flow.getFlowGraphJson(), SceneGraphDTO.class);
      List<ResourceElementDTO> elementList = new ArrayList<>();
      EntityRelationParseUtil.parseParameter(elementList, tenantId, sceneGraph);
      for (ResourceElementDTO dto : elementList) {
        elements.add(createElement(tenantId, flowId, dto.getElementId(), dto.getElementType()));
      }
    }
    return elements;
  }

  /**
   * 复杂场景，根据编排节点，匹配对应的元素类型、元素值
   */
  private void parseElement(Long tenantId, Long resourceId, AbstractStep step, List<ResourceElementDTO> elements) {
    List<ResourceElementDTO> list = new ArrayList<>();
    StepElementConverter.convert(step, list, tenantId);
    for (ResourceElementDTO dto : list) {
      elements.add(createElement(tenantId, resourceId, dto.getElementId(), dto.getElementType()));
    }
  }
}
