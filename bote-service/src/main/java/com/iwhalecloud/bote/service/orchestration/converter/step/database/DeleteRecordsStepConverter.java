package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.DeleteRecordsStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;

/**
 * 删除记录转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class DeleteRecordsStepConverter extends AbstractDatabaseStepConverter<DeleteRecordsStep> {
  public DeleteRecordsStepConverter() {
    super(DeleteRecordsStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, DeleteRecordsStep step, ConverterContext context) {
    super.convertStep(node, step, context);
    step.setWhere(parseRequiredJsonAttr(node, "where", new TypeReference<>() {
    }));
  }

}
