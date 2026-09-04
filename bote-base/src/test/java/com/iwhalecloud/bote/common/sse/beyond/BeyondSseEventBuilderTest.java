package com.iwhalecloud.bote.common.sse.beyond;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * {@link BeyondSseEventBuilder} 单元测试。
 *
 * <p>验证各 setter 返回自身（链式）、字段被正确写入，以及 build() 抛出
 * {@link UnsupportedOperationException}（该构造器仅用于承载原始信息，不支持构建 SSE 报文）。</p>
 */
class BeyondSseEventBuilderTest {

  @Test
  void id_setsAndReturnsSelf() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    assertThat(builder.id("evt-1")).isSameAs(builder);
    assertThat(builder.getId()).isEqualTo("evt-1");
  }

  @Test
  void name_setsAndReturnsSelf() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    assertThat(builder.name("text")).isSameAs(builder);
    assertThat(builder.getEventName()).isEqualTo("text");
  }

  @Test
  void data_object_setsData() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    Object payload = new Object();
    assertThat(builder.data(payload)).isSameAs(builder);
    assertThat(builder.getData()).isSameAs(payload);
    assertThat(builder.getMediaType()).isNull();
  }

  @Test
  void data_withMediaType_setsDataAndMediaType() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    Object payload = new Object();
    assertThat(builder.data(payload, MediaType.APPLICATION_JSON)).isSameAs(builder);
    assertThat(builder.getData()).isSameAs(payload);
    assertThat(builder.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON);
  }

  @Test
  void reconnectTime_returnsSelfWithoutEffect() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    assertThat(builder.reconnectTime(3000L)).isSameAs(builder);
  }

  @Test
  void comment_returnsSelfWithoutEffect() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    assertThat(builder.comment("ping")).isSameAs(builder);
  }

  @Test
  void build_throwsUnsupportedOperationException() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    builder.name("text");
    builder.data("hi");
    assertThatThrownBy(builder::build).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void defaults_areNull() {
    BeyondSseEventBuilder builder = new BeyondSseEventBuilder();
    assertThat(builder.getId()).isNull();
    assertThat(builder.getEventName()).isNull();
    assertThat(builder.getData()).isNull();
    assertThat(builder.getMediaType()).isNull();
  }
}
