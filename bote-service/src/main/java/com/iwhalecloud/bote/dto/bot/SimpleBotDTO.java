package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.app.SimpleWebAppDTO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 机器人简单信息
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleBotDTO {
  @Schema(description = "机器人 ID")
  private Long botId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "机器人名称")
  private String botName;
  @Schema(description = "机器人用途")
  private String botUse;
  @Schema(description = "开场白")
  private String prologue;
  @Schema(description = "图标")
  private String botIcon;
  @Schema(description = "是否默认")
  private String isDefault;
  @Schema(description = "机器人状态")
  private String botStatus;
  @Schema(description = "欢迎页版式信息")
  private String pageSettingInfo;
  @Schema(description = "欢迎页版式信息 JSON")
  private Map<String, Object> pageSettingInfoJson;
  @Schema(description = "欢迎页图标")
  private String pageSettingIcon;
  @Schema(description = "智能体规划策略")
  private String agentStrategy;
  @Schema(description = "是否已收藏")
  private Boolean isFavor;
  @Schema(description = "默认场景 ID")
  private Long defaultSceneId;
  @Schema(description = "默认场景名称")
  private String defaultSceneName;
  @Schema(description = "会话辅助信息")
  private Map<String, List<SimpleBotUserExperienceDTO>> experiences;
  @Schema(description = "场景列表")
  private List<SimpleBotSceneDTO> scenes;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "租户funcSwitch设置信息")
  private Map<String, Object> funcSwitch;
  @Schema(description = "更新时间")
  private Date updatedTime;
  @Schema(description = "网页应用")
  private SimpleWebAppDTO webApp;
  @Schema(description = "数据来源")
  private String dataFrom;
  @Schema(description = "是否是BoteClaw")
  private String isBoteClaw;
  @Schema(description = "创建人ID")
  private Long creatorId;
  @Schema(description = "是否是创建人")
  private String isCreator;

  /**
   * 判断是否存在策略配置
   */
  @JsonIgnore
  public boolean hasAgentStrategy() {
    // 全局开关
    if (BooleanUtils.isNotTrue(SystemParameter.AGENT_STRATEGY_ENABLED.getBooleanValueFromDb())) {
      return false;
    }
    if (StringUtils.isNotEmpty(agentStrategy)) {
      SimpleAgentStrategyDTO strategy = JsonUtil.parseJson(agentStrategy, SimpleAgentStrategyDTO.class);
      if (strategy != null) {
        return !BaseConsts.AGENT_STRATEGY_NONE.equals(strategy.getType());
      }
    }
    return false;
  }
}
