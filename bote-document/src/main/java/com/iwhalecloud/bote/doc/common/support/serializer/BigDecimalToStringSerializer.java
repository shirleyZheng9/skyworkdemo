package com.iwhalecloud.bote.doc.common.support.serializer;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import java.math.BigDecimal;

/**
 * 将 BigDecimal 转为字符串的序列化实现
 *
 * @author Aiqing
 * @since 2025/8/22
 */
public class BigDecimalToStringSerializer extends ToStringSerializerBase {
  private static final long serialVersionUID = -1L;

  public BigDecimalToStringSerializer() {
    super(BigDecimal.class);
  }

  @Override
  public boolean isEmpty(SerializerProvider prov, Object value) {
    return false;
  }

  @Override
  public final String valueToString(Object value) {
    // 浮点数转字符串，移除多余的小数位 0
    return ((BigDecimal) value).stripTrailingZeros().toPlainString();
  }
}
