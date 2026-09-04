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
 * 智能应用授权申请 Entity
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_BOT_AUTH_APPLY")
public class BotAuthApplyEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long applyId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用ID")
  private Long botId;
  @DiffField(name = "USER_ID")
  @Schema(description = "申请人用户ID")
  private Long userId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "申请人租户ID")
  private Long tenantId;
  @DiffField(name = "AUTH_TYPE")
  @Schema(description = "授权类型")
  private String authType;
  @DiffField(name = "AUTH_TENANT_IDS")
  @Schema(description = "授权目标租户ID列表")
  private String authTenantIds;
  @DiffField(name = "AUTH_USER_IDS")
  @Schema(description = "授权目标用户ID列表")
  private String authUserIds;
  @DiffField(name = "ORG_IDS")
  @Schema(description = "授权目标组织ID列表")
  private String orgIds;
  @DiffField(name = "BOT_DESC")
  @Schema(description = "应用描述")
  private String botDesc;
  @DiffField(name = "BOT_ICON")
  @Schema(description = "应用图片:发布后的应用封面")
  private String botIcon;
  @DiffField(name = "AUDIT_STATUS")
  @Schema(description = "审核状态（0:待审核, 1:已通过, 2:未通过）")
  private Integer auditStatus;
  @DiffField(name = "AUDIT_USER_ID")
  @Schema(description = "审核人ID")
  private Long auditUserId;
  @DiffField(name = "AUDIT_CONTENT")
  @Schema(description = "审核内容")
  private String auditContent;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "DATA_FROM")
  @Schema(description = "数据来源（10A：运行态创建的智能体）")
  private String dataFrom;

}
