package com.iwhalecloud.bote.entity.portal;

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
 * 租户 Entity
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_tenant")
public class TenantEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long tenantId;
  @DiffField(name = "TENANT_CODE")
  @Schema(description = "租户编码")
  private String tenantCode;
  @DiffField(name = "TENANT_NAME")
  @Schema(description = "租户名称")
  private String tenantName;
  @DiffField(name = "TENANT_USE")
  @Schema(description = "用途")
  @Size(max = 10, message = "用途名称超过限定长度10")
  private String tenantUse;
  @DiffField(name = "TENANT_ICON")
  @Schema(description = "头像")
  private String tenantIcon;
  @DiffField(name = "PROLOGUE")
  @Schema(description = "开场白")
  @Size(max = 200, message = "开场白超过限定长度200")
  private String prologue;
  @DiffField(name = "QUESTION_SETTING_INFO")
  @Schema(description = "推荐问题")
  private String questionSettingInfo;
  @DiffField(name = "BOT_SETTING_INFO")
  @Schema(description = "推荐BOT")
  private String botSettingInfo;
  @DiffField(name = "APP_ID")
  @Schema(description = "灵犀低代码应用ID")
  private Long appId;
  @DiffField(name = "SYSTEM_TYPE")
  @Schema(description = "系统类型, lcdp: 灵犀低代码")
  private String systemType;
  @DiffField(name = "EXT_TENANT_ID")
  @Schema(description = "外系统租户ID")
  private Long extTenantId;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;
}
