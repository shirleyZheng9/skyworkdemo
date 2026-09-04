package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.skill.LlmSkillItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单场景信息
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleBotSceneDTO {
  @Schema(description = "场景 ID")
  private Long sceneId;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "场景描述")
  private String sceneDesc;
  @Schema(description = "场景类型")
  private String sceneType;
  @Schema(description = "场景开场白")
  private String prologue;
  @Schema(description = "是否允许跳变")
  private Boolean jumpEnabled;
  @Schema(description = "是否自动启动")
  private Boolean autoStartEnabled;
  @Schema(description = "是否确认启动")
  private Boolean confirmEnabled;
  @Schema(description = "应用 ID，缓存对象中没有该值，慎用")
  private Long botId;
  @Schema(description = "流程步骤 JSON")
  private String flowStepJson;
  @Schema(description = "模型 ID")
  private Long modelId;
  @Schema(description = "模型配置 JSON")
  private String modelConfigJson;
  @Schema(description = "是否启用长期记忆")
  private String longTermMemoryEnabled;

  // 仅供 SceneCache 读写，其它地方不要直接使用。只放在本地缓存，不写入分布式缓存
  @JsonIgnore
  @Schema(description = "简单场景的技能")
  private List<LlmSkillItem> skillItems;
  // 仅供 SceneCache 读写，其它地方不要直接使用。只放在本地缓存，不写入分布式缓存
  @JsonIgnore
  @Schema(description = "claw 场景工作区")
  private List<SimpleAiWorkspaceDTO> clawWorkspaces;
  // 仅供 SceneCache 读写，其它地方不要直接使用。只放在本地缓存，不写入分布式缓存
  @JsonIgnore
  @Schema(description = "claw 场景环境变量")
  private Map<String, String> clawEnvVariables;
}
