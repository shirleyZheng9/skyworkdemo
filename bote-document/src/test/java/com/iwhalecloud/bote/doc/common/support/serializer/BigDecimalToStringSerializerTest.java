package com.iwhalecloud.bote.doc.common.support.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * {@link BigDecimalToStringSerializer} 单元测试。
 *
 * <p>覆盖 valueToString 去尾零转纯字符串（stripTrailingZeros.toPlainString）、isEmpty 恒 false、
 * 经 JsonGenerator 与 ObjectMapper 的序列化路径。</p>
 */
class BigDecimalToStringSerializerTest {

  @Test
  void valueToString_stripsTrailingZeros_andReturnsPlainString() {
    BigDecimalToStringSerializer serializer = new BigDecimalToStringSerializer();
    assertThat(serializer.valueToString(new BigDecimal("1.500"))).isEqualTo("1.5");
    assertThat(serializer.valueToString(new BigDecimal("100"))).isEqualTo("100");
    assertThat(serializer.valueToString(new BigDecimal("0.00"))).isEqualTo("0");
    assertThat(serializer.valueToString(new BigDecimal("1234500"))).isEqualTo("1234500");
    assertThat(serializer.valueToString(new BigDecimal("3.14"))).isEqualTo("3.14");
  }

  @Test
  void isEmpty_alwaysReturnsFalse() {
    BigDecimalToStringSerializer serializer = new BigDecimalToStringSerializer();
    assertThat(serializer.isEmpty(null, new BigDecimal("0"))).isFalse();
    assertThat(serializer.isEmpty(null, null)).isFalse();
  }

  @Test
  void serialize_writesStrippedPlainStringViaGenerator() throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator gen = new JsonFactory().createGenerator(sw)) {
      new BigDecimalToStringSerializer().serialize(new BigDecimal("1.500"), gen, null);
    }
    assertThat(sw.toString()).isEqualTo("\"1.5\"");
  }

  @Test
  void serialize_viaObjectMapper_rendersBigDecimalAsString() throws IOException {
    Holder holder = new Holder();
    holder.amount = new BigDecimal("12.3400");
    assertThat(new ObjectMapper().writeValueAsString(holder)).isEqualTo("{\"amount\":\"12.34\"}");
  }

  static class Holder {

    @JsonSerialize(using = BigDecimalToStringSerializer.class)
    BigDecimal amount;
  }
}
