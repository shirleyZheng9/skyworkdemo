package com.iwhalecloud.bote.dto.oauth;

import com.iwhalecloud.bote.entity.oauth.OAuth2ClientRedirectUriEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2客户端重定向URI DTO
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "OAuth2客户端重定向URI")
public class OAuth2ClientRedirectUriDTO extends OAuth2ClientRedirectUriEntity {
} 