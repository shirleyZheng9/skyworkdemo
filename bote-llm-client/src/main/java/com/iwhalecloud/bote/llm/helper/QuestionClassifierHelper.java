package com.iwhalecloud.bote.llm.helper;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.llm.client.dto.message.UserMessage;
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
import org.springframework.util.FileCopyUtils;

/**
 * 问题分类辅助器
 *
 * @author bianjp
 * @since 2024-10-21
 */
public final class QuestionClassifierHelper {
  /** 系统提示词模板 */
  private static final String systemPromptTemplate;

  private QuestionClassifierHelper() {
  }

  static {
    ClassPathResource resource = new ClassPathResource("prompt-templates/question-classifier.md");
    try (InputStream inputStream = resource.getInputStream()) {
      systemPromptTemplate = FileCopyUtils.copyToString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new BssException("读取问题分类提示词模板失败: " + e.getMessage(), e);
    }
  }

  /**
   * 构造调用大模型使用的消息列表
   *
   * @param text 输入文本
   * @param instruction 分类指令，可选
   * @param categories 分类列表
   * @param history 历史消息列表
   * @return 消息列表
   */
  public static List<Message> buildMessages(String text, @Nullable String instruction, List<String> categories, @Nullable List<Message> history) {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("text", text);
    params.put("categories", categories);
    if (StringUtils.isNotEmpty(instruction)) {
      params.put("instruction", instruction);
    }
    if (CollectionUtils.isNotEmpty(history)) {
      params.put("history", history);
    }

    // 组装消息列表
    List<Message> messages = new ArrayList<>();
    messages.add(new SystemMessage(systemPromptTemplate));
    messages.add(new UserMessage(JsonUtil.toJsonString(params)));
    return messages;
  }

}
