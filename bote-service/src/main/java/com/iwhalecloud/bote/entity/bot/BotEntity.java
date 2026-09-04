package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 机器人 Entity
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot")
public class BotEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long botId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "BOT_NAME")
  @Schema(description = "机器人名称")
  @Size(max = 20, message = "智能应用名称超过限定长度20")
  private String botName;
  @DiffField(name = "BOT_USE")
  @Schema(description = "机器人用途说明")
  @Size(max = 400, message = "智能应用描述超过限定长度400")
  private String botUse;
  @DiffField(name = "BOT_ICON")
  @Schema(description = "图标")
  private String botIcon;
  @DiffField(name = "PROLOGUE")
  @Schema(description = "开场白")
  private String prologue;
  @DiffField(name = "IS_DEFAULT")
  @Schema(description = "是否租户下的默认机器人")
  private String isDefault;
  @DiffField(name = "BOT_STATUS")
  @Schema(description = "机器人状态: 0：未上架  1：已上架 2：已下架")
  private String botStatus;
  @DiffField(name = "MODEL_ID")
  @Schema(description = "大模型 ID")
  private Long modelId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @DiffField(name = "PAGE_BASE_INFO")
  @Schema(description = "欢迎页基础信息")
  private String pageBaseInfo;
  @DiffField(name = "PAGE_SETTING_INFO")
  @Schema(description = "欢迎页版式信息")
  private String pageSettingInfo;
  @DiffField(name = "PAGE_SETTING_ICON")
  @Schema(description = "欢迎页图标")
  private String pageSettingIcon;
  @DiffField(name = "AGENT_STRATEGY")
  @Schema(description = "智能体规划策略")
  private String agentStrategy;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "数据来源（10A：AI门户创建的BoteClaw）")
  private String dataFrom;
}
