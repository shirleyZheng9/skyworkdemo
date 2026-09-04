package com.iwhalecloud.bote.doc.common.support.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * 7 个 Null 值默认序列化器的单元测试：{@link NullArraySerializer}、{@link NullObjectSerializer}、
 * {@link NullStringSerializer}、{@link NullNumberSerializer}、{@link NullBooleanSerializer}、
 * {@link NullCollectionSerializer}、{@link NullJsonObjectSerializer}。
 *
 * <p>这些序列化器均为单行行为变体（把 null/任意值写为指定默认 JSON token），同属一个"null 兜底"族，
 * 故合并为一个测试类。直接以 StringWriter 支撑的 JsonGenerator 调用 serialize；需 writeObject
 * 的分支（NullArray 非 null、NullJsonObject）在 generator 上设置 ObjectMapper codec。</p>
 */
class NullValueSerializersTest {

  @SuppressWarnings("rawtypes")
  private String serialize(JsonSerializer serializer, Object value, boolean withCodec) throws IOException {
    StringWriter sw = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(sw);
    if (withCodec) {
      gen.setCodec(new ObjectMapper());
    }
    serializer.serialize(value, gen, null);
    gen.close();
    return sw.toString();
  }

  // ==================== NullArraySerializer ====================

  @Test
  void nullArray_null_writesEmptyArray() throws IOException {
    assertThat(serialize(new NullArraySerializer(), null, false)).isEqualTo("[]");
  }

  @Test
  void nullArray_nonNull_writesObject() throws IOException {
    assertThat(serialize(new NullArraySerializer(), "x", true)).isEqualTo("\"x\"");
  }

  // ==================== NullObjectSerializer ====================

  @Test
  void nullObject_writesEmptyObject() throws IOException {
    assertThat(serialize(new NullObjectSerializer(), null, false)).isEqualTo("{}");
  }

  // ==================== NullStringSerializer ====================

  @Test
  void nullString_writesEmptyString() throws IOException {
    assertThat(serialize(new NullStringSerializer(), null, false)).isEqualTo("\"\"");
  }

  // ==================== NullNumberSerializer ====================

  @Test
  void nullNumber_writesZero() throws IOException {
    assertThat(serialize(new NullNumberSerializer(), 42, false)).isEqualTo("0");
  }

  // ==================== NullBooleanSerializer ====================

  @Test
  void nullBoolean_writesFalse() throws IOException {
    assertThat(serialize(new NullBooleanSerializer(), null, false)).isEqualTo("false");
  }

  // ==================== NullCollectionSerializer ====================

  @Test
  void nullCollection_writesEmptyArray() throws IOException {
    assertThat(serialize(new NullCollectionSerializer(), Collections.emptyList(), false)).isEqualTo("[]");
  }

  // ==================== NullJsonObjectSerializer ====================

  @Test
  void nullJsonObject_writesEmptyObject() throws IOException {
    assertThat(serialize(new NullJsonObjectSerializer(), null, true)).isEqualTo("{}");
  }
}
