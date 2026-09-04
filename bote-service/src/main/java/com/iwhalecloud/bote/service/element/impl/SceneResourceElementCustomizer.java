package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.element.helper.EntityRelationParseUtil;
import com.iwhalecloud.bote.service.element.helper.StepElementConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 配置数据实体关系记录 - 智能体
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.SCENE)
public class SceneResourceElementCustomizer extends AbstractResourceElementCustomizer {

  private final IBotSceneManageService sceneManageService;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.SCENE.getCode();
  }

  /**
   * <p>1.目录 </p>
   * <p>2.标签 </p>
   * <p>3.模型 </p>
   * <p>4.技能 </p>
   */
  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long sceneId) {
    BotSceneDTO scene = sceneManageService.getScene(tenantId, sceneId, true);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, sceneId, scene.getCatalogItemId()));
    for (Long labelId : CollectionUtils.emptyIfNull(scene.getLabelIds())) {
      elements.add(createElement(tenantId, sceneId, labelId, DataSyncCodeEnum.LABEL.getCode()));
    }

    if (scene.getModelId() != null && !Objects.equals(scene.getModelId(), ModelConsts.DEFAULT_MODEL)) {
      elements.add(createElement(tenantId, sceneId, scene.getModelId(), DataSyncCodeEnum.MODEL.getCode()));
    }

    flowSkillElementHandler(tenantId, sceneId, scene, elements);
    return elements;
  }

  /**
   * 技能元素处理
   */
  private void flowSkillElementHandler(Long tenantId, Long sceneId, BotSceneDTO scene, List<ResourceElementDTO> elements) {
    if (SceneConsts.SCENE_TYPE_CHATFLOW.equals(scene.getSceneType()) && StringUtils.isNotEmpty(scene.getSceneDsl())) {
      // 负责智能体，根据节点步骤信息，计算关联的技能
      SceneDslDTO dsl = JsonUtil.parseJson(scene.getSceneDsl(), SceneDslDTO.class);
      if (dsl != null && CollectionUtils.isNotEmpty(dsl.getSteps())) {
        for (AbstractStep step : dsl.getSteps()) {
          parseElement(tenantId, sceneId, step, elements);
        }
      }
      // 解析节点参数，计算关联关系，如环境变量
      if (StringUtils.isNotEmpty(scene.getSceneGraphJson())) {
        SceneGraphDTO sceneGraph = JsonUtil.parseJson(scene.getSceneGraphJson(), SceneGraphDTO.class);
        List<ResourceElementDTO> elementList = new ArrayList<>();
        EntityRelationParseUtil.parseParameter(elementList, tenantId, sceneGraph);
        for (ResourceElementDTO dto : elementList) {
          elements.add(createElement(tenantId, sceneId, dto.getElementId(), dto.getElementType()));
        }
      }
    }
    else {
      // 简单智能体，知识问答，计算关联的技能
      for (BotSceneSkillDTO skill : CollectionUtils.emptyIfNull(scene.getFlatSkills())) {
        elements.add(createElement(tenantId, sceneId, skill.getSkillId(), StepElementConverter.getElementType(skill.getSkillType())));
      }
      if (scene.getPromptId() != null) {
        elements.add(createElement(tenantId, sceneId, scene.getPromptId(), DataSyncCodeEnum.PROMPT.getCode()));
      }
    }
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
