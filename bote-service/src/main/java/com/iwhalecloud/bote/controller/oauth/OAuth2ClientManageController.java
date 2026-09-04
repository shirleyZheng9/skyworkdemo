package com.iwhalecloud.bote.controller.oauth;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientDTO;
import com.iwhalecloud.bote.dto.oauth.OAuth2ClientQueryParams;
import com.iwhalecloud.bote.entity.oauth.OAuth2ClientEntity;
import com.iwhalecloud.bote.service.oauth.IOAuth2ClientManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2客户端管理 controller
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@RestController
@RequestMapping(value = BaseConsts.API_PREFIX + "oauth2client", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "OAuth2客户端管理")
public class OAuth2ClientManageController {

  private final IOAuth2ClientManageService oauth2ClientManageService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存OAuth2客户端")
  @PostMapping("saveOAuth2Client")
  public ResultVO<OAuth2ClientDTO> saveOAuth2Client(@RequestBody OAuth2ClientDTO oauth2Client) {
    ResultVO<OAuth2ClientDTO> oAuth2ClientDTOResultVO = oauth2ClientManageService.saveOAuth2Client(oauth2Client);
    if (oAuth2ClientDTOResultVO.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_OAUTH2CLIENT, oauth2Client.getClientCode());
    }
    return oAuth2ClientDTOResultVO;
  }

  @Operation(summary = "删除OAuth2客户端")
  @PostMapping("deleteOAuth2Client")
  public ResultVO<Boolean> deleteOAuth2Client(@RequestParam(name = "clientId") Long clientId) {
    // 验证客户端是否存在
    OAuth2ClientEntity existingClient = oauth2ClientManageService.findOAuth2Client(clientId);
    if (existingClient == null) {
      throw new BssException("客户端不存在: " + clientId);
    }
    Boolean delete = oauth2ClientManageService.deleteOAuth2Client(clientId);
    if (delete) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_OAUTH2CLIENT, existingClient.getClientCode());
    }
    return ResultVO.success(delete);
  }

  @Operation(summary = "查询OAuth2客户端列表")
  @PostMapping("queryOAuth2ClientList")
  public ResultVO<List<OAuth2ClientDTO>> queryOAuth2ClientList(@RequestBody OAuth2ClientQueryParams queryParams) {
    return ResultVO.success(oauth2ClientManageService.queryOAuth2ClientList(queryParams));
  }

  @Operation(summary = "查询OAuth2客户端列表（分页）")
  @PostMapping("queryOAuth2ClientPage")
  public ResultVO<PageInfo<OAuth2ClientDTO>> queryOAuth2ClientPage(@RequestBody OAuth2ClientQueryParams queryParams) {
    return ResultVO.success(oauth2ClientManageService.queryOAuth2ClientPage(queryParams));
  }

  @Operation(summary = "启用/禁用OAuth2客户端")
  @PostMapping("toggleOAuth2ClientStatus")
  public ResultVO<Void> toggleOAuth2ClientStatus(@RequestParam(name = "clientId") Long clientId,
                                                 @RequestParam(name = "enabled") String enabled) {
    return oauth2ClientManageService.toggleOAuth2ClientStatus(clientId, enabled);
  }

  @Operation(summary = "重置OAuth2客户端密钥")
  @PostMapping("resetOAuth2ClientSecret")
  public ResultVO<String> resetOAuth2ClientSecret(@RequestParam(name = "clientId") Long clientId) {
    return oauth2ClientManageService.resetOAuth2ClientSecret(clientId);
  }
}
