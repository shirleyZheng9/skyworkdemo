package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 租户设置信息 Entity
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_TENANT_SETTING_INFO")
public class TenantSettingInfoEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long settingId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人ID")
  private Long botId;
  @DiffField(name = "FUNC_TYPE")
  @Schema(description = "功能类型")
  private String funcType;
  @DiffField(name = "SETTING_INFO")
  @Schema(description = "租户设置信息")
  private String settingInfo;
}
