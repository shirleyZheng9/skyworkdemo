package com.iwhalecloud.bote.service.oauth;

import java.util.List;

import org.springframework.lang.Nullable;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * OAuth2客户端管理服务
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
public interface IOAuth2ClientManageService {

  /**
   * 查询单个OAuth2客户端
   *
   * @param clientId OAuth2客户端主键
   * @return OAuth2客户端
   */
  @Nullable
  OAuth2ClientDTO findOAuth2Client(Long clientId);

  /**
   * 根据客户端标识查询OAuth2客户端
   *
   * @param clientCode 客户端标识
   * @return OAuth2客户端
   */
  @Nullable
  OAuth2ClientDTO findOAuth2ClientByCode(String clientCode);

  /**
   * 根据客户端标识和密钥查询OAuth2客户端
   *
   * @param clientCode   客户端标识
   * @param clientSecret 客户端密钥
   * @return OAuth2客户端
   */
  @Nullable
  OAuth2ClientDTO findOAuth2ClientByCodeAndSecret(String clientCode, String clientSecret);

  /**
   * 保存OAuth2客户端
   *
   * @param oauth2Client OAuth2客户端
   * @return 结果
   */
  ResultVO<OAuth2ClientDTO> saveOAuth2Client(OAuth2ClientDTO oauth2Client);

  /**
   * 删除OAuth2客户端
   *
   * @param clientId OAuth2客户端主键
   * @return 结果
   */
  Boolean deleteOAuth2Client(Long clientId);

  /**
   * 查询OAuth2客户端列表
   *
   * @param queryParams 查询条件
   * @return OAuth2客户端列表
   */
  List<OAuth2ClientDTO> queryOAuth2ClientList(OAuth2ClientQueryParams queryParams);

  /**
   * 查询OAuth2客户端列表（分页）
   *
   * @param queryParams 查询条件
   * @return OAuth2客户端分页列表
   */
  PageInfo<OAuth2ClientDTO> queryOAuth2ClientPage(OAuth2ClientQueryParams queryParams);


  /**
   * 启用/禁用OAuth2客户端
   *
   * @param clientId OAuth2客户端主键
   * @param enabled  是否启用
   * @return 结果
   */
  ResultVO<Void> toggleOAuth2ClientStatus(Long clientId, String enabled);

  /**
   * 重置OAuth2客户端密钥
   *
   * @param clientId OAuth2客户端主键
   * @return 结果
   */
  ResultVO<String> resetOAuth2ClientSecret(Long clientId);

}
