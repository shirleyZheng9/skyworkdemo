package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 机器人授权 Entity
 *
 * @author auto
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_BOT_AUTH")
public class BotAuthEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long authId;
  @DiffField(name = "BOT_NAME")
  @Schema(description = "机器人名称")
  private String botName;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人ID")
  private Long botId;
  @DiffField(name = "OWNER_TENANT_ID")
  @Schema(description = "拥有者租户ID")
  private Long ownerTenantId;
  @DiffField(name = "AUTH_TENANT_ID")
  @Schema(description = "被授权租户ID")
  private Long authTenantId;
  @DiffField(name = "USER_ID")
  @Schema(description = "被授权用户ID")
  private Long userId;
  @DiffField(name = "ORG_ID")
  @Schema(description = "被授权组织ID")
  private Long orgId;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "BOT_DESC")
  @Schema(description = "机器人描述")
  private String botDesc;
  @Schema(description = "机器人图标")
  private String botIcon;
  @DiffField(name = "AUTH_PUBLISHER")
  @Schema(description = "授权发布人")
  private Long authPublisher;
  @DiffField(name = "AUTH_STATUS")
  @Schema(description = "授权状态（T:上架, F:下架）")
  private String authStatus;
  @DiffField(name = "AUTH_OPERATOR")
  @Schema(description = "授权操作人")
  private Long authOperator;
  @DiffField(name = "AUTH_OPERATE_TIME")
  @Schema(description = "授权操作时间")
  private Date authOperateTime;
  @Schema(description = "数据来源")
  private String dataFrom;

}
