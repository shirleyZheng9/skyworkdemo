package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.PageFuncStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * 页面函数步骤转换器
 *
 * @author chen.linfa
 * @since 2024-08-06
 */
public class PageFuncStepConverter extends AbstractStepConverter<PageFuncStep> {
  public PageFuncStepConverter() {
    super(PageFuncStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, PageFuncStep step, ConverterContext context) {
    step.setPageFuncId(parseRequiredJsonAttr(node, "pageFuncId", "页面函数", Long.class));
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
