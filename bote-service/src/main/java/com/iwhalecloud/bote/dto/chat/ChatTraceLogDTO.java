package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 会话日志
 *
 * @author bianjp
 * @since 2025-01-08
 */
@Getter
@Setter
@ToString
public class ChatTraceLogDTO {
  /** 日志 ID */
  private Long logId;
  /** 用户消息 ID */
  private Long msgId;
  /** 步骤名称 */
  private String stepName;
  /** 入参 */
  private String input;
  /** 出参 */
  private String outputJson;
  /** 日志内容 */
  private String logContent;
  /** 开始时间 */
  private Date startTime;
  /** 耗时(ms) */
  private Integer timeSpent;
  /** 状态 */
  private String logStatus;
  /** 序号 */
  private Integer sort;

  /**
   * 获取构造器实例
   *
   * @param stepName 步骤名称
   * @return 构造器实例
   */
  public static ChatTraceLogBuilder builder(String stepName) {
    return new ChatTraceLogBuilder(stepName);
  }

  /**
   * 会话日志构造器
   */
  @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
  public static class ChatTraceLogBuilder {
    /** 步骤名称 */
    private final String stepName;
    /** 入参 */
    private String input;
    /** 出参 */
    private String output;
    /** 日志内容 */
    private final StringBuilder logContent = new StringBuilder();
    /** 开始时间 */
    private final Date startTime;
    /** 耗时(ms) */
    private int timeSpent;
    /** 是否失败 */
    private boolean failed = false;

    protected ChatTraceLogBuilder(String stepName) {
      this.stepName = stepName;
      this.startTime = new Date();
    }

    /**
     * 设置入参
     */
    public ChatTraceLogBuilder input(@Nullable Object input) {
      if (input != null) {
        this.input = input instanceof String ? (String) input : JsonUtil.toJsonStringCompact(input);
      }
      return this;
    }

    /**
     * 设置出参
     */
    public ChatTraceLogBuilder output(@Nullable Object output) {
      if (output != null) {
        this.output = output instanceof String ? (String) output : JsonUtil.toJsonStringCompact(output);
      }
      return this;
    }

    /**
     * 添加一行日志
     */
    public ChatTraceLogBuilder addLog(String content) {
      if (StringUtils.isNotEmpty(content)) {
        logContent.append(content).append("\n");
      }
      return this;
    }

    /**
     * 添加一行日志
     */
    public ChatTraceLogBuilder addLog(String content, Object... args) {
      logContent.append(String.format(content, args)).append("\n");
      return this;
    }

    /**
     * 标记失败
     */
    public ChatTraceLogBuilder failed() {
      this.failed = true;
      // 失败时自动结束
      end();
      return this;
    }

    /**
     * 标记结束
     */
    public ChatTraceLogBuilder end() {
      if (this.timeSpent == 0) {
        this.timeSpent = (int) (System.currentTimeMillis() - startTime.getTime());
      }
      return this;
    }

    /**
     * 构造会话日志
     */
    public ChatTraceLogDTO build() {
      // 确保日志已结束
      end();

      ChatTraceLogDTO dto = new ChatTraceLogDTO();
      dto.setStepName(stepName);
      dto.setInput(input);
      dto.setOutputJson(output);
      dto.setLogContent(logContent.length() > 0 ? logContent.toString().trim() : null);
      dto.setStartTime(startTime);
      dto.setTimeSpent(timeSpent);
      dto.setLogStatus(failed ? BaseConsts.STATE_FAIL : BaseConsts.STATE_SUCCESS);
      return dto;
    }
  }
}
