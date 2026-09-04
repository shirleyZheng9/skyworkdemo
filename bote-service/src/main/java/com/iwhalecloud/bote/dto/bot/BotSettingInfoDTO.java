package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Bot设置信息DTO
 *
 * @author tingyun.wang
 * @since 2025-07-25
 */
@Getter
@Setter
@ToString
public class BotSettingInfoDTO {

  @Schema(description = "智能应用ID")
  private Long botId;
  @Schema(description = "主题ID")
  private Long themeId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "页面设置信息")
  private Map<String, Object> pageSettingInfoJson;
  @Schema(description = "页面设置图标")
  private Map<String, Object> pageSettingIcon;
  @Schema(description = "应用的funcSwitch配置信息")
  private Map<String, Object> funcSwitch;

}
