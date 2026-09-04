package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.TokenUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ToolCall;
import lombok.Data;

import java.util.List;

/**
 * 执行Prompt结果
 * 迁移对应关系: Go语言ExecutePromptResult结构体
 */
@Data
public class ExecutePromptResult {
  private String content;
  private List<ToolCall> toolCalls;
  private TokenUsage tokenUsage;
}
