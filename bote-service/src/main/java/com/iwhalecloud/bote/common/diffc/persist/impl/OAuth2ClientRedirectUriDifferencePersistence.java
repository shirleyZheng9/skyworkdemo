package com.iwhalecloud.bote.common.diffc.persist.impl;

import org.springframework.stereotype.Component;

import com.iwhalecloud.bote.dto.oauth.OAuth2ClientRedirectUriDTO;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientRedirectUriMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;

/**
 * 数据差异保存服务：OAuth2客户端 授权URL
 *
 * @author zhao.xu104
 * @since 2025-07-09
 */
@Component
public final class OAuth2ClientRedirectUriDifferencePersistence extends BaseRootPersistence<OAuth2ClientRedirectUriDTO> {

  public OAuth2ClientRedirectUriDifferencePersistence(OAuth2ClientRedirectUriMapper oauth2ClientManageMapper) {
    setAddConsumer(oauth2ClientManageMapper::insertOAuth2ClientRedirectUri);
    setBatchAddConsumer(oauth2ClientManageMapper::batchInsertOAuth2ClientRedirectUri);
    setModifyConsumer(oauth2ClientManageMapper::updateOAuth2ClientRedirectUri);
  }
}
