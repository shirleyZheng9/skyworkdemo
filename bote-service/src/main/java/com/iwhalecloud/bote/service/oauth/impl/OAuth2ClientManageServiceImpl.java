package com.iwhalecloud.bote.service.oauth.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientQueryParams;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientRedirectUriDTO;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientManageMapper;
import com.iwhalecloud.bote.mapper.oauth.OAuth2ClientRedirectUriMapper;
import com.iwhalecloud.bote.service.oauth.IOAuth2ClientManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.DataDifferenceStarter;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2客户端管理服务实现
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Service
@RequiredArgsConstructor
public class OAuth2ClientManageServiceImpl implements IOAuth2ClientManageService {

  private final OAuth2ClientManageMapper oauth2ClientManageMapper;
  private final OAuth2ClientRedirectUriMapper oauth2ClientRedirectUriMapper;

  @Override
  @Nullable
  public OAuth2ClientDTO findOAuth2Client(Long clientId) {
    OAuth2ClientDTO dto = oauth2ClientManageMapper.getOAuth2Client(clientId);
    if (dto == null) {
      return null;
    }
    // 查询关联的重定向URI列表
    List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(clientId);
    dto.setRedirectUris(redirectUris);
    return dto;
  }

  @Override
  @Nullable
  public OAuth2ClientDTO findOAuth2ClientByCode(String clientCode) {
    OAuth2ClientDTO dto = oauth2ClientManageMapper.getOAuth2ClientByCode(clientCode);
    if (dto == null) {
      return null;
    }
    // 查询关联的重定向URI列表
    List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(dto.getClientId());
    dto.setRedirectUris(redirectUris);
    return dto;
  }

  @Override
  @Nullable
  public OAuth2ClientDTO findOAuth2ClientByCodeAndSecret(String clientCode, String clientSecret) {
    OAuth2ClientDTO dto = oauth2ClientManageMapper.getOAuth2ClientByCodeAndSecret(clientCode, clientSecret);
    if (dto == null) {
      return null;
    }
    // 查询关联的重定向URI列表
    List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(dto.getClientId());
    dto.setRedirectUris(redirectUris);
    return dto;
  }

  @Override
  @Transactional
  public ResultVO<OAuth2ClientDTO> saveOAuth2Client(OAuth2ClientDTO oauth2Client) {
    OAuth2ClientDTO old = oauth2Client.getClientId() == null ? null : findOAuth2Client(oauth2Client.getClientId());
    //处理新增数据
    if (oauth2Client.getClientId() == null) {
      // 校验编码唯一性
      if (oauth2ClientManageMapper.existsOAuth2ClientCode(oauth2Client)) {
        throw new BssException("客户端已存在");
      }
      oauth2Client.setClientId(Sequences.OAUTH2_CLIENT_ID.next());
      oauth2Client.setCreatorId(SessionUtil.getLoginInfo().getUserId());
      oauth2Client.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
      oauth2Client.setCreatedTime(new Date());
      oauth2Client.setStatusTime(new Date());
      // 关联ClientId
      if (oauth2Client.getRedirectUris() != null && !oauth2Client.getRedirectUris().isEmpty()) {
        oauth2Client.getRedirectUris().forEach(redirectUri -> {
            redirectUri.setClientId(oauth2Client.getClientId());
            redirectUri.setCreatorId(SessionUtil.getLoginInfo().getUserId());
            redirectUri.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
            redirectUri.setCreatedTime(new Date());
            redirectUri.setStatusTime(new Date());
          }
        );
      }
    }
    DataDifference<OAuth2ClientDTO> difference = DataDifferenceStarter.computeSave(old, oauth2Client, false, true);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public List<OAuth2ClientDTO> queryOAuth2ClientList(OAuth2ClientQueryParams queryParams) {
    List<OAuth2ClientDTO> clients = oauth2ClientManageMapper.selectOAuth2ClientList(queryParams);
    // 为每个客户端加载重定向URI列表
    for (OAuth2ClientDTO client : clients) {
      List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(client.getClientId());
      client.setRedirectUris(redirectUris);
    }
    return clients;
  }

  @Override
  public PageInfo<OAuth2ClientDTO> queryOAuth2ClientPage(OAuth2ClientQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    PageInfo<OAuth2ClientDTO> pageInfo = oauth2ClientManageMapper.selectOAuth2ClientPage(queryParams, rowBounds).toPageInfo();
    // 为每个客户端加载重定向URI列表
    for (OAuth2ClientDTO client : pageInfo.getList()) {
      List<OAuth2ClientRedirectUriDTO> redirectUris = oauth2ClientRedirectUriMapper.selectByClientId(client.getClientId());
      client.setRedirectUris(redirectUris);
    }
    return pageInfo;
  }

  @Override
  @Transactional
  public ResultVO<Void> toggleOAuth2ClientStatus(Long clientId, String enabled) {
    Assert.notNull(clientId, "客户端ID不能为空");
    OAuth2ClientDTO oauth2Client = findOAuth2Client(clientId);
    if (oauth2Client == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(clientId);
    }
    oauth2Client.setEnabled(enabled);
    oauth2Client.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    oauth2ClientManageMapper.updateOAuth2Client(oauth2Client);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<String> resetOAuth2ClientSecret(Long clientId) {
    Assert.notNull(clientId, "客户端ID不能为空");
    OAuth2ClientDTO oauth2Client = findOAuth2Client(clientId);
    if (oauth2Client == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(clientId);
    }
    // 生成新的客户端密钥
    String newSecret = generateClientSecret();
    oauth2Client.setClientSecret(newSecret);
    oauth2Client.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    oauth2ClientManageMapper.updateOAuth2Client(oauth2Client);
    return ResultVO.success(newSecret);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteOAuth2Client(Long clientId) {
    // 删除关联的重定向URI
    oauth2ClientRedirectUriMapper.deleteByClientId(clientId, SessionUtil.getLoginInfo().getUserId());
    // 删除数据库记录
    int result = oauth2ClientManageMapper.deleteOAuth2Client(clientId, SessionUtil.getLoginInfo().getUserId());
    return result > 0;
  }


  /**
   * 生成客户端密钥
   *
   * @return 客户端密钥
   */
  public String generateClientSecret() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
