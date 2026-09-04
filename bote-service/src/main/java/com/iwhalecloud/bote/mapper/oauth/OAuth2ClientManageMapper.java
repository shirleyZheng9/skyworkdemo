package com.iwhalecloud.bote.mapper.oauth;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * OAuth2客户端管理Mapper
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
public interface OAuth2ClientManageMapper {

    /**
     * 校验OAuth2客户端的编码唯一性
     *
     * @param oauth2Client OAuth2客户端
     * @return 结果
     */
    boolean existsOAuth2ClientCode(@Param("dto") OAuth2ClientDTO oauth2Client);

    /**
     * 根据主键获取OAuth2客户端
     *
     * @param clientId OAuth2客户端主键
     * @return OAuth2客户端
     */
    OAuth2ClientDTO getOAuth2Client(@Param("id") Long clientId);

    /**
     * 根据客户端标识获取OAuth2客户端
     *
     * @param clientCode 客户端标识
     * @return OAuth2客户端
     */
    OAuth2ClientDTO getOAuth2ClientByCode(@Param("clientCode") String clientCode);

    /**
     * 根据客户端标识和密钥获取OAuth2客户端
     *
     * @param clientCode     客户端标识
     * @param clientSecret   客户端密钥
     * @return OAuth2客户端
     */
    OAuth2ClientDTO getOAuth2ClientByCodeAndSecret(@Param("clientCode") String clientCode, @Param("clientSecret") String clientSecret);

    /**
     * 新增OAuth2客户端
     *
     * @param oauth2Client OAuth2客户端
     * @return 结果
     */
    int insertOAuth2Client(@Param("dto") OAuth2ClientDTO oauth2Client);

    /**
     * 批量新增OAuth2客户端
     *
     * @param oauth2Clients OAuth2客户端列表
     * @return 结果
     */
    int batchInsertOAuth2Client(@Param("list") List<OAuth2ClientDTO> oauth2Clients);

    /**
     * 修改OAuth2客户端
     *
     * @param oauth2Client OAuth2客户端
     * @return 结果
     */
    int updateOAuth2Client(@Param("dto") OAuth2ClientDTO oauth2Client);

    /**
     * 删除OAuth2客户端
     *
     * @param clientId 主键 ID
     * @param updatorId 操作人 ID
     * @return 结果
     */
    int deleteOAuth2Client(@Param("clientId") Long clientId, @Param("updatorId") Long updatorId);

    /**
     * 获取OAuth2客户端列表
     *
     * @param queryParams 查询条件
     * @return OAuth2客户端列表
     */
    List<OAuth2ClientDTO> selectOAuth2ClientList(@Param("query") OAuth2ClientQueryParams queryParams);

    /**
     * 获取OAuth2客户端列表（分页）
     *
     * @param queryParams 查询条件
     * @param rowBounds 分页参数
     * @return OAuth2客户端分页列表
     */
    Page<OAuth2ClientDTO> selectOAuth2ClientPage(@Param("query") OAuth2ClientQueryParams queryParams, RowBounds rowBounds);

}
