package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.entity.bot.BotUserExperienceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户会话辅助信息
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BotUserExperienceDTO extends BotUserExperienceEntity {
  @Schema(description = "更新人名称")
  private String updatorName;
  @Schema(description = "场景名称")
  private String sceneName;
  @Schema(description = "页面信息")
  private JsonNode pageContentInfo;
  @Schema(description = "场景状态")
  private String sceneStatus;
  @Schema(description = "静态数据列表")
  private Map<String, List<SimpleAttrDTO>> staticCodeList;
}
