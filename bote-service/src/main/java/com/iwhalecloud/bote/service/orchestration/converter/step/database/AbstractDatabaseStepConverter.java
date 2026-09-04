package com.iwhalecloud.bote.service.orchestration.converter.step.database;

import com.iwhalecloud.bote.dto.orchestration.context.ConverterContext;
import com.iwhalecloud.bote.dto.orchestration.step.database.AbstractDatabaseStep;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.service.orchestration.converter.AbstractStepConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Supplier;

/**
 * 数据库步骤转换器抽象类
 *
 * @author bianjp
 * @since 2025-11-25
 */
@SuppressFBWarnings("MS_PKGPROTECT")
public abstract class AbstractDatabaseStepConverter<T extends AbstractDatabaseStep> extends AbstractStepConverter<T> {

  protected AbstractDatabaseStepConverter(Supplier<T> stepFactory) {
    super(stepFactory);
  }

  @Override
  protected void convertStep(SceneGraphNodeDTO node, T step, ConverterContext context) {
    Long tableId = parseRequiredJsonAttr(node, "tableId", Long.class);
    step.setTableId(tableId);
  }
}
