package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.PageStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 页面步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class PageStepConverter extends AbstractStepConverter<PageStep> {
  public PageStepConverter() {
    super(PageStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, PageStep step, ConverterContext context) {
    step.setPageId(parseRequiredJsonAttr(node, "pageId", "页面", Long.class));
    step.setMemorized(parseJsonAttr(node, "memorized", Boolean.class));
    if (Boolean.TRUE.equals(step.getMemorized())) {
      step.setCustomMemorized(parseJsonAttr(node, "customMemorized", Boolean.class));
      if (Boolean.TRUE.equals(step.getCustomMemorized())) {
        step.setMemoryContent(parseJsonAttr(node, "memoryContent", String.class));
      }
    }
    step.setParameters(getInputParams(node));
  }
}
