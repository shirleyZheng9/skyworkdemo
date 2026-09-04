package com.iwhalecloud.bote.adapter.dify.util;

import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import org.apache.commons.lang3.StringUtils;

/**
 * 推理内容处理工具类
 * <p> Dify推理有两种形式返回 </p>
 * <p> <think>推理内容</think>正文内容 </p>
 * <p> <details style=".."><summary> Thinking... </summary>推理内容</details>正文内容 </p>
 *
 * @author qian.sisheng
 * @since 2025-01-27
 */
public final class ReasoningContentUtil {
  /** 推理标签 */
  private static final String THINK_TAG = "<think>";
  /** 推理标签结束 */
  private static final String CLOSING_THINK_TAG = "</think>";
  /** 推理详情标签 */
  private static final String DETAILS_TAG = "<details";
  /** 推理详情标签结束 */
  private static final String CLOSING_DETAILS_TAG = "</details>";
  /** 推理详情标签结束 */
  private static final String CLOSING_SUMMARY_TAG = "</summary>";

  private ReasoningContentUtil() {

  }

  /**
   * 返回字符串中第一个出现的标签的下标；都不存在则返回 -1
   *
   * @param s 源字符串
   * @param t1 第一个标签
   * @param t2 第二个标签
   * @return 第一个出现的标签的下标，都不存在则返回 -1
   */
  public static int indexOfAny(String s, String t1, String t2) {
    int i1 = s.indexOf(t1);
    int i2 = s.indexOf(t2);
    if (i1 < 0) {
      return i2;
    }
    if (i2 < 0) {
      return i1;
    }
    return Math.min(i1, i2);
  }

  /**
   * 解析推理内容，将包含推理标签的内容分离为推理内容和普通内容
   *
   *
   * @param content 原始内容
   * @return 解析后的消息对象，如果没有推理内容则返回包含原始内容的消息
   */
  public static AssistantMessage parseReasoningContent(String content) {
    AssistantMessage message = new AssistantMessage();
    if (StringUtils.isEmpty(content)) {
      return message;
    }
    // 检查是否包含推理标签
    if (hasReasoningContent(content)) {
      int startPos = indexOfAny(content, CLOSING_SUMMARY_TAG, THINK_TAG);
      int endPos = indexOfAny(content, CLOSING_DETAILS_TAG, CLOSING_THINK_TAG);
      if (endPos > 0) {
        String tag = content.startsWith(THINK_TAG, startPos) ? THINK_TAG : CLOSING_SUMMARY_TAG;
        String reasoningContent = content.substring(startPos + tag.length(), endPos).trim();
        String remainingContent = content.substring(endPos + getClosingTagLength(content, endPos)).trim();
        message.setReasoningContent(reasoningContent);
        message.setContent(remainingContent);
        return message;
      }
    }
    // 没有推理内容，直接设置为普通内容
    message.setContent(content);
    return message;
  }

  /**
   * 移除推理内容，只保留普通内容
   *
   * @param content 原始内容
   * @return 移除推理内容后的普通内容
   */
  public static String removeReasoningContent(String content) {
    if (StringUtils.isEmpty(content)) {
      return content;
    }
    // 检查是否包含推理标签
    if (hasReasoningContent(content)) {
      int startPos = indexOfAny(content, CLOSING_SUMMARY_TAG, THINK_TAG);
      int endPos = indexOfAny(content, CLOSING_DETAILS_TAG, CLOSING_THINK_TAG);
      if (endPos > 0) {
        String closingTag = content.startsWith(THINK_TAG, startPos) ? CLOSING_THINK_TAG : CLOSING_DETAILS_TAG;
        return content.substring(endPos + closingTag.length()).trim();
      }
    }
    return content;
  }

  /**
   * 检查内容是否包含推理标签
   *
   * @param content 要检查的内容
   * @return 如果包含推理标签返回 true，否则返回 false
   */
  public static boolean hasReasoningContent(String content) {
    if (StringUtils.isEmpty(content)) {
      return false;
    }
    return content.startsWith(THINK_TAG) || content.startsWith(DETAILS_TAG);
  }

  /**
   * 获取结束标签的长度
   *
   * @param content 内容
   * @param endPos 结束位置
   * @return 结束标签的长度
   */
  private static int getClosingTagLength(String content, int endPos) {
    if (content.startsWith(THINK_TAG, endPos)) {
      return CLOSING_THINK_TAG.length();
    }
    else if (content.startsWith(DETAILS_TAG, endPos)) {
      return CLOSING_DETAILS_TAG.length();
    }
    return 0;
  }
}
