package com.iwhalecloud.bote.dto.bot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模板应用
 *
 * @author qian.sisheng
 * @since 2025-05-29
 */
@Setter
@Getter
@ToString
public class TemplateBotDTO {
  @Schema(description = "模板ID")
  private Long platBotId;
  @Schema(description = "机器人名称")
  private String botName;
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "拥有者租户ID")
  private Long ownerTenantId;
  @Schema(description = "被授权租户ID")
  private Long authTenantId;
  @Schema(description = "被授权用户ID")
  private Long userId;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "机器人描述")
  private String botDesc;
  @Schema(description = "机器人用途说明")
  private String botUse;
  @Schema(description = "机器人图标")
  private String botIcon;
  @Schema(description = "智能应用使用路径")
  private String reqUrl;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "创建人编码")
  private String creatorCode;
  @Schema(description = "创建人图标")
  private String creatorIcon;
  @Schema(description = "创建人ID")
  private Long creatorId;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "机器人类型：平台 platform, 其它 other")
  private String botType;
  @Schema(description = "是否平台发布：T:是，F:否")
  private String isPlatformPublish;
}
