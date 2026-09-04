package com.iwhalecloud.bote.dto.orchestration.step;

import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import java.util.List;

import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 问题分类步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class QuestionClassifierStep extends AbstractStep {
  /** 问题（取值表达式） */
  private String question;
  /** 大模型 ID（字面量或引用表达式） */
  private String modelId;
  /** 指令的提示词 ID, 可选 */
  private Long promptId;
  /** 提示词参数 */
  private List<ParameterSpec> promptParameters;
  /** 指令，可选，支持引用变量 */
  private String instruction;
  /** 分类列表 */
  private List<QuestionClassification> classifications;
  /** 识别不到分类时的下一步 */
  private String elseStep;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 自定义模型配置 */
  private CustomModelConfig customModelConfig;

  public QuestionClassifierStep() {
    super(StepType.QUESTION_CLASSIFIER);
  }

  /**
   * 问题分类的单个分类
   *
   * @author bianjp
   * @since 2024-10-21
   */
  @Getter
  @Setter
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionClassification {
    /** 分类 ID, 自动生成（从 1 开始递增），与列表顺序无关（界面上删除前面的分类时不要调整 id） */
    private Long id;
    /** 分类名称 */
    private String name;
    /** 连接的节点编码 */
    private String next;
  }
}
