package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.UpdateRecordsStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;

/**
 * 更新记录步骤转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class UpdateRecordsStepConverter extends AbstractDatabaseStepConverter<UpdateRecordsStep> {
  public UpdateRecordsStepConverter() {
    super(UpdateRecordsStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, UpdateRecordsStep step, ConverterContext context) {
    super.convertStep(node, step, context);
    step.setParameters(parseRequiredJsonAttr(node, "parameters", new TypeReference<>() {
    }));
    step.setWhere(parseRequiredJsonAttr(node, "where", new TypeReference<>() {
    }));
  }
}
