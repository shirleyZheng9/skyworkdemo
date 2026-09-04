package com.iwhalecloud.bote.loop.evaluation.domain.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 评估器配置属性
 * 迁移对应关系: Go语言conf.IConfiger
 * - 功能: 评估器相关配置
 * - 配置项:
 * * evaluatorToolConf - 评估器工具配置
 * * evaluatorToolMapping - 评估器工具映射
 * * evaluatorPromptSuffix - 评估器提示后缀
 * * evaluatorPromptSuffixMapping - 评估器提示后缀映射
 */
@Data
@Component
@ConfigurationProperties(prefix = "evaluation.evaluator")
public class EvaluatorConfigProperties {

  /**
   * 评估器工具配置
   * 迁移对应关系: Go语言GetEvaluatorToolConf
   */
  private Map<String, ToolConfig> toolConf = new java.util.HashMap<>();

  /**
   * 评估器工具映射
   * 迁移对应关系: Go语言GetEvaluatorToolMapping
   */
  private Map<String, String> toolMapping = new java.util.HashMap<>();

  /**
   * 评估器提示后缀
   * 迁移对应关系: Go语言GetEvaluatorPromptSuffix
   */
  private Map<String, String> promptSuffix = new java.util.HashMap<>();

  /**
   * 评估器提示后缀映射
   * 迁移对应关系: Go语言GetEvaluatorPromptSuffixMapping
   */
  private Map<String, String> promptSuffixMapping = new java.util.HashMap<>();

  @Data
  public static class ToolConfig {
    private String type;
    private String description;
    private FunctionConfig function;
  }

  @Data
  public static class FunctionConfig {
    private String name;
    private String description;
    private String parameters;
  }
}
