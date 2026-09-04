package com.iwhalecloud.bote.entity.a2a;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A2A 平台
 *
 * @author bianjp
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "A2A 平台")
@DiffNode(name = "bt_a2a_platform")
public class A2aPlatformEntity extends BaseEntity {
  @Schema(description = "平台 ID")
  @DiffId
  protected Long platformId;
  @Schema(description = "平台名称")
  @DiffField
  protected String platformName;
  @Schema(description = "平台编码")
  @DiffField
  protected String platformCode;
  @Schema(description = "平台描述")
  @DiffField
  protected String platformDesc;
  @Schema(description = "平台图标")
  @DiffField
  protected String platformIcon;
  @Schema(description = "发布密钥")
  @DiffField
  protected String publishKey;
  @Schema(description = "鉴权配置(JSON)")
  @DiffField
  protected String authConfigJson;
  @Schema(description = "鉴权扩展服务函数")
  @DiffField
  protected Long authExtFuncId;
  @Schema(description = "租户 ID")
  @DiffField
  protected Long tenantId;
}
