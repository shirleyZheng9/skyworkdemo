package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.InsertRecordStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;

/**
 * 添加记录步骤转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class InsertRecordStepConverter extends AbstractDatabaseStepConverter<InsertRecordStep> {
  public InsertRecordStepConverter() {
    super(InsertRecordStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, InsertRecordStep step, ConverterContext context) {
    super.convertStep(node, step, context);
    step.setParameters(parseRequiredJsonAttr(node, "parameters", new TypeReference<>() {
    }));
  }
}
