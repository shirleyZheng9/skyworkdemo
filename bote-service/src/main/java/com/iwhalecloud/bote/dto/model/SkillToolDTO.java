package com.iwhalecloud.bote.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.util.JsonSchemaUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 技能工具
 *
 * @author bianjp
 * @since 2025-04-17
 */
@Getter
@Setter
@ToString
public final class SkillToolDTO {
  /** 技能类型 */
  private String skillType;
  /** 技能 ID */
  private Long skillId;
  /** 自定义入参(Agent 节点给技能的部分参数赋值时使用) */
  private ParameterSpec customParameters;
  /** 大模型工具 */
  private Tool tool;
  /** 步骤 */
  private AbstractStep step;

  /**
   * 创建构造器
   */
  public static SkillToolBuilder builder(String skillType, Long skillId, AbstractStep step) {
    return new SkillToolBuilder(skillType, skillId, step);
  }

  private SkillToolDTO(String skillType, Long skillId, @Nullable ParameterSpec customParameters, Tool tool, AbstractStep step) {
    this.skillType = skillType;
    this.skillId = skillId;
    this.customParameters = customParameters;
    this.tool = tool;
    this.step = step;
    step.setCode(tool.getFunction().getName());
    if (step.getName() == null) {
      step.setName(tool.getFunction().getDescription());
    }
  }

  /**
   * 是否是前端动作类型
   */
  @JsonIgnore
  public boolean isFrontendActions() {
    return StepType.PAGE_FUNC.equals(skillType) || StepType.PAGE.equals(skillType);
  }


  /**
   * 获取工具编码（技能编码）
   */
  @JsonIgnore
  public String getToolCode() {
    return tool.getFunction().getName();
  }

  /**
   * 技能工具构造器
   */
  public static class SkillToolBuilder {
    /** 技能类型 */
    private final String skillType;
    /** 技能 ID */
    private final Long skillId;
    /** 技能入参(工作流 Agent 节点配置了部分入参对模型不可见时使用) */
    private ParameterSpec customParameters;
    /** 大模型工具 */
    private Tool tool;
    /** 步骤 */
    private final AbstractStep step;

    public SkillToolBuilder(String skillType, Long skillId, AbstractStep step) {
      this.skillType = skillType;
      this.skillId = skillId;
      this.step = step;
    }

    /**
     * 设置工具
     *
     * @param name 工具名称
     * @param description 工具描述
     * @param parameter 参数结构
     * @param customParameters 自定义参数
     */
    public SkillToolBuilder tool(String name, String description, @Nullable ParameterSpec parameter, @Nullable ParameterSpec customParameters) {
      JsonSchemaNode inputSchema = JsonSchemaUtil.convertRoot(customParameters != null ? customParameters : parameter);
      this.tool = new Tool(name, description, inputSchema);
      this.customParameters = customParameters;
      return this;
    }

    /**
     * 设置工具
     *
     * @param name 工具名称
     * @param description 工具描述
     * @param parameter 参数结构
     * @param customParameters 自定义参数
     */
    public SkillToolBuilder tool(String name, String description, JsonSchemaNode parameter, @Nullable ParameterSpec customParameters) {
      JsonSchemaNode inputSchema = customParameters != null ? JsonSchemaUtil.convertRoot(customParameters) : parameter;
      this.tool = new Tool(name, description, inputSchema);
      this.customParameters = customParameters;
      return this;
    }

    public SkillToolDTO build() {
      return new SkillToolDTO(skillType, skillId, customParameters, tool, step);
    }
  }
}
