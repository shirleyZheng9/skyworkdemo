package com.iwhalecloud.bote.config;

import com.iwhalecloud.bote.common.interceptor.WebSocketSessionInterceptor;
import com.iwhalecloud.bote.controller.video.RealtimeAsrWebSocketHandler;
import com.iwhalecloud.bote.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 配置
 *
 * @author auto
 * @since 2026-01-08
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class BoteWebSocketConfig implements WebSocketConfigurer {
  private final RealtimeAsrWebSocketHandler realtimeAsrWebSocketHandler;
  private final ChatWebSocketHandler chatWebSocketHandler;
  private final WebSocketSessionInterceptor webSocketSessionInterceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // 实时语音识别 WebSocket 端点
    registry.addHandler(realtimeAsrWebSocketHandler, "/bote/ws/asr/realtime");
    // 对话接口
    registry.addHandler(chatWebSocketHandler, "/bote/ws/chat/completions").addInterceptors(webSocketSessionInterceptor);
  }

  @Bean
  public ServletServerContainerFactoryBean createWebSocketContainer() {
    ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
    // 设置文本消息缓冲区大小 (512KB)
    container.setMaxTextMessageBufferSize(512 * 1024);
    // 设置二进制消息缓冲区大小 (512KB)
    container.setMaxBinaryMessageBufferSize(512 * 1024);
    return container;
  }
}
