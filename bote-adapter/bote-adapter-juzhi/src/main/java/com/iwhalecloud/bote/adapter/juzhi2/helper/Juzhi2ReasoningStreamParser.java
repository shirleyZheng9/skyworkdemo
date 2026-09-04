package com.iwhalecloud.bote.adapter.juzhi2.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 聚智知识问答流式内容解析器
 *
 * <p>聚智会将思考与正文均放在 {@code content_type=text} 中，且 header.status 可能均为 0，
 * 需根据 {@code <think>} / {@code </think>} 标签区分推理与正文。</p>
 */
public final class Juzhi2ReasoningStreamParser {
  /** 推理内容开始标签 */
  private static final String THINK_OPEN_TAG = "<think>";
  /** 推理内容结束标签 */
  private static final String THINK_CLOSE_TAG = "</think>";

  private State state = State.INITIAL;
  /** 是否已输出过推理片段 */
  private boolean sentReasoning;
  /** 是否已输出过正文片段 */
  private boolean sentContent;

  /**
   * 解析一条 WebSocket 文本片段，可能产生 0~N 个输出片段
   */
  public List<Chunk> parse(@Nullable String raw) {
    if (StringUtils.isEmpty(raw)) {
      return Collections.emptyList();
    }
    List<Chunk> chunks = new ArrayList<>(2);
    switch (state) {
      case INITIAL -> parseInitial(raw, chunks);
      case REASONING -> parseReasoning(raw, chunks);
      case CONTENT -> appendContent(raw, chunks);
      default -> {
        // unreachable
      }
    }
    return chunks;
  }

  private void parseInitial(String raw, List<Chunk> chunks) {
    int startIdx = raw.indexOf(THINK_OPEN_TAG);
    if (startIdx >= 0) {
      state = State.REASONING;
      String afterTag = raw.substring(startIdx + THINK_OPEN_TAG.length());
      if (StringUtils.isNotEmpty(afterTag)) {
        parseReasoning(afterTag, chunks);
      }
      return;
    }
    state = State.CONTENT;
    appendContent(raw, chunks);
  }

  private void parseReasoning(String raw, List<Chunk> chunks) {
    if (THINK_OPEN_TAG.equals(raw)) {
      return;
    }
    int endIdx = raw.indexOf(THINK_CLOSE_TAG);
    if (endIdx >= 0) {
      appendReasoning(raw.substring(0, endIdx), chunks);
      state = State.CONTENT;
      String afterTag = raw.substring(endIdx + THINK_CLOSE_TAG.length());
      appendContent(afterTag, chunks);
      return;
    }
    appendReasoning(raw, chunks);
  }

  private void appendReasoning(String raw, List<Chunk> chunks) {
    String reasoning = sentReasoning ? raw : StringUtils.stripStart(raw, null);
    if (StringUtils.isEmpty(reasoning)) {
      return;
    }
    chunks.add(new Chunk(true, reasoning));
    sentReasoning = true;
  }

  private void appendContent(String raw, List<Chunk> chunks) {
    if (THINK_OPEN_TAG.equals(raw) || THINK_CLOSE_TAG.equals(raw)) {
      return;
    }
    String text = raw.replace(THINK_OPEN_TAG, "").replace(THINK_CLOSE_TAG, "");
    if (!sentContent) {
      text = StringUtils.stripStart(text, null);
    }
    if (StringUtils.isEmpty(text)) {
      return;
    }
    chunks.add(new Chunk(false, text));
    sentContent = true;
  }

  /** 解析状态 */
  private enum State {
    /** 尚未遇到推理标签 */
    INITIAL,
    /** 推理内容进行中 */
    REASONING,
    /** 正文输出中 */
    CONTENT
  }

  /**
   * 流式片段
   *
   * @param reasoning 是否为推理内容
   * @param content 片段文本
   */
  public record Chunk(boolean reasoning, String content) {
  }
}
