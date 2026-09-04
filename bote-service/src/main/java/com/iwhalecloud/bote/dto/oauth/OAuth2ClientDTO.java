package com.iwhalecloud.bote.dto.oauth;

import com.iwhalecloud.bote.entity.oauth.OAuth2ClientEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * OAuth2客户端 DTO
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "OAuth2客户端")
public class OAuth2ClientDTO extends OAuth2ClientEntity {

  @Schema(description = "重定向URI列表")
  @DiffField(childNode = true)
  private List<OAuth2ClientRedirectUriDTO> redirectUris;

}
