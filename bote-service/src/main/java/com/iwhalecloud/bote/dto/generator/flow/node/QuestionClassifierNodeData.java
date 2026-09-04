package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.step.QuestionClassifierStep.QuestionClassification;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 问题分类节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QuestionClassifierNodeData {
  /** 大模型 ID */
  private Long modelId;
  /** 问题（取值表达式） */
  private String question;
  /** 指令，可选，支持引用变量 */
  private String instruction;
  /** 分类列表 */
  private List<QuestionClassification> classifications;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
}
