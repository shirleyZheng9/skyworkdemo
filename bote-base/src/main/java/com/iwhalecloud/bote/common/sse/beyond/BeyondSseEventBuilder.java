package com.iwhalecloud.bote.common.sse.beyond;

import java.util.Set;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

/**
 * 百应 SSE 事件构造器
 *
 * <p>用于能从构造器获取原始的信息（默认的 SseEventBuilderImpl 只能写入，不能读取）</p>
 *
 * @author bianjp
 * @since 2025-07-17
 */
@Getter
public class BeyondSseEventBuilder implements SseEventBuilder {
  /** 事件 ID */
  @Nullable
  private String id;
  /** 事件名称 */
  private String eventName;
  /** 数据 */
  private Object data;
  /** 数据的媒体类型 */
  private MediaType mediaType;

  @Override
  public SseEventBuilder id(String id) {
    this.id = id;
    return this;
  }

  @Override
  public SseEventBuilder name(String eventName) {
    this.eventName = eventName;
    return this;
  }

  @Override
  public SseEventBuilder reconnectTime(long reconnectTimeMillis) {
    return this;
  }

  @Override
  public SseEventBuilder comment(String comment) {
    return this;
  }

  @Override
  public SseEventBuilder data(Object object) {
    this.data = object;
    return this;
  }

  @Override
  public SseEventBuilder data(Object object, @Nullable MediaType mediaType) {
    this.data = object;
    this.mediaType = mediaType;
    return this;
  }

  @Override
  public Set<DataWithMediaType> build() {
    throw new UnsupportedOperationException();
  }
}
