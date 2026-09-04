package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：OAuth2客户端
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Component
public final class OAuth2ClientDifferencePersistence extends BaseRootPersistence<OAuth2ClientDTO> {

    public OAuth2ClientDifferencePersistence(OAuth2ClientManageMapper oauth2ClientManageMapper) {
        setAddConsumer(oauth2ClientManageMapper::insertOAuth2Client);
        setBatchAddConsumer(oauth2ClientManageMapper::batchInsertOAuth2Client);
        setModifyConsumer(oauth2ClientManageMapper::updateOAuth2Client);
    }
}
