package com.iwhalecloud.bote.service.orchestration.helper;

/**
 * 工具使用缓冲
 *
 * <p>用于在大模型流式输出的过程中提取 tool_use 标签。</p>
 *
 * @author bianjp
 * @since 2025-06-01
 */
public class ToolUseBuffer {
  /** 非标签内容 */
  private final StringBuilder nonMatchingContent = new StringBuilder();
  /** 当前处理的内容 */
  private final StringBuilder buffer = new StringBuilder();
  /** tool_use 标签内容 */
  private final StringBuilder toolUseContent = new StringBuilder();
  /** 当前是否在 tool_use 标签内 */
  private boolean inToolUse = false;
  /** 待匹配的开始标签字符索引 */
  private int tagMatchIndex = 0;
  /** 待匹配的结束标签字符索引 */
  private int endTagMatchIndex = 0;

  /** 开始标签 */
  private static final String START_TAG = "<tool_use>";
  /** 结束标签 */
  private static final String END_TAG = "</tool_use>";

  /**
   * 重置状态，以便处理下一次大模型调用
   */
  public void reset() {
    nonMatchingContent.setLength(0);
    buffer.setLength(0);
    toolUseContent.setLength(0);
    inToolUse = false;
    tagMatchIndex = 0;
    endTagMatchIndex = 0;
  }

  /**
   * 处理流式输出的片段
   *
   * @param chunk 片段，字符数量不固定
   * @return 非 tool_use 标签内容
   */
  public String processChunk(String chunk) {
    // 每次的字符数量不固定，拆成字符方便统一处理
    for (char c : chunk.toCharArray()) {
      processChar(c);
    }
    if (!nonMatchingContent.isEmpty()) {
      String str = nonMatchingContent.toString();
      nonMatchingContent.setLength(0);
      return str;
    }
    return "";
  }

  /**
   * 处理单个字符
   */
  private void processChar(char c) {
    buffer.append(c);
    if (inToolUse) {
      processCharWithinTag(c);
      return;
    }
    // 检查是否匹配开始标签
    if (c == START_TAG.charAt(tagMatchIndex)) {
      tagMatchIndex++;
      if (tagMatchIndex == START_TAG.length()) {
        // 找到完整的开始标签
        inToolUse = true;
        tagMatchIndex = 0;

        // 打印开始标签之前的内容（不包括标签本身）
        String beforeTag = buffer.substring(0, buffer.length() - START_TAG.length());
        if (!beforeTag.isEmpty()) {
          nonMatchingContent.append(beforeTag);
        }

        // 清空buffer，开始收集tool_use内容
        buffer.setLength(0);
        toolUseContent.append(START_TAG);
      }
    }
    else {
      // 不匹配，重置匹配索引
      if (tagMatchIndex > 0) {
        tagMatchIndex = 0;
        // 重新检查当前字符是否是开始标签的第一个字符
        if (c == START_TAG.charAt(0)) {
          tagMatchIndex = 1;
        }
      }

      // 如果buffer中只有普通文本，实时打印
      if (buffer.length() >= START_TAG.length()) {
        char firstChar = buffer.charAt(0);
        nonMatchingContent.append(firstChar);
        buffer.deleteCharAt(0);
      }
    }
  }

  /**
   * 当前处于 tool_use 标签内时处理单个字符
   */
  private void processCharWithinTag(char c) {
    // 在tool_use标签内，收集内容
    toolUseContent.append(c);

    // 检查是否匹配结束标签
    if (c == END_TAG.charAt(endTagMatchIndex)) {
      endTagMatchIndex++;
      if (endTagMatchIndex == END_TAG.length()) {
        // 找到完整的结束标签，结束tool_use收集
        inToolUse = false;
        endTagMatchIndex = 0;
        buffer.setLength(0); // 清空buffer
      }
    }
    // 不匹配，重置匹配索引
    else if (endTagMatchIndex > 0) {
      endTagMatchIndex = 0;
      // 重新检查当前字符是否是结束标签的第一个字符
      if (c == END_TAG.charAt(0)) {
        endTagMatchIndex = 1;
      }
    }
  }

  /**
   * 获取末尾的非标签内容
   */
  public String getRemainingText() {
    if (!inToolUse && !buffer.isEmpty()) {
      return buffer.toString();
    }
    return "";
  }

  /**
   * 获取 tool_use 标签
   */
  public String getToolUseContent() {
    return toolUseContent.isEmpty() ? "" : toolUseContent.toString();
  }

}
