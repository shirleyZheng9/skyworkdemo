package com.iwhalecloud.bote.entity.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2客户端重定向URI实体
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_OAUTH2_CLIENT_REDIRECT_URI")
@Schema(description = "OAuth2客户端重定向URI")
public class OAuth2ClientRedirectUriEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键ID")
  private Long id;

  @NotNull(message = "客户端ID不能为空")
  @DiffField(name = "CLIENT_ID")
  @Schema(description = "OAuth2客户端ID")
  private Long clientId;

  @NotBlank(message = "重定向URI不能为空")
  @DiffField(name = "REDIRECT_URI")
  @Schema(description = "重定向URI")
  private String redirectUri;

  @DiffField(name = "DESCRIPTION_INFO")
  @Schema(description = "URI描述")
  private String descriptionInfo;

}
