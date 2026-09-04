package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.consts.ResponseFormatType;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 会话补全请求
 *
 * @author bianjp
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/create">Create chat completion</a>
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public final class ChatCompletionRequest {
  /** 模型 */
  private String model;
  /** 消息列表 */
  private List<Message> messages;
  /** 频率惩罚，取值范围为 [-2.0, 2.0]，默认为 0。正值会根据新 token 在前面的文本中出现的频率做惩罚，降低模型重复输出同一行的概率 */
  private Double frequencyPenalty;
  /** 设置 token 在补全中出现的概率 */
  private Map<String, Integer> logitBias;
  /** 允许生成的最大 token 数量 */
  private Integer maxTokens;
  /** 补全选项数量，默认为 1 */
  private Integer n;
  /** 存在惩罚，取值范围为 [-2.0, 2.0], 默认为 0。正值会根据新 token 在前面的文本中是否出现做惩罚，提高模型谈论新主题的概率 */
  private Double presencePenalty;
  /** 采样温度，取值范围为 [0, 2], 默认为 1。更高的值会让输出更加随机，更低的值会让输出更加聚焦和明确 */
  private Double temperature;
  /** 核采样方法概率阈值，取值范围为 (0, 1]，默认为 1。取值越大，生成的随机性越高；取值越低，生成的确定性越高 */
  private Double topP;
  /** 是否使用流式输出 */
  private Boolean stream;
  /** 停止标识。控制模型在生成的内容即将包含指定的字符串或 token_id 时自动停止 */
  private List<String> stop;
  /** 终端用户标识，辅助 OpenAI 监控和检测滥用 */
  private String user;
  /** 响应格式 */
  private ResponseFormat responseFormat;
  /** 随机数种子。用于控制模型生成内容的随机性，模型会尽量保证对相同 seed 和参数的重复请求返回相同的结果 */
  private Long seed;
  /** 是否开启并行工具调用(一次返回多个工具调用), OpenAI 默认为 true */
  private Boolean parallelToolCalls;
  /** 工具列表 */
  @JsonInclude(Include.NON_EMPTY) // 部分模型比如 Qwen/Qwen2.5-72B-Instruct-128K 传递空列表会报错
  private List<Tool> tools;
  /** 工具选择，控制模型是否应该调用工具，值为字符串或对象。值为对象时表示模型必须调用指定的工具 */
  private Object toolChoice;
  /** 函数列表。已废弃，请使用 tools */
  private List<Function> functions;
  /** 函数选择。已废弃，请使用 toolChoice */
  private Object functionCall;
  /** 扩展参数 */
  private Map<String, Object> extParams;

  // 扩展参数，后端自己使用，不发给大模型
  /** 是否转换思考内容（将 content 中 {@code <think>} 标签内的内容转为 reasoning_content） */
  @JsonIgnore
  @Builder.Default
  private boolean convertReasoning = true;
  /** 链路追踪标识，用于统计 token 用量 */
  @JsonIgnore
  private String traceId;
  /** 会话 ID，用于统计 token 用量 */
  @JsonIgnore
  private Long sessionId;
  /** 租户 ID，用于统计 token 用量 */
  @JsonIgnore
  private Long tenantId;
  /** 应用 ID，用于统计 token 用量 */
  @JsonIgnore
  private Long botId;
  /** 机器人 ID，用于统计 token 用量 */
  @JsonIgnore
  private Long sceneId;
  /** 开始时间，用于统计 token 用量 */
  @JsonIgnore
  private Date startTime;
  /** 使用来源 */
  @JsonIgnore
  private String sourceFrom;


  /**
   * 获取扩展参数
   *
   * <p>使用 @JsonAnyGetter 让扩展参数序列化到外层</p>
   */
  @JsonAnyGetter
  public Map<String, Object> getExtParams() {
    return extParams;
  }

  /**
   * 添加扩展参数
   *
   * <p>使用 @JsonAnySetter 让未知的参数反序列化到扩展参数</p>
   */
  @JsonAnySetter
  public void addExtParams(String key, Object value) {
    if (extParams == null) {
      extParams = new LinkedHashMap<>();
    }
    extParams.put(key, value);
  }

  /**
   * 添加默认的扩展参数（不覆盖已有参数）
   */
  public void addDefaultExtParams(Map<String, Object> defaultExtParams) {
    if (MapUtils.isEmpty(defaultExtParams)) {
      return;
    }
    if (MapUtils.isEmpty(extParams)) {
      extParams = new LinkedHashMap<>(defaultExtParams);
    }
    else {
      // 不覆盖已有参数
      Map<String, Object> result = new LinkedHashMap<>(defaultExtParams);
      result.putAll(extParams);
      extParams = result;
    }
  }

  /**
   * 会话补全请求构造器
   */
  public static final class ChatCompletionRequestBuilder {

    /**
     * 添加用户消息
     */
    public ChatCompletionRequestBuilder addUserMessage(String message) {
      // 忽略空消息
      if (StringUtils.isEmpty(message)) {
        return this;
      }
      return addMessage(new UserMessage(message));
    }

    /**
     * 添加系统消息
     */
    public ChatCompletionRequestBuilder addSystemMessage(String message) {
      // 忽略空消息
      if (StringUtils.isEmpty(message)) {
        return this;
      }
      return addMessage(new SystemMessage(message));
    }

    /**
     * 添加消息
     */
    public ChatCompletionRequestBuilder addMessage(Message message) {
      if (this.messages == null) {
        this.messages = new ArrayList<>();
      }
      this.messages.add(message);
      return this;
    }

    /**
     * 添加多条消息
     */
    public ChatCompletionRequestBuilder addMessages(@Nullable List<Message> messages) {
      if (messages == null || messages.isEmpty()) {
        return this;
      }
      if (this.messages == null) {
        this.messages = new ArrayList<>(messages);
      }
      else {
        this.messages.addAll(messages);
      }
      return this;
    }

    /**
     * 设置自定义模型配置
     */
    public ChatCompletionRequestBuilder customModelConfig(CustomModelConfig customModelConfig) {
      if (customModelConfig != null) {
        if (Boolean.TRUE.equals(customModelConfig.getTemperatureEnabled()) && customModelConfig.getTemperature() != null) {
          this.temperature = customModelConfig.getTemperature();
        }
        if (Boolean.TRUE.equals(customModelConfig.getMaxTokensEnabled()) && customModelConfig.getMaxTokens() != null) {
          this.maxTokens = customModelConfig.getMaxTokens();
        }
      }
      return this;
    }

    /**
     * 设置响应格式
     */
    public ChatCompletionRequestBuilder responseFormatType(ResponseFormatType responseFormatType) {
      this.responseFormat = new ResponseFormat(responseFormatType);
      return this;
    }
  }

}
