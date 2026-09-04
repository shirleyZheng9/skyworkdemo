package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt评估器版本实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptEvaluatorVersion implements IEvaluatorVersion {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("evaluator_type")
  private EvaluatorType evaluatorType;

  @JsonProperty("evaluator_id")
  private Long evaluatorId;

  @JsonProperty("description")
  private String description;

  @JsonProperty("version")
  private String version;

  @JsonProperty("input_schemas")
  private List<ArgsSchema> inputSchemas;

  @JsonProperty("prompt_source_type")
  private PromptSourceType promptSourceType;

  @JsonProperty("prompt_template_key")
  private String promptTemplateKey;

  @JsonProperty("message_list")
  private List<Message> messageList;

  @JsonProperty("model_config")
  private ModelConfig modelConfig;

  @JsonProperty("tools")
  private List<Tool> tools;

  @JsonProperty("receive_chat_history")
  private Boolean receiveChatHistory;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;

  @JsonProperty("parse_type")
  private ParseType parseType;

  @JsonProperty("prompt_suffix")
  private String promptSuffix;

  @JsonProperty("pass_score")
  private Double passScore;

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  @Override
  public void setEvaluatorId(Long evaluatorId) {
    this.evaluatorId = evaluatorId;
  }

  @Override
  public Long getEvaluatorId() {
    return this.evaluatorId;
  }

  @Override
  public void setSpaceId(Long spaceId) {
    this.spaceId = spaceId;
  }

  @Override
  public Long getSpaceId() {
    return this.spaceId;
  }

  @Override
  public String getVersion() {
    return this.version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public void setBaseInfo(BaseInfo baseInfo) {
    this.baseInfo = baseInfo;
  }

  @Override
  public BaseInfo getBaseInfo() {
    return this.baseInfo;
  }

  @Override
  public void setTools(List<Tool> tools) {
    this.tools = tools;
  }

  @Override
  public String getPromptTemplateKey() {
    return this.promptTemplateKey;
  }

  @Override
  public void setPromptSuffix(String promptSuffix) {
    this.promptSuffix = promptSuffix;
  }

  @Override
  public ModelConfig getModelConfig() {
    return this.modelConfig;
  }

  @Override
  public void setParseType(ParseType parseType) {
    this.parseType = parseType;
  }

  /**
   * 验证输入数据
   * 迁移对应关系: Go语言PromptEvaluatorVersion.ValidateInput()
   */
  @Override
  public void validateInput(EvaluatorInputData input) {
    // 实现验证逻辑
    // 这里需要根据具体的业务逻辑来实现
  }

  /**
   * 验证基础信息
   * 迁移对应关系: Go语言PromptEvaluatorVersion.ValidateBaseInfo()
   */
  @Override
  public void validateBaseInfo() {
    // 实现验证逻辑
    // 这里需要根据具体的业务逻辑来实现
  }

  @Override
  public Double getPassScore() {
    return this.passScore;
  }

  @Override
  public void setPassScore(Double passScore) {
    this.passScore = passScore;
  }
}
