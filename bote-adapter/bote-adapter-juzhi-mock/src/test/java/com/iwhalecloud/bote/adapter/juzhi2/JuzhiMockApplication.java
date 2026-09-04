package com.iwhalecloud.bote.adapter.juzhi2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 聚智接口模拟应用
 *
 * @author bianjp
 * @since 2025-05-17
 */
@SpringBootApplication
@EnableWebSocket
@SuppressWarnings("PMD.UseUtilityClass")
public class JuzhiMockApplication {
  public static void main(String[] args) {
    SpringApplication.run(JuzhiMockApplication.class, args);
  }

  /**
   * WebSocket 接口配置，用于模拟二级聚智接口
   */
  @Configuration
  static class MockWebSocketConfiguration implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
      registry.addHandler(new Juzhi2MockWebSocketHandler(), "/openapi/flames/api/v1/chat");
    }
  }
}
