package com.iwhalecloud.bote.adapter.dify.client;

import com.iwhalecloud.bote.adapter.dify.config.DifyLlmProperties;
import com.iwhalecloud.bote.adapter.dify.dto.ChatFlowBlockingResponse;
import com.iwhalecloud.bote.adapter.dify.util.ReasoningContentUtil;
import com.iwhalecloud.bote.common.util.SseApiUtil;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.function.Consumer;
import okhttp3.sse.EventSource;
import org.springframework.lang.Nullable;

/**
 * dify大语言模型客户端
 *
 * @author qian.sisheng
 * @since 2025-10-16
 */
public class DifyLlmClient implements LlmClient {

  private final DifyLlmProperties properties;

  public DifyLlmClient(DifyLlmProperties properties) {
    this.properties = properties;
  }

  @Override
  @Nullable
  public ModelConfigInfoDTO getModelConfigInfo() {
    return properties.getModelConfig();
  }

  @Override
  public int contextLength() {
    return 0;
  }

  @Override
  public boolean supportsVision() {
    return false;
  }

  @Override
  public ChatCompletionResponse chatCompletion(ChatCompletionRequest request, @Nullable Consumer<Object> requestListener) {
    ChatFlowBlockingResponse chatFlowBlockingResponse = DifyApiHelper.llmChat(request, properties.getChatFlowSecret());
    // 转换思考内容
    if (request.isConvertReasoning() && chatFlowBlockingResponse != null) {
      String content = chatFlowBlockingResponse.getAnswer();
      if (ReasoningContentUtil.hasReasoningContent(content)) {
        AssistantMessage message = ReasoningContentUtil.parseReasoningContent(content);
        return ChatCompletionResponse.ofMessage(message);
      }
    }
    if (chatFlowBlockingResponse == null) {
      return ChatCompletionResponse.ofMessage(null);
    }
    ChatCompletionResponse response = ChatCompletionResponse.ofMessage(new AssistantMessage(chatFlowBlockingResponse.getAnswer()));
    response.setId(chatFlowBlockingResponse.getId());
    response.setUsage(chatFlowBlockingResponse.getMetadata().getUsage());
    response.setCreated(chatFlowBlockingResponse.getCreatedAt());
    return response;
  }

  @Override
  public EventSource chatCompletionStream(ChatCompletionRequest request, Consumer<ChatCompletionResponse> partialHandler,
                                          Consumer<BssException> completionHandler) {
    return SseApiUtil.wrapBlockingApi(request, partialHandler, completionHandler, this::chatCompletionStreamBlocking);
  }

  @Override
  public void chatCompletionStreamBlocking(ChatCompletionRequest request, Consumer<ChatCompletionResponse> eventHandler, @Nullable Consumer<Object> requestListener) {
    DifyApiHelper.llmChatStream(request, properties.getChatFlowSecret(), eventHandler, requestListener);
  }

  @Override
  public String defaultModel() {
    return "dify";
  }
}
