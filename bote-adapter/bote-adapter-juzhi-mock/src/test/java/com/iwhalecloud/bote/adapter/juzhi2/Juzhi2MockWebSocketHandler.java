package com.iwhalecloud.bote.adapter.juzhi2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 二级聚智 WebSocket 模拟接口处理器
 *
 * @author bianjp
 * @since 2025-05-17
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class Juzhi2MockWebSocketHandler extends TextWebSocketHandler {
  private static final Logger logger = LoggerFactory.getLogger(Juzhi2MockWebSocketHandler.class);
  /** 大模型接口响应 */
  private final ClassPathResource llmResponseResource = new ClassPathResource("mock-response/juzhi2/llm.txt");
  /** 知识召回响应 */
  private final ClassPathResource knowledgeRecallResponseResource = new ClassPathResource("mock-response/juzhi2/knowledgeRecall.txt");
  /** 知识问答响应 */
  private final ClassPathResource knowledgeChatResponseResource = new ClassPathResource("mock-response/juzhi2/knowledgeChat.txt");
  /** 文件解析响应 */
  private final ClassPathResource ocrResponseResource = new ClassPathResource("mock-response/juzhi2/ocrRecall.txt");
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    logger.debug("WebSocket connection established: url={}, headers={}", session.getUri(), session.getHandshakeHeaders());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    logger.debug("WebSocket connection closed: url={}, code={}, reason={}", session.getUri(), status.getCode(), status.getReason());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    logger.error("WebSocket transport error", exception);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String text = new String(message.asBytes(), StandardCharsets.UTF_8);
    logger.debug("Received text message: {}", text);

    try {
      JsonNode request = objectMapper.readTree(new String(message.asBytes(), StandardCharsets.UTF_8));
      JsonNode textNode = request.path("payload").path("text");
      // 知识问答请求
      if (textNode.isArray()) {
        sendMockResponse(session, knowledgeChatResponseResource);
        return;
      }

      JsonNode inputNode = request.path("payload").path("input");
      String startNodeId = IteratorUtils.first(inputNode.fieldNames());
      if (StringUtils.isEmpty(startNodeId)) {
        logger.warn("Invalid request message: {}", text);
      }
      else {
        JsonNode startNode = inputNode.path(startNodeId);
        if (startNode.has("file")) {
          sendMockResponse(session, ocrResponseResource);
          return;
        }
        String question = startNode.path("USER_INPUT").asText("");
        if (StringUtils.isEmpty(question)) {
          logger.warn("No question found in message: {}", text);
        }
        // 知识召回请求
        else if (startNode.has("dbNames")) {
          sendMockResponse(session, knowledgeRecallResponseResource);
        }
        // 大模型请求
        else {
          sendMockResponse(session, llmResponseResource);
        }
      }
      logger.debug("Sent response successfully");
    }
    catch (Exception e) {
      logger.warn("Failed to send mock response", e);
    }
    session.close();
  }

  /**
   * 发送模拟响应
   */
  private void sendMockResponse(WebSocketSession session, ClassPathResource responseResource) throws IOException, InterruptedException {
    List<String> lines;
    try (InputStream inputStream = responseResource.getInputStream()) {
      lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }
    long interval = lines.size() < 10 ? 200 : 50;
    Thread.sleep(2000);
    for (String line : lines) {
      session.sendMessage(new TextMessage(line));
      Thread.sleep(interval);
    }
  }
}
