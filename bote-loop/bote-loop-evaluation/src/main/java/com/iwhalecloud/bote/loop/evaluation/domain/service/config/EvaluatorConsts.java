package com.iwhalecloud.bote.loop.evaluation.domain.service.config;

/**
 * 评估器常量
 * 迁移对应关系: Go语言consts包
 * - 功能: 定义评估器相关常量
 * - 常量:
 * * DEFAULT_EVALUATOR_TOOL_KEY - 默认评估器工具键
 * * DEFAULT_EVALUATOR_PROMPT_SUFFIX_KEY - 默认评估器提示后缀键
 */
public class EvaluatorConsts {

  /**
   * 默认评估器工具键
   * 迁移对应关系: Go语言DefaultEvaluatorToolKey
   */
  public static final String DEFAULT_EVALUATOR_TOOL_KEY = "default_evaluator_tool_key";

  /**
   * 默认评估器提示后缀键
   * 迁移对应关系: Go语言DefaultEvaluatorPromptSuffixKey
   */
  public static final String DEFAULT_EVALUATOR_PROMPT_SUFFIX_KEY = "default_evaluator_prompt_suffix_key";
}
