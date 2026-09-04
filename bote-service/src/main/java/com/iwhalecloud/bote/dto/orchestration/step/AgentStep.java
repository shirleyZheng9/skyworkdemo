package com.iwhalecloud.bote.dto.orchestration.step;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import com.iwhalecloud.bote.dto.model.VisionConfig;
import com.iwhalecloud.bote.dto.orchestration.AbstractStep;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import java.util.List;

import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent 步骤
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@Setter
public class AgentStep extends AbstractStep {
  /** 大模型 ID（字面量或 {@code $} 引用，与知识库 ID 配置方式一致） */
  private String modelId;
  /** 提示词 ID, 可选 */
  private Long promptId;
  /** 提示词参数 */
  private List<ParameterSpec> promptParameters;
  /** 提示词（模板字符串，支持引用变量），非必填 */
  private String promptContent;
  /** 是否展示工具调用，未配置时默认为 true */
  private Boolean showToolInvocation;
  /** 视觉配置，可选 */
  private VisionConfig vision;
  /** 工具使用规则提示词 */
  private String toolUseRulesPrompt;
  /** 技能列表 */
  private List<LlmSkillItem> skills;
  /** 动态技能配置（开启时仍可使用 {@link #skills}, 二者会合并) */
  private DynamicSkillsConfig dynamicSkillsConfig;
  /** 自定义 MCP 服务 */
  private CustomMcpService customMcpService;
  /** 文件处理策略(只处理 MCP 工具返回的图片) */
  private FileProcessingStrategy fileProcessingStrategy;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 自定义大模型配置 */
  private CustomModelConfig customModelConfig;

  public AgentStep() {
    super(StepType.AGENT);
  }

  /**
   * 自定义 MCP 服务
   */
  @Getter
  @Setter
  @ToString
  public static class CustomMcpService {
    /** 是否启用 */
    private Boolean enabled;
    /** 服务类型(sse, streamable) */
    private String serverType;
    /** 服务名称 */
    private String serverName;
    /** 服务地址(取值表达式) */
    private String serverUrl;
  }

  /**
   * 动态技能配置
   */
  @Getter
  @Setter
  @ToString
  public static class DynamicSkillsConfig {
    /** 是否启用 */
    private Boolean enabled;
    /** 技能列表（取值表达式，值应为数组，结构与 {@link AgentStep#skills} 一致） */
    private String skills;
  }

  /**
   * 文件处理策略
   */
  public enum FileProcessingStrategy {
    /** 不处理（保持原样，调用大模型时会占用很多 token） */
    @JsonProperty("none")
    NONE,
    /** 替换为链接 */
    @JsonProperty("url")
    URL,
    /** 丢弃 */
    @JsonProperty("drop")
    DROP,
  }
}
