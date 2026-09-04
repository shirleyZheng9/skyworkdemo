package com.iwhalecloud.bote.dto.orchestration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.json.FileAwareJsonSerializer;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.orchestration.log.OrchestrationStepRunLog;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.lang.Nullable;

/**
 * 场景编排引擎执行响应对象
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
@ToString
@Schema(description = "逻辑编排引擎执行响应")
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class OrchestrationEngineResponse {
  @Schema(description = "是否成功")
  private Boolean success;
  @Schema(description = "耗时（毫秒）")
  private Long timeSpent;
  @Schema(description = "错误信息")
  private String failMsg;
  @Schema(description = "错误堆栈")
  private String failStack;
  @Schema(hidden = true)
  @JsonIgnore
  private BssException exception;
  @Schema(description = "是否是对话流", hidden = true)
  @JsonIgnore
  private Boolean chatflow;
  @Schema(description = "是否结束场景")
  private Boolean sceneFinished;
  @Schema(description = "全局变量")
  private Map<String, Object> globalVariables;
  @Schema(description = "出参（仅用于任务型工作流）")
  @JsonSerialize(using = FileAwareJsonSerializer.class)
  private Map<String, Object> output;
  @Schema(description = "回复列表（仅用于对话型工作流）")
  private List<ReplyDTO> replies;
  @Schema(description = "步骤执行日志。只在运行模式为调试时返回；只包含已执行的步骤，按执行顺序排序")
  private List<OrchestrationStepRunLog> stepLogs;
  @Schema(description = "日志 ID", hidden = true)
  @JsonIgnore
  private Long logId;

  /**
   * 构造执行成功响应
   */
  public static OrchestrationEngineResponse success(SceneOrchestrationContext context) {
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    response.setSuccess(true);
    response.setTimeSpent(System.currentTimeMillis() - context.getStartTime().getTime());
    response.setSceneFinished(context.getSceneFinished());
    response.setOutput(context.getOutputParameters());
    return response;
  }

  /**
   * 构造执行失败响应
   *
   * @param startTime 开始时间
   * @param failMsg 错误信息
   */
  public static OrchestrationEngineResponse fail(Date startTime, String failMsg) {
    return fail(startTime, failMsg, null);
  }

  /**
   * 构造执行失败响应
   *
   * @param startTime 开始时间
   * @param throwable 异常实例
   */
  public static OrchestrationEngineResponse fail(Date startTime, Throwable throwable) {
    return fail(startTime, null, throwable);
  }

  /**
   * 构造执行失败响应
   *
   * @param startTime 开始时间
   * @param failMsg 错误信息
   * @param throwable 异常实例
   */
  public static OrchestrationEngineResponse fail(Date startTime, @Nullable String failMsg, @Nullable Throwable throwable) {
    OrchestrationEngineResponse response = new OrchestrationEngineResponse();
    response.setSuccess(false);
    response.setTimeSpent(System.currentTimeMillis() - startTime.getTime());
    response.setFailMsg(StringUtils.isNotEmpty(failMsg) ? failMsg : ExpUtil.getMsg(throwable));
    if (throwable != null) {
      response.setFailStack(ExceptionUtils.getStackTrace(throwable));
    }
    return response;
  }

  /**
   * 提取回复
   *
   * @return (回复文本, 消息类型, 消息内容)
   */
  public Triple<String, ChatMessageType, Object> extractReply() {
    if (replies == null || replies.isEmpty()) {
      return Triple.of(null, null, null);
    }
    // 回复文本，只取最后一个文本回复
    String replyText = replies.reversed().stream().filter(r -> r.getType() == ChatMessageType.TEXT).findFirst().map(ReplyDTO::getText).orElse("");
    // SSE 消息类型
    ChatMessageType msgType = null;
    // 消息内容
    Object msgContent = null;
    // 倒序遍历，只取最后一个事件
    for (ReplyDTO reply : replies.reversed()) {
      if (reply.getType() == ChatMessageType.PAGE) {
        msgType = reply.getType();
        msgContent = reply.getPage();
        break;
      }
      else if (reply.getType() == ChatMessageType.PAGE_FUNC) {
        msgType = reply.getType();
        msgContent = reply.getPageFunc();
        break;
      }
      else if (reply.getType() == ChatMessageType.A2UI) {
        msgType = reply.getType();
        msgContent = reply.getA2uiEvents();
        break;
      }
    }
    return Triple.of(replyText, msgType, msgContent);
  }
}
