package com.iwhalecloud.bote.cache;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientRedirectUriDTO;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientManageMapper;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientRedirectUriMapper;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;

/**
 * OAuth2客户端配置缓存
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Component
public class OAuth2ClientCache extends BaseSecondaryCache<OAuth2ClientDTO> {

  private final OAuth2ClientManageMapper oauth2ClientManageMapper;
  private final OAuth2ClientRedirectUriMapper oauth2ClientRedirectUriMapper;

  public OAuth2ClientCache(OAuth2ClientManageMapper oauth2ClientManageMapper, OAuth2ClientRedirectUriMapper oauth2ClientRedirectUriMapper) {
    super(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_OAUTH2CLIENT);
    this.oauth2ClientManageMapper = oauth2ClientManageMapper;
    this.oauth2ClientRedirectUriMapper = oauth2ClientRedirectUriMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_OAUTH2CLIENT;
  }

  @Nullable
  @Override
  protected OAuth2ClientDTO load(String clientCode) {
    if (StringUtils.isEmpty(clientCode)) {
      return null;
    }
    OAuth2ClientDTO oAuth2ClientByCode = oauth2ClientManageMapper.getOAuth2ClientByCode(clientCode);
    if (oAuth2ClientByCode != null) {
      // 查询关联的重定向URI列表
      List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(oAuth2ClientByCode.getClientId());
      oAuth2ClientByCode.setRedirectUris(redirectUris);
      return oAuth2ClientByCode;
    }
    return null;
  }
}
