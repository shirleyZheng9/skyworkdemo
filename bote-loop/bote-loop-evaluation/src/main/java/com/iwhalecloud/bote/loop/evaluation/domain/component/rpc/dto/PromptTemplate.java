package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import lombok.Data;

import java.util.List;

/**
 * Prompt模板
 * 迁移对应关系: Go语言PromptTemplate结构体
 */
@Data
public class PromptTemplate {
  private List<VariableDef> variableDefs;
}
