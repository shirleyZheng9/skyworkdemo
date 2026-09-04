package com.iwhalecloud.bote.loop.evaluation.domain.service.config;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Tool;

import java.util.Map;

/**
 * 评估器配置服务接口
 * 迁移对应关系: Go语言conf.IConfiger
 * - 功能: 提供评估器配置访问
 * - 主要方法:
 * * getEvaluatorToolConf - 获取评估器工具配置
 * * getEvaluatorToolMapping - 获取评估器工具映射
 * * getEvaluatorPromptSuffix - 获取评估器提示后缀
 * * getEvaluatorPromptSuffixMapping - 获取评估器提示后缀映射
 */
public interface EvaluatorConfigService {

  /**
   * 获取评估器工具配置
   * 迁移对应关系: Go语言GetEvaluatorToolConf
   */
  Map<String, Tool> getEvaluatorToolConf();

  /**
   * 获取评估器工具映射
   * 迁移对应关系: Go语言GetEvaluatorToolMapping
   */
  Map<String, String> getEvaluatorToolMapping();

  /**
   * 获取评估器提示后缀
   * 迁移对应关系: Go语言GetEvaluatorPromptSuffix
   */
  Map<String, String> getEvaluatorPromptSuffix();

  /**
   * 获取评估器提示后缀映射
   * 迁移对应关系: Go语言GetEvaluatorPromptSuffixMapping
   */
  Map<String, String> getEvaluatorPromptSuffixMapping();
}
