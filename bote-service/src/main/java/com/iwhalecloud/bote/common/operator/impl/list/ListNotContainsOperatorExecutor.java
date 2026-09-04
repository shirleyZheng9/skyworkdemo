package com.iwhalecloud.bote.common.operator.impl.list;

import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.function.Function;

/**
 * 列表不包含操作符
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class ListNotContainsOperatorExecutor extends ListContainsOperatorExecutor {

  @Override
  public boolean execute(IfCondition condition, Function<String, Object> paramResolver) {
    return !super.execute(condition, paramResolver);
  }

}
