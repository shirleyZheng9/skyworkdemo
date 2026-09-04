package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.QuerySingleRecordStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;

/**
 * 查询单条记录步骤转换器
 *
 * @author bianjp
 * @since 2025-11-25
 */
public class QuerySingleRecordStepConverter extends AbstractDatabaseStepConverter<QuerySingleRecordStep> {
  public QuerySingleRecordStepConverter() {
    super(QuerySingleRecordStep::new);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, QuerySingleRecordStep step, ConverterContext context) {
    super.convertStep(node, step, context);
    step.setWhere(parseJsonAttr(node, "where", new TypeReference<>() {
    }));
  }

}
