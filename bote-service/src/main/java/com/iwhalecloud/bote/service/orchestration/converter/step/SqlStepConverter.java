package com.iwhalecloud.bote.service.orchestration.converter.step;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.SqlStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;

/**
 * SQL 步骤转换器
 *
 * @author bianjp
 * @since 2024-08-16
 */
public class SqlStepConverter extends AbstractStepConverter<SqlStep> {
  public SqlStepConverter() {
    super(SqlStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, SqlStep step, ConverterContext context) {
    step.setSqlId(parseRequiredJsonAttr(node, "sqlId", "SQL 服务", Long.class));
    step.setParameters(getInputParams(node));
    step.setResponse(getOutputParams(node));
  }

}
