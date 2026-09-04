package com.iwhalecloud.bote.llm.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.llm.client.dto.ServerSentEvent;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;

/**
 * {@link ServerSentEventReader} 单元测试
 *
 * <p>与被测类同包，以访问包级类 ServerSentEventReader。覆盖 SSE 事件解析、多 data 行合并、异常处理与请求取消场景。</p>
 */
class ServerSentEventReaderTest {

  private static final HttpUrl URL = HttpUrl.parse("http://localhost/test");

  private ServerSentEventReader newReader(String sseText) {
    Buffer buffer = new Buffer().writeUtf8(sseText);
    return new ServerSentEventReader(URL, mock(Call.class), buffer);
  }

  @Test
  void read_singleDataEvent() {
    ServerSentEventReader reader = newReader("data: hello\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).data()).isEqualTo("hello");
    assertThat(events.get(0).id()).isNull();
    assertThat(events.get(0).event()).isNull();
  }

  @Test
  void read_multipleDataLines_joined() {
    ServerSentEventReader reader = newReader("data: line1\ndata: line2\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).data()).isEqualTo("line1\nline2");
  }

  @Test
  void read_eventAndId() {
    ServerSentEventReader reader = newReader("id: 7\nevent: add\ndata: {\"x\":1}\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).hasSize(1);
    assertThat(events.get(0).id()).isEqualTo("7");
    assertThat(events.get(0).event()).isEqualTo("add");
    assertThat(events.get(0).data()).isEqualTo("{\"x\":1}");
  }

  @Test
  void read_multipleEvents() {
    ServerSentEventReader reader = newReader("data: e1\n\ndata: e2\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).hasSize(2);
    assertThat(events.get(0).data()).isEqualTo("e1");
    assertThat(events.get(1).data()).isEqualTo("e2");
  }

  @Test
  void read_emptyDataLine_noEvent() {
    // 未知字段行触发 warn 但不影响；无 data 行则 buildEvent 返回 null
    ServerSentEventReader reader = newReader("comment line\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).isEmpty();
  }

  @Test
  void read_commentAndRetryAndUnknown_ignored_noEvent() {
    // :comment 与 retry: 行被忽略；foo: bar 走 warn 分支但不抛异常
    ServerSentEventReader reader = newReader(":comment\nretry: 5\nfoo: bar\n\n");
    List<ServerSentEvent> events = new ArrayList<>();
    reader.read(events::add);
    assertThat(events).isEmpty();
  }

  @Test
  void read_handlerThrowsBssException_propagates() {
    ServerSentEventReader reader = newReader("data: hello\n\n");
    BssException expected = new BssException("handler error");
    assertThatThrownBy(() -> reader.read(event -> {
      throw expected;
    })).isSameAs(expected);
  }

  @Test
  void read_handlerThrowsRuntime_wrapsBssException() {
    ServerSentEventReader reader = newReader("data: hello\n\n");
    assertThatThrownBy(() -> reader.read(event -> {
      throw new RuntimeException("runtime boom");
    })).isInstanceOf(BssException.class)
        .hasMessageStartingWith("处理 SSE 事件失败: ")
        .hasMessageContaining("runtime boom");
  }

  @Test
  void read_ioExceptionCanceled_throwsCanceledMsg() {
    Call call = mock(Call.class);
    when(call.isCanceled()).thenReturn(true);
    BufferedSource source = mock(BufferedSource.class, invocation -> {
      throw new IOException("Socket closed");
    });
    ServerSentEventReader reader = new ServerSentEventReader(URL, call, source);
    assertThatThrownBy(() -> reader.read(event -> {}))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("请求被取消");
  }

  @Test
  void read_ioExceptionOther_throwsReadFailed() {
    Call call = mock(Call.class);
    when(call.isCanceled()).thenReturn(false);
    BufferedSource source = mock(BufferedSource.class, invocation -> {
      throw new IOException("boom");
    });
    ServerSentEventReader reader = new ServerSentEventReader(URL, call, source);
    assertThatThrownBy(() -> reader.read(event -> {}))
        .isInstanceOf(BssException.class)
        .hasMessageContaining("读取 SSE 响应失败")
        .hasMessageContaining("boom");
  }
}
