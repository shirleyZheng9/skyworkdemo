package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料基本信息 Entity
 *
 * @author auto
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_PLAT_BOT_INFO")
public class PlatBotInfoEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long platBotId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "BOT_DESC")
  @Schema(description = "详细说明")
  private String botDesc;
  @DiffField(name = "REQ_URL")
  @Schema(description = "智能应用使用路径")
  private String reqUrl;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "BOT_NAME")
  @Schema(description = "应用名称")
  private String botName;
  @DiffField(name = "BOT_CODE")
  @Schema(description = "应用编码")
  private String botCode;
  @DiffField(name = "BOT_ICON")
  @Schema(description = "应用图标")
  private String botIcon;
  @DiffField(name = "STATUS")
  @Schema(description = "状态: 0: 下架，1: 上架")
  private String status;
  @DiffField(name = "BOT_TYPE")
  @Schema(description = "机器人类型：平台 platform, 其它 other")
  private String botType;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人ID")
  private Long botId;
  @DiffField(name = "OWNER_TENANT_ID")
  @Schema(description = "拥有者租户ID")
  private Long ownerTenantId;
}
