package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;

/**
 * 评估器版本接口
 * 迁移对应关系: Go语言IEvaluatorVersion
 * - 功能: 评估器版本接口定义
 * - 方法: 各种getter和setter方法，以及验证方法
 */
public interface IEvaluatorVersion {
  void setId(Long id);

  Long getId();

  void setEvaluatorId(Long evaluatorId);

  Long getEvaluatorId();

  void setVersion(String version);

  String getVersion();

  void setSpaceId(Long spaceId);

  Long getSpaceId();

  void setDescription(String description);

  String getDescription();

  void setBaseInfo(BaseInfo baseInfo);

  BaseInfo getBaseInfo();

  void setTools(List<Tool> tools);

  String getPromptTemplateKey();

  void setPromptSuffix(String promptSuffix);

  ModelConfig getModelConfig();

  void setParseType(ParseType parseType);

  void validateInput(EvaluatorInputData input);

  void validateBaseInfo();

  Double getPassScore();

  void setPassScore(Double passScore);
}
