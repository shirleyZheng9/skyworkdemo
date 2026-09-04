package com.iwhalecloud.bote.llm.client.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;

import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.consts.FunctionCallMode;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Function;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import com.iwhalecloud.bote.llm.client.dto.message.FunctionMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.ToolMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bote.llm.client.util.ModelHttpClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import java.util.function.Consumer;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * {@link OpenAiLlmClient} 单元测试
 *
 * <p>使用 mockStatic(ModelHttpClient.class) 模式 B stub post 4 参重载，覆盖预处理默认值、函数调用模式转换、
 * think 标签转换、异常分支等。</p>
 */
class OpenAiLlmClientTest {

  // ==================== 辅助方法 ====================

  private static LlmProperties toolModeProps() {
    return LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .temperature(0.5)
        .maxTokens(100)
        .functionCallMode(FunctionCallMode.TOOL)
        .build();
  }

  private static LlmProperties functionModeProps() {
    return LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .temperature(0.5)
        .maxTokens(100)
        .functionCallMode(FunctionCallMode.FUNCTION)
        .build();
  }

  private static LlmProperties noneModeProps() {
    return LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .functionCallMode(FunctionCallMode.NONE)
        .build();
  }

  private static ChatCompletionResponse stubResponse() {
    return ChatCompletionResponse.ofMessage(new AssistantMessage("hi"));
  }

  private static Tool weatherTool() {
    return new Tool("getWeather", "查询天气", JsonSchemaNode.newObject());
  }

  // ==================== chatCompletion 基本流程 ====================

