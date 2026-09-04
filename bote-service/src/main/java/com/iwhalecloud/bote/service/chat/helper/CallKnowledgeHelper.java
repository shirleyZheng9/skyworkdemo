package com.iwhalecloud.bote.service.chat.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.service.model.helper.LargeModelAnswerHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 调用知识库辅助类
 *
 * @author Admin
 */
@Component
@RequiredArgsConstructor
public class CallKnowledgeHelper {
  private final LargeModelAnswerHelper modelAnswerHelper;

  /**
   * 根据回复内容生成几个相关问题
   *
   * @param tenantId 租户 ID
   * @param answer 回复内容
   * @return 相关问题列表
   */
  @SuppressWarnings("unchecked")
  public List<String> generateRelatedQuestions(Long tenantId, String answer) {
    if (StringUtils.isEmpty(answer)) {
      return Collections.emptyList();
    }
    // 部分大模型，对会话的 token 数有限制（小于 32 kb），知识召回的数据量可能很大，需要裁剪
    int maxTokens = SystemParameter.LARGE_MODEL_MAX_TOKENS.getRequiredIntegerValueFromDb();
    String prompt = SystemParameter.QUESTION_PROMPT.getValueFromDb();
    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, StringUtils.left(answer, maxTokens),
      new TypeReference<Map<String, Object>>() {
      });
    if (MapUtils.isEmpty(map) || !map.containsKey("content")) {
      return Collections.emptyList();
    }
    // 大模型输出的数据格式，不一定规范，这里需要格式化处理
    List<String> questions;
    Object object = map.get("content");
    if (object instanceof List) {
      questions = (List<String>) object;
    }
    else {
      if (object.toString().contains("?")) {
        questions = Arrays.asList(object.toString().split("\\?"));
      }
      else {
        questions = Arrays.asList(object.toString().split("？"));
      }
    }
    if (CollectionUtils.isEmpty(questions)) {
      return Collections.emptyList();
    }

    List<String> normalizedQuestions = new ArrayList<>(questions.size());
    for (String content : questions) {
      String text = content.replaceFirst(",", "");
      text = StringUtils.stripEnd(text, "？") + "？";
      normalizedQuestions.add(text);
    }
    return normalizedQuestions;
  }
}
