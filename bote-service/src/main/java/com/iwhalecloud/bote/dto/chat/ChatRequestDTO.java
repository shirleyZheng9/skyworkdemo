package com.iwhalecloud.bote.dto.chat;

import com.iwhalecloud.bote.dto.planning.query.PlanParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 对话接口请求对象
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
@Schema(description = "对话接口请求对象")
public class ChatRequestDTO {
  @Schema(description = "当前用户所在的空间 ID")
  private Long spaceId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "当前机器人 ID, 意图识别时默认限定当前机器人下的场景，除非租户设置开启了多机器人意图识别")
  private Long botId;
  @Schema(description = "客户端 ID")
  private String clientId;
  @Schema(description = "链路追踪标识，用于统计大模型 token 用量。客户端生成，应全局唯一")
  private String traceId;
  @Schema(description = "会话 ID, 为空或 -1 时不记录会话消息")
  private Long sessionId;
  @Schema(description = "上下文 ID")
  private String contextId;
  @Schema(description = "当前场景 ID。-1 表示退出场景")
  private Long sceneId;
  @Schema(description = "当前场景名称")
  private String sceneName;
  @Schema(description = "上一个场景 ID，用于退出场景")
  private Long lastSceneId;
  @Schema(description = "上一个上下文 ID，用于退出场景")
  private String lastContextId;
  @Schema(description = "消息")
  private ChatRequestMessageDTO message;
  @Schema(description = "上下文参数")
  private Map<String, Object> contextParams;
  @Schema(description = "是否启用联网搜索")
  private Boolean webSearchEnabled;
  @Schema(description = "计划相关参数")
  private PlanParams planParams;
  @Schema(description = "通用对话标识，用于标识对话来源于首页")
  private Boolean commonFlag;
  @Schema(description = "意图识别，开启个性化规划标识。用于规划模式应用，选择性使用个性化规划功能，默认开启。(来源于 BSS 需求)")
  private Boolean planable;
  @Schema(description = "本轮对话优先使用的 Agent Skill ID 列表。大模型推理时优先考虑这些技能；若对话内容需要其他已启用技能也可使用")
  private List<Long> agentSkillIds;
  @Schema(description = "渠道类型")
  private String channelType;
  @Schema(description = "用户的操作系统(名称 + 版本)", example = "Windows 11")
  private String os;
  @Schema(description = "工作目录")
  private String workDir;
  @Schema(description = "需要调试的 Agent Skill ID")
  private Long debugSkillId;
}
