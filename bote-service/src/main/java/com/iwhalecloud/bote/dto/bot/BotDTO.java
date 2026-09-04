package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.AiSkillDTO;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.entity.bot.BotEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 应用
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotDTO extends BotEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "修改人图标")
  private String updatorIcon;
  @Schema(description = "是否为收藏机器人")
  private String isFavor;
  @Schema(description = "关联的场景")
  private List<BotSceneRelDTO> scenes;
  @Schema(description = "欢迎页基础定义")
  private Map<String, Object> pageBaseInfoJson;
  @Schema(description = "关联的场景")
  private Map<String, Object> pageSettingInfoJson;
  @Schema(description = "智能体规划策略")
  private SimpleAgentStrategyDTO agentStrategyJson;
  @Schema(description = "智能应用是否发布到应用广场")
  private String isPublish;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "百应平台发布状态（S:成功，F:失败等）")
  private String beyondPublishStatus;
  @Schema(description = "策略类型")
  private String strategyType;
  @Schema(description = "空间ID")
  private Long spaceId;
  @Schema(description = "关联的工作区")
  private List<AiWorkspaceDTO> workspaces;
  @Schema(description = "关联的模型")
  private AiModelDTO aiModel;
  @Schema(description = "关联的技能列表")
  private List<AiSkillDTO> skills;
}
