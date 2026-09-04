package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.VariableVal;
import lombok.Data;

import java.util.List;

/**
 * 执行Prompt参数
 * 迁移对应关系: Go语言ExecutePromptParam结构体
 */
@Data
public class ExecutePromptParam {
  private Long promptId;
  private String promptVersion;
  private List<VariableVal> variables;
  private List<Message> history;
}
