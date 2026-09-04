package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.QueryRecordsStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;

/**
 * 查询多条记录步骤转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class QueryRecordsStepConverter extends AbstractDatabaseStepConverter<QueryRecordsStep> {
  public QueryRecordsStepConverter() {
    super(QueryRecordsStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, QueryRecordsStep step, ConverterContext context) {
    super.convertStep(node, step, context);
    step.setWhere(parseJsonAttr(node, "where", new TypeReference<>() {
    }));
    step.setLimit(parseJsonAttr(node, "limit", Integer.class));
  }

}
