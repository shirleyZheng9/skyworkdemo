package com.iwhalecloud.bote.common.operator.impl.string;

import com.iwhalecloud.bote.common.operator.impl.binary.AbstractBinaryOperatorExecutor;
import java.util.Objects;
import org.apache.commons.lang3.Strings;
import org.springframework.lang.Nullable;

/**
 * 字符串包含开头运算符
 *
 * @author bianjp
 * @since 2024-08-30
 */
public class StartsWithOperatorExecutor extends AbstractBinaryOperatorExecutor {
  @Override
  protected boolean executeBinary(@Nullable Object left, @Nullable Object right) {
    return Strings.CS.startsWith(Objects.toString(left, null), Objects.toString(right, null));
  }
}
