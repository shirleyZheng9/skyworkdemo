package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.bot.BotAuthApplyEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * 智能应用授权申请 DTO
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_BOT_AUTH_APPLY")
@JsonInclude(Include.NON_NULL)
public class BotAuthApplyDTO extends BotAuthApplyEntity {
  @Schema(description = "应用名称")
  private String botName;
  @Schema(description = "申请人用户名称")
  private String userName;
  @Schema(description = "租户名称")
  private String tenantName;
  @Schema(description = "审核人姓名")
  private String auditUserName;
  @Schema(description = "租户ID列表")
  private List<Long> tenantIds;
  @Schema(description = "用户ID列表")
  private List<Long> userIds;
  @Schema(description = "组织ID列表")
  private List<Long> authOrgIds;
  @Schema(description = "申请与审批标题")
  private String applyTitle;
  @Schema(description = "分类名称")
  private String catalogName;
  @Schema(description = "是否是BoteClaw")
  private String isBoteClaw;
  @Schema(description = "授权信息Map")
  private Map<String, List<BotAuthDTO>> botAuthInfoMap;
}
