package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.bot.BotSceneSkillEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 场景关联的技能
 *
 * @author chen.linfa
 * @since 2024-09-11
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotSceneSkillDTO extends BotSceneSkillEntity {
  @Schema(description = "技能入参变量，用于提示词生成")
  private List<LogicViewVariableDTO> inputVariables;
  @Schema(description = "技能详情")
  private Map<String, Object> skillOriginInfo;
  @Schema(description = "技能配置")
  private Map<String, Object> skillConfig;
  @Schema(description = "是否是平台级技能")
  private Boolean platform;
  @Schema(description = "技能匹配内容，用于AI场景匹配技能")
  private String skillMatchContent;
  @Schema(description = "是否删除")
  @JsonIgnore
  private boolean deleted;

  /**
   * 解析技能配置
   */
  public void parseSkillConfig() {
    if (StringUtils.isNotEmpty(getSkillConfigJson())) {
      skillConfig = JsonUtil.parseJsonRequired(getSkillConfigJson(), new TypeReference<Map<String, Object>>() {
      });
    }
    setSkillConfigJson(null);
  }
  /**
   * 解析技能原始信息
   */
  public void parseSkillOriginInfo() {
    if (StringUtils.isNotEmpty(getSkillJson())) {
      skillOriginInfo = JsonUtil.parseJsonRequired(getSkillJson(), new TypeReference<Map<String, Object>>() {
      });
    }
    setSkillJson(null);
  }
  /**
   * 保存技能配置
   */
  public void saveSkillConfig() {
    if (MapUtils.isNotEmpty(skillConfig)) {
      setSkillConfigJson(JsonUtil.toJsonString(skillConfig));
    }
    else {
      setSkillConfigJson(null);
    }
  }

  /**
   * 判断 插件 技能是否启用了插件市场
   */
  public boolean enabledPluginHub() {
    parseSkillOriginInfo();
    return MapUtils.isNotEmpty(skillOriginInfo) && BooleanUtils.isTrue(MapUtils.getBoolean(skillOriginInfo, "isPluginHub"));
  }
}
