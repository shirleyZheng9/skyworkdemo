package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.entity.bot.BotAuthEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 机器人授权 DTO
 *
 * @author auto
 * @since 2025-03-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class BotAuthDTO extends BotAuthEntity {
  @Schema(description = "租户ID列表")
  private List<Long> tenantIds;
  @Schema(description = "用户ID列表")
  private List<Long> userIds;
  @Schema(description = "组织ID列表")
  private List<Long> authOrgIds;
  @Schema(description = "授权类型")
  private String authType;
  @Schema(description = "授权子类型")
  private String authSubType;
  @Schema(description = "租户名称")
  private String tenantName;
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "组织名称")
  private String orgName;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "机器人用途")
  private String botUse;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "授权ID列表")
  private List<Long> authIds;

  /**
   * 是否授权给所有用户
   * @return 结果
   */
  @JsonIgnore
  public boolean isAuthorizedToAllUsers() {
    return getAuthTenantId() == -1L && getUserId() == -1L && getOrgId() == -1L;
  }

  /**
   * 是否授权给部分租户
   * @return 结果
   */
  @JsonIgnore
  public boolean isAuthorizedToPartTenants() {
    return getAuthTenantId() != -1L && getUserId() == -1L && getOrgId() == -1L;
  }

  /**
   * 是否授权给部分用户
   * @return 结果
   */
  @JsonIgnore
  public boolean isAuthorizedToPartUsers() {
    return getAuthTenantId() == -1L && getUserId() != -1L && getOrgId() == -1L;
  }

  @JsonIgnore
  public boolean isAuthorizedToPartOrgs() {
    return getOrgId() != -1L && getAuthTenantId() == -1L && getUserId() == -1L;
  }
}
