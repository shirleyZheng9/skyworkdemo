package com.iwhalecloud.bote.mapper.oauth;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.iwhalecloud.bote.dto.oauth.OAuth2ClientRedirectUriDTO;

/**
 * OAuth2客户端重定向URI Mapper
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
public interface OAuth2ClientRedirectUriMapper {

  /**
   * 根据客户端ID查询重定向URI列表
   *
   * @param clientId 客户端ID
   * @return 重定向URI列表
   */
  List<OAuth2ClientRedirectUriDTO> selectByClientId(@Param("clientId") Long clientId);



  /**
   * 根据重定向URI查询客户端信息
   *
   * @param redirectUri 重定向URI
   * @return 重定向URI信息
   */
  OAuth2ClientRedirectUriDTO selectByRedirectUri(@Param("redirectUri") String redirectUri);

  /**
   * 批量插入重定向URI
   *
   * @param redirectUris 重定向URI列表
   * @return 影响行数
   */
  int batchInsertOAuth2ClientRedirectUri(@Param("list") List<OAuth2ClientRedirectUriDTO> redirectUris);

  /**
   * 根据客户端ID删除重定向URI
   *
   * @param clientId 客户端ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteByClientId(@Param("clientId") Long clientId, @Param("updatorId") Long updatorId);

  /**
   * 根据ID删除重定向URI
   *
   * @param id 重定向URI ID
   * @param updatorId 更新人ID
   * @return 影响行数
   */
  int deleteById(@Param("id") Long id, @Param("updatorId") Long updatorId);

  /**
   * 更新重定向URI
   *
   * @param redirectUri 重定向URI信息
   * @return 影响行数
   */
  int updateOAuth2ClientRedirectUri(OAuth2ClientRedirectUriDTO redirectUri);

  /**
   * 插入重定向URI
   *
   * @param redirectUri 重定向URI信息
   * @return 影响行数
   */
  int insertOAuth2ClientRedirectUri(OAuth2ClientRedirectUriDTO redirectUri);
}
