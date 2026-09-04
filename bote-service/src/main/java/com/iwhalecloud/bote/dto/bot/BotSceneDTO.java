package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.scene.KnowledgeSceneSettingDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 场景
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class BotSceneDTO extends BotSceneEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "修改人图标")
  private String updatorIcon;
  @Schema(description = "流程图")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private SceneGraphDTO graph;
  @DiffField(childNode = true)
  @Schema(description = "场景变量", hidden = true)
  private BotSceneParamDTO param;
  @Schema(description = "入参")
  private ParameterSpec request;
  @Schema(description = "场景变量列表")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private List<ParameterSpec> variables;
  @Schema(description = "简单场景的提示词 ID")
  private Long promptId;
  @Schema(description = "简单场景的自定义提示词内容")
  private String scenePrompt;
  @DiffField(childNode = true)
  @Schema(description = "场景提示词，后端保存数据库时使用", hidden = true)
  @JsonIgnore
  private BotScenePromptDTO prompt;
  @Schema(description = "关联标签 ID 集合")
  private List<Long> labelIds;
  @DiffField(childNode = true)
  @Schema(description = "关联标签，后端保存数据库时使用，前端展示也需要")
  private List<LabelObjectRelDTO> labels;
  @Schema(description = "关联技能")
  private Map<String, List<BotSceneSkillDTO>> skills;
  @DiffField(childNode = true)
  @Schema(description = "关联技能，后端保存数据库时使用", hidden = true)
  @JsonIgnore
  private List<BotSceneSkillDTO> flatSkills;
  @Schema(description = "拷贝场景 ID")
  private Long copySceneId;
  @Schema(description = "简单场景，大纲树")
  private List<LogicViewDTO> logicViews;
  @Schema(description = "知识问答场景设置")
  private KnowledgeSceneSettingDTO knowledgeSetting;
  @Schema(description = "带有关联的应用")
  private Boolean withRelBot;
  @Schema(description = "流程步骤")
  @SuppressFBWarnings("SE_BAD_FIELD")
  private List<SimpleFlowStepDTO> flowSteps;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "智能生成场景进程ID")
  private String processId;
  @Schema(description = "自定义模型配置")
  private CustomModelConfig customModelConfig;

  // A2A 服务的属性，智能应用关联 A2A 服务功能使用
  @Schema(description = "A2A 服务的技能数量")
  private Integer skillCount;
  @Schema(description = "A2A 平台名称")
  private String platformName;
  @Schema(description = "A2A 平台图标")
  private String platformIcon;

  @Schema(description = "百应平台发布状态（S:成功，F:失败等）")
  private String beyondPublishStatus;

  @Schema(description = "claw 关联的环境变量")
  @DiffField(childNode = true)
  private List<ClawEnvVariableDTO> envVariables;

  @Schema(description = "claw 角色定义")
  private String agentPrompt;
  @Schema(description = "claw 资料配置")
  private String profilePrompt;
  @Schema(description = "claw 行为准则")
  private String soulPrompt;
  @Schema(description = "claw的工作空间")
  @DiffField(childNode = true)
  private List<ClawWorkspaceDTO> workspaces;
  /**
   * 解析参数
   */
  public void parseParams() {
    if (param != null) {
      if (StringUtils.isNotEmpty(param.getRequestJson())) {
        request = JsonUtil.parseJsonRequired(param.getRequestJson(), ParameterSpec.class);
      }
      if (StringUtils.isNotEmpty(param.getVariableJson())) {
        variables = JsonUtil.parseJsonRequired(param.getVariableJson(), new TypeReference<List<ParameterSpec>>() {
        });
      }
    }
    param = null;
  }
}
