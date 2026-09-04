package com.iwhalecloud.bote.entity.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * API 鉴权 Entity
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_api_auth")
public class ApiAuthEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long authId;
  @DiffField(name = "TOKEN_EXP_TIME")
  @Schema(description = "令牌失效时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date tokenExpTime;
  @DiffField(name = "SIGNATURE")
  @Schema(description = "密钥")
  private String signature;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