  @Test
  void chatCompletion_success_returnsResponse() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);
    ChatCompletionResponse stub = stubResponse();

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stub);

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      ChatCompletionResponse response = client.chatCompletion(request, null);

      assertThat(response.getMessageContent()).isEqualTo("hi");
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)));
    }
  }

  @Test
  void chatCompletion_setsDefaultsFromProperties() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      assertThat(captured.getModel()).isEqualTo("gpt-4o");
      assertThat(captured.getTemperature()).isEqualTo(0.5);
      assertThat(captured.getMaxTokens()).isEqualTo(100);
      assertThat(captured.getStream()).isFalse();
    }
  }

  @Test
  void chatCompletion_setsParallelToolCallsFalse_whenToolsPresent() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addUserMessage("天气")
          .tools(List.of(weatherTool()))
          .build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      assertThat(captured.getParallelToolCalls()).isFalse();
    }
  }

  @Test
  void chatCompletion_emptyChoices_throws() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(new ChatCompletionResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      assertThatThrownBy(() -> client.chatCompletion(request, null))
          .isInstanceOf(BssException.class)
          .hasMessageContaining("choices 为空");
    }
  }

  @Test
  void chatCompletion_lengthFinishReason_throws() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    ChatCompletionResponse lengthResp = ChatCompletionResponse.ofMessage(new AssistantMessage("hi"));
    lengthResp.getChoices().get(0).setFinishReason("length");

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(lengthResp);

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      assertThatThrownBy(() -> client.chatCompletion(request, null))
          .isInstanceOf(BssException.class)
          .hasMessageContaining("长度超出限制");
    }
  }

  // ==================== preProcessResponse (think 标签转换) ====================

  @Test
  void preProcessResponse_thinkTagConverted() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    ChatCompletionResponse thinkResp = ChatCompletionResponse.ofMessage(
        new AssistantMessage("<think>思考</think>正文"));

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(thinkResp);

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      // convertReasoning defaults to true
      assertThat(request.isConvertReasoning()).isTrue();

      ChatCompletionResponse response = client.chatCompletion(request, null);
      AssistantMessage message = response.getMessage();
      assertThat(message.getReasoningContent()).isEqualTo("思考");
      assertThat(message.getContent()).isEqualTo("正文");
    }
  }

  @Test
  void preProcessResponse_noThinkTag_unchanged() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    ChatCompletionResponse normalResp = ChatCompletionResponse.ofMessage(new AssistantMessage("普通回复"));

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(normalResp);

      ChatCompletionRequest request = ChatCompletionRequest.builder().addUserMessage("你好").build();
      ChatCompletionResponse response = client.chatCompletion(request, null);
      AssistantMessage message = response.getMessage();
      assertThat(message.getReasoningContent()).isNull();
      assertThat(message.getContent()).isEqualTo("普通回复");
    }
  }

  // ==================== processFunctionCallRequest ====================

  @Test
  void processFunctionCallRequest_functionMode_convertsToolsToFunctions() {
    LlmProperties props = functionModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      // FUNCTION 模式陷阱：ToolMessage 的 toolCallId 必须等于函数名
      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addMessage(new AssistantMessage(new ToolCall(0, "call_1", "getWeather", "{}")))
          .addMessage(new ToolMessage("getWeather", "result"))
          .tools(List.of(weatherTool()))
          .toolChoice("auto")
          .build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      // tools 转为 functions
      assertThat(captured.getTools()).isNull();
      assertThat(captured.getFunctions()).isNotEmpty();
      assertThat(captured.getFunctionCall()).isEqualTo("auto");

      // AssistantMessage 的 toolCalls 转为 functionCall
      AssistantMessage assistantMsg = (AssistantMessage) captured.getMessages().get(0);
      assertThat(assistantMsg.getToolCalls()).isNull();
      assertThat(assistantMsg.getFunctionCall()).isNotNull();
      assertThat(assistantMsg.getFunctionCall().getName()).isEqualTo("getWeather");

      // ToolMessage 替换为 FunctionMessage
      Message secondMsg = captured.getMessages().get(1);
      assertThat(secondMsg).isInstanceOf(FunctionMessage.class);
      assertThat(((FunctionMessage) secondMsg).getName()).isEqualTo("getWeather");
      assertThat(((FunctionMessage) secondMsg).getContent()).isEqualTo("result");
    }
  }

  @Test
  void processFunctionCallRequest_functionMode_assistantToolCallToFunctionCall() {
    LlmProperties props = functionModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addMessage(new AssistantMessage(new ToolCall(0, "call_1", "getWeather", "{}")))
          .tools(List.of(weatherTool()))
          .build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      AssistantMessage assistantMsg = (AssistantMessage) captured.getMessages().get(0);
      assertThat(assistantMsg.getToolCalls()).isNull();
      assertThat(assistantMsg.getFunctionCall()).isNotNull();
      assertThat(assistantMsg.getFunctionCall().getName()).isEqualTo("getWeather");
    }
  }

  @Test
  void processFunctionCallRequest_noneMode_withTools_throws() {
    LlmProperties props = noneModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addUserMessage("天气")
          .tools(List.of(weatherTool()))
          .build();
      assertThatThrownBy(() -> client.chatCompletion(request, null))
          .isInstanceOf(BssException.class)
          .hasMessageContaining("不支持函数调用");
    }
  }

  @Test
  void processFunctionCallRequest_toolMode_keepsTools() {
    LlmProperties props = toolModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addUserMessage("天气")
          .tools(List.of(weatherTool()))
          .build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      assertThat(captured.getTools()).isNotEmpty();
      assertThat(captured.getFunctions()).isNull();
    }
  }

  // ==================== processFunctionParameters ====================

  @Test
  void processFunctionParameters_nullParameters_setToEmpty() {
    LlmProperties props = functionModeProps();
    OpenAiLlmClient client = new OpenAiLlmClient(props);

    // 用无参构造器创建 parameters=null 的 Function
    Function fn = new Function();
    fn.setName("testFn");
    fn.setDescription("desc");
    assertThat(fn.getParameters()).isNull();

    Tool tool = new Tool(fn);

    try (MockedStatic<ModelHttpClient> mocked = Mockito.mockStatic(ModelHttpClient.class)) {
      mocked.when(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), any(), nullable(Consumer.class)))
          .thenReturn(stubResponse());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .addUserMessage("你好")
          .tools(List.of(tool))
          .build();
      client.chatCompletion(request, null);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      mocked.verify(() -> ModelHttpClient.post(any(HttpUrl.class), nullable(Headers.class), captor.capture(), nullable(Consumer.class)));
      ChatCompletionRequest captured = (ChatCompletionRequest) captor.getValue();

      assertThat(captured.getFunctions()).isNotEmpty();
      assertThat(captured.getFunctions().get(0).getParameters()).isNotNull();
      assertThat(captured.getFunctions().get(0).getParameters()).isEqualTo(JsonSchemaNode.EMPTY);
    }
  }

  // ==================== 属性方法 ====================

  @Test
  void supportsFunctionCall_true_whenNotNone() {
    OpenAiLlmClient toolClient = new OpenAiLlmClient(toolModeProps());
    assertThat(toolClient.supportsFunctionCall()).isTrue();

    OpenAiLlmClient functionClient = new OpenAiLlmClient(functionModeProps());
    assertThat(functionClient.supportsFunctionCall()).isTrue();

    OpenAiLlmClient noneClient = new OpenAiLlmClient(noneModeProps());
    assertThat(noneClient.supportsFunctionCall()).isFalse();
  }

  @Test
  void supportsStreamingFunctionCall() {
    LlmProperties toolStreamingProps = LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .functionCallMode(FunctionCallMode.TOOL)
        .streamingFunctionCall(true)
        .build();
    OpenAiLlmClient toolClient = new OpenAiLlmClient(toolStreamingProps);
    assertThat(toolClient.supportsStreamingFunctionCall()).isTrue();

    LlmProperties noneStreamingProps = LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .functionCallMode(FunctionCallMode.NONE)
        .streamingFunctionCall(true)
        .build();
    OpenAiLlmClient noneClient = new OpenAiLlmClient(noneStreamingProps);
    assertThat(noneClient.supportsStreamingFunctionCall()).isFalse();
  }

  @Test
  void contextLength_defaultMinus1() {
    OpenAiLlmClient client = new OpenAiLlmClient(toolModeProps());
    assertThat(client.contextLength()).isEqualTo(-1);

    LlmProperties propsWithCtx = LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .contextLength(8000)
        .functionCallMode(FunctionCallMode.TOOL)
        .build();
    OpenAiLlmClient clientWithCtx = new OpenAiLlmClient(propsWithCtx);
    assertThat(clientWithCtx.contextLength()).isEqualTo(8000);
  }

  @Test
  void supportsVision() {
    LlmProperties propsWithVision = LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .supportsVision(true)
        .functionCallMode(FunctionCallMode.TOOL)
        .build();
    OpenAiLlmClient visionClient = new OpenAiLlmClient(propsWithVision);
    assertThat(visionClient.supportsVision()).isTrue();

    OpenAiLlmClient normalClient = new OpenAiLlmClient(toolModeProps());
    assertThat(normalClient.supportsVision()).isFalse();
  }

  @Test
  void defaultModel() {
    OpenAiLlmClient client = new OpenAiLlmClient(toolModeProps());
    assertThat(client.defaultModel()).isEqualTo("gpt-4o");
  }

  @Test
  void getModelConfigInfo() {
    OpenAiLlmClient client = new OpenAiLlmClient(toolModeProps());
    assertThat(client.getModelConfigInfo()).isNull();

    ModelConfigInfoDTO config = ModelConfigInfoDTO.builder().modelId(1L).modelName("test").build();
    LlmProperties propsWithConfig = LlmProperties.builder()
        .url("http://localhost/v1/chat/completions")
        .apiKey("sk-test")
        .model("gpt-4o")
        .modelConfig(config)
        .functionCallMode(FunctionCallMode.TOOL)
        .build();
    OpenAiLlmClient clientWithConfig = new OpenAiLlmClient(propsWithConfig);
    assertThat(clientWithConfig.getModelConfigInfo()).isEqualTo(config);
  }
}
