package com.iwhalecloud.bote.llm.helper;

import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * 参数提取器辅助器
 *
 * @author bianjp
 * @since 2024-10-18
 */
public final class ParamExtractorHelper {
  /** 系统提示词模板 */
  private static final String systemPromptTemplate;

  private ParamExtractorHelper() {
  }

  static {
    ClassPathResource resource = new ClassPathResource("prompt-templates/param-extractor.md");
    try (InputStream inputStream = resource.getInputStream()) {
      systemPromptTemplate = FileCopyUtils.copyToString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new BssException("读取参数提取器提示词模板失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构造调用大模型使用的消息列表
   *
   * @param input 输入，支持复杂内容，不需要转为 JSON 字符串
   * @param instruction 指令，可选
   * @param params 要提取的参数结构，第一层必须是对象
   * @param history 历史消息，可选
   * @return 消息类表
   */
  public static List<Message> buildMessages(Object input, @Nullable String instruction, JsonSchemaNode params, @Nullable List<Message> history) {
    Assert.notNull(input, "文本不能为空");
    Assert.notNull(params, "参数不能为空");
    // 组装消息列表
    List<Message> messages = new ArrayList<>();
    messages.add(new SystemMessage(systemPromptTemplate));
    Map<String, Object> messageData = new LinkedHashMap<>();
    messageData.put("text", input);
    messageData.put("params", params);
    if (StringUtils.isNotEmpty(instruction)) {
      messageData.put("instruction", instruction);
    }
    if (CollectionUtils.isNotEmpty(history)) {
      messageData.put("history", history);
    }
    messages.add(new UserMessage(JsonUtil.toJsonString(messageData)));
    return messages;
  }
}
