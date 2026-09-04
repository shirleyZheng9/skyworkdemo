package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.LoopStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * 循环步骤转换器
 *
 * @author bianjp
 * @since 2024-08-29
 */
public class LoopStepConverter extends AbstractStepConverter<LoopStep> {
  public LoopStepConverter() {
    super(LoopStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, LoopStep step, ConverterContext context) {
    String loopType = parseJsonAttr(node, "loopType", String.class);
    step.setLoopType(loopType);
    if (SceneConsts.LOOP_TYPE_RANGE.equals(loopType)) {
      step.setStart(parseRequiredJsonAttr(node, "start", "起始值", String.class));
      step.setEnd(parseRequiredJsonAttr(node, "end", "结束值", String.class));
      step.setStep(StringUtils.defaultIfEmpty(parseJsonAttr(node, "step", String.class), "1"));
    }
    else if (SceneConsts.LOOP_TYPE_OBJECT.equals(loopType)) {
      step.setObject(parseRequiredJsonAttr(node, "object", "循环对象", String.class));
    }
    else {
      step.setList(parseRequiredJsonAttr(node, "list", "循环列表", String.class));
    }
    step.setChildrenCodes(node.getChildrenCodes());
  }
}
