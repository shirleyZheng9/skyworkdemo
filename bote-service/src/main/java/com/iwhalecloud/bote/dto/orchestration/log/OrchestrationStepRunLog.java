package com.iwhalecloud.bote.dto.orchestration.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iwhalecloud.bote.common.json.FileAwareJsonSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;

/**
 * 场景编排步骤执行日志
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@Schema(description = "逻辑编排步骤执行日志")
public class OrchestrationStepRunLog {
  @Schema(description = "日志 ID")
  private Long logId;
  @Schema(description = "步骤名称")
  private String stepName;
  @Schema(description = "步骤编码")
  private String stepCode;
  @Schema(description = "步骤类型")
  private String stepType;
  @Schema(description = "是否成功")
  private Boolean success;
  @Schema(description = "开始时间")
  private Date startTime;
  @Schema(description = "耗时（毫秒）")
  private Long timeSpent;
  @Schema(description = "错误信息")
  private String failMsg;
  @Schema(description = "错误堆栈")
  private String failStack;
  @Schema(description = "入参")
  @JsonSerialize(using = FileAwareJsonSerializer.class)
  private Object input;
  @Schema(description = "出参")
  @JsonSerialize(using = FileAwareJsonSerializer.class)
  private Object output;
  @Schema(description = "日志信息列表。各个步骤执行器中自行添加的调试信息")
  private List<String> log;
  @Schema(description = "内部服务的执行日志")
  private List<OrchestrationStepRunLog> innerServiceLogs;
  @Schema(description = "内部服务的执行日志 ID")
  private Long innerServiceLogId;
  @Schema(description = "并行分支的执行日志。每个子列表表示一个分支")
  private List<List<OrchestrationStepRunLog>> parallelBranchesLogs;
  @Schema(description = "流程ID")
  private Long flowId;
  @Schema(description = "智能体ID")
  private Long sceneId;
  @Schema(description = "租户ID")
  private Long tenantId;

  /**
   * 判断是否执行结束
   */
  @JsonIgnore
  public boolean isFinished() {
    return success != null;
  }

  /**
   * 执行成功
   */
  public void succeed() {
    this.success = true;
    this.timeSpent = System.currentTimeMillis() - startTime.getTime();
  }

  /**
   * 执行成功
   */
  public void succeed(@Nullable Object output) {
    succeed();
    this.output = output;
  }

  /**
   * 执行失败
   */
  public void fail(Throwable throwable) {
    this.success = false;
    this.timeSpent = System.currentTimeMillis() - startTime.getTime();
    this.failMsg = throwable.getMessage();
    this.failStack = ExceptionUtils.getStackTrace(throwable);
  }

  /**
   * 添加一行日志
   *
   * @param format 日志格式。使用 String#format 格式，可包含占位符，以避免不需要生成日志拼接字符串的开销
   * @param args 参数列表。支持使用 Supplier 以避免不需要生成日志时的开销
   */
  public void addLog(String format, Object... args) {
    if (log == null) {
      log = new ArrayList<>();
    }

    if (args.length == 0) {
      log.add(format);
    }
    else {
      // 解析参数列表
      Object[] resolvedArgs = Arrays.stream(args).map(a -> a instanceof Supplier ? ((Supplier<?>) a).get() : a).toArray();
      log.add(String.format(format, resolvedArgs));
    }
  }
}
