package com.iwhalecloud.bote.common.operator;

import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import java.util.function.Function;

/**
 * 运算符执行器
 *
 * <p>目前只支持布尔表达式，用于条件判断</p>
 *
 * @author bianjp
 * @since 2024-08-30
 */
public interface OperatorExecutor {

  /**
   * 执行条件
   *
   * @param condition 条件
   * @param paramResolver 参数解析器
   * @return 执行结果
   */
  boolean execute(IfCondition condition, Function<String, Object> paramResolver);

}
