package com.iwhalecloud.bote.doc.common.support.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.iwhalecloud.bote.doc.common.utils.ShortTimeFormatter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link ShortTimeDateSerializer} 单元测试。
 *
 * <p>null 值写 null token；非 null 值经 {@link ShortTimeFormatter#formatDate} 格式化后写字符串。
 * formatDate 静态调用用 mockStatic 固定返回，避开相对时间边界抖动。</p>
 */
class ShortTimeDateSerializerTest {

  @Test
  void serialize_null_writesNullToken() throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator gen = new JsonFactory().createGenerator(sw)) {
      new ShortTimeDateSerializer().serialize(null, gen, null);
    }
    assertThat(sw.toString()).isEqualTo("null");
  }

  @Test
  void serialize_nonNull_writesFormattedString() throws IOException {
    StringWriter sw = new StringWriter();
    try (JsonGenerator gen = new JsonFactory().createGenerator(sw);
         MockedStatic<ShortTimeFormatter> mocked = mockStatic(ShortTimeFormatter.class)) {
      mocked.when(() -> ShortTimeFormatter.formatDate(any(Date.class))).thenReturn("刚刚");
      new ShortTimeDateSerializer().serialize(new Date(0L), gen, null);
    }
    assertThat(sw.toString()).isEqualTo("\"刚刚\"");
  }
}
