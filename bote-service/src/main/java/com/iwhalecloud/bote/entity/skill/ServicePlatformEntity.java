package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：API 平台 Entity
 *
 * @author auto
 * @since 2024-09-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_service_platform")
public class ServicePlatformEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long platformId;
  @DiffField(name = "PLATFORM_CODE")
  @Schema(description = "平台编码")
  @Size(max = 50, message = "平台编码超过限定长度50")
  private String platformCode;
  @DiffField(name = "PLATFORM_NAME")
  @Schema(description = "平台名称")
  @Size(max = 20, message = "平台名称超过限定长度20")
  private String platformName;
  @DiffField(name = "ACC_TYPE")
  @Schema(description = "鉴权类型")
  private String accType;
  @DiffField(name = "ACC_TOKEN")
  @Schema(description = "鉴权码")
  private String accToken;
  @DiffField(name = "ACC_TOKEN_EXP_DATE")
  @Schema(description = "鉴权码失效时间")
  private Date accTokenExpDate;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
