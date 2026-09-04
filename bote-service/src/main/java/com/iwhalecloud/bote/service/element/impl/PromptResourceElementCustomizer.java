package com.iwhalecloud.bote.service.element.impl;

import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.consts.ResourceElementConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.skill.PromptContentDTO;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.service.skill.IPromptManageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 配置数据实体关系记录 - 提示词
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
@RequiredArgsConstructor
@Component(ResourceElementConsts.PROMPT)
public class PromptResourceElementCustomizer extends AbstractResourceElementCustomizer {
  private final IPromptManageService promptManageService;

  @Override
  protected String getResourceType() {
    return DataSyncCodeEnum.PROMPT.getCode();
  }

  @Override
  protected List<ResourceElementDTO> compute(Long tenantId, Long promptId) {
    PromptDTO prompt = promptManageService.findPrompt(tenantId, promptId);
    Assert.notNull(prompt, () -> "提示词不存在: id=" + promptId);
    List<ResourceElementDTO> elements = new ArrayList<>(createCatalogElement(tenantId, promptId, prompt.getCatalogItemId()));
    for (PromptContentDTO dto : CollectionUtils.emptyIfNull(prompt.getPromptContents())) {
      if (!Objects.equals(dto.getModelId(), ModelConsts.DEFAULT_MODEL)) {
        elements.add(createElement(tenantId, promptId, dto.getModelId(), DataSyncCodeEnum.MODEL.getCode()));
      }
    }
    return elements;
  }
}
