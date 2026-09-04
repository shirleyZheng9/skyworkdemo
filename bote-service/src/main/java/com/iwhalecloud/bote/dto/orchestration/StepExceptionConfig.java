package com.iwhalecloud.bote.dto.orchestration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 节点异常处理配置
 *
 * @author bianjp
 * @since 2025-08-26
 */
@Getter
@Setter
@ToString
public class StepExceptionConfig {
  /** 异常处理策略 */
  private StepExceptionProcessingStrategy strategy;
  /** 默认值 */
  private String fallbackValue;
  /** 异常分支编码 */
  private String exceptionBranch;

  /** 缓存解析后的默认值 */
  @JsonIgnore
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private Object parsedFallbackValue;

  /**
   * 解析默认值
   */
  public Object parseFallbackValue(AbstractStep step, BiFunction<AbstractStep, Object, Object> converter) {
    // 不需要防止并发，重复处理了也没影响
    if (parsedFallbackValue == null && StringUtils.isNotEmpty(fallbackValue) && !"null".equals(fallbackValue)) {
      Object value;
      try {
        value = JsonUtil.getObjectMapper().readValue(fallbackValue, Object.class);
      }
      catch (JsonProcessingException e) {
        throw new BssException(String.format("步骤【%s】的异常处理配置的设定内容不是合法的 JSON: %s", step.getName(), e.getMessage()), e);
      }
      // 转换参数结构
      // 虽然保存流程时对参数结构做了校验，但运行时仍需做转换，因为流程的 DSL 会使用 JSON 存储在数据库中，而 JSON 不支持部分数据类型（比如日期）
      if (value != null) {
        parsedFallbackValue = converter.apply(step, value);
      }
    }
    return parsedFallbackValue;
  }

  /**
   * 异常处理策略
   */
  public enum StepExceptionProcessingStrategy {
    /** 中断流程 */
    @JsonProperty("abort")
    ABORT,
    /** 返回设定内容 */
    @JsonProperty("fallback")
    FALLBACK,
    /** 执行异常分支 */
    @JsonProperty("branch")
    BRANCH
  }
}
