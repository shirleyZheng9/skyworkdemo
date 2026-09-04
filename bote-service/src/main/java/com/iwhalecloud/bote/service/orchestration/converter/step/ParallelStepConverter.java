package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.ParallelStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 并行步骤转换器
 *
 * @author bianjp
 * @since 2024-11-06
 */
public class ParallelStepConverter extends AbstractStepConverter<ParallelStep> {
  public ParallelStepConverter() {
    super(ParallelStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, ParallelStep step, ConverterContext context) {
    step.setBranches(context.getTargetEdges(node).stream().map(Pair::getRight).map(SceneGraphNodeDTO::getNodeCode).collect(Collectors.toList()));
  }
}
