package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TokenUtil;
import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.dto.base.ApiDTO;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.dto.base.CatalogTree;
import com.iwhalecloud.bote.dto.base.query.ApiAuthQueryParams;
import com.iwhalecloud.bote.mapper.base.ApiAuthManageMapper;
import com.iwhalecloud.bote.service.base.IApiAuthManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * API 鉴权管理服务实现
 *
 * @author auto
 * @since 2024-09-19
 */
@Service
@RequiredArgsConstructor
public class ApiAuthManageServiceImpl implements IApiAuthManageService {

  private final ApiAuthManageMapper apiAuthManageMapper;

  @Override
  @Nullable
  public ApiAuthDTO getApiAuth(Long tenantId, Long authId) {
    return apiAuthManageMapper.getApiAuth(tenantId, authId);
  }

  @Override
  @Transactional
  public ResultVO<ApiAuthDTO> saveApiAuth(ApiAuthDTO apiAuth) {
    apiAuth.setStatusCd(BaseConsts.STATUS_CD_VALID);
    ApiAuthDTO old = apiAuth.getAuthId() == null ? null : getApiAuth(apiAuth.getTenantId(), apiAuth.getAuthId());
    if (old == null) {
      apiAuth.setSignature(UUIDUtils.randomFormatUuid());
    }
    DataDifference<ApiAuthDTO> difference = DataDifferenceStarter.computeSave(old, apiAuth, false, apiAuth.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteApiAuth(Long tenantId, Long authId) {
    apiAuthManageMapper.deleteApiAuth(tenantId, authId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<ApiAuthDTO> queryApiAuthList(ApiAuthQueryParams queryParams) {
    return apiAuthManageMapper.selectApiAuthList(queryParams);
  }

  @Override
  public List<CatalogTree<ApiDTO>> selectApiList() {
    Map<Long, List<ApiDTO>> group = CollectionUtils.emptyIfNull(apiAuthManageMapper.selectApiList()).stream()
      .collect(Collectors.groupingBy(ApiDTO::getCatalogItemId));
    if (MapUtils.isEmpty(group)) {
      return Collections.emptyList();
    }
    List<CatalogTree<ApiDTO>> list = new ArrayList<>();
    for (Entry<Long, List<ApiDTO>> entry : group.entrySet()) {
      CatalogTree<ApiDTO> dto = new CatalogTree<>();
      dto.setId(entry.getValue().get(0).getCatalogItemId().toString());
      dto.setName(entry.getValue().get(0).getCatalogName());
      dto.setValues(entry.getValue());
      list.add(dto);
    }
    return list;
  }

  @Override
  @Transactional
  public ResultVO<AppPublishDTO> saveAppPublish(AppPublishDTO publish) {
    Assert.hasText(publish.getToken(), "密钥不能为空");
    // 获取博特api地址
    String boteApiUrl = SystemParameter.BOTE_API_URL.getValueFromEnv();
    Assert.hasText(boteApiUrl, "博特平台接口地址不能为空，请联系系统管理员");
    // 生成access_token
    String accessToken = TokenUtil.buildAccessToken(publish.getToken());
    // 重定向地址
    String redirect = getBotRedirect(publish);
    // 构造单点链接地址
    String url;
    String prefix = StringUtils.stripEnd(boteApiUrl, "/");
    if (publish.getBotId() != null) {
      url = String.format("%s/bote/single?tenantId=%s&ownerTenantId=%s&botId=%s&modeType=%s&accessToken=%s&redirect=%s", prefix, publish.getTenantId(),
        publish.getTenantId(), publish.getBotId(), publish.getModeType(), accessToken, redirect);
    }
    else if (StringUtils.isNotEmpty(publish.getBotIds())) {
      url = String.format("%s/bote/single?tenantId=%s&modeType=%s&botIds=%s&accessToken=%s&redirect=%s", prefix, publish.getTenantId(), publish.getModeType(),
        publish.getBotIds(), accessToken, redirect);
    }
    else {
      url = String.format("%s/bote/single?tenantId=%s&modeType=%s&accessToken=%s&redirect=%s", prefix, publish.getTenantId(), publish.getModeType(), accessToken, redirect);
    }
    if (BaseConsts.TRUE.equals(publish.getIsNewSession())) {
      url = url + "&pattern=S";
    }
    // 保存智能体发布信息
    publish.setUrl(url);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    publish.setPublishId(Sequences.APP_PUBLISH_ID.next());
    publish.setStatusCd(BaseConsts.STATUS_CD_VALID);
    publish.setCreatorId(userId);
    publish.setUpdatorId(userId);
    apiAuthManageMapper.insertAppPublish(publish);
    return ResultVO.success(publish);
  }

  /**
   * 获取应用重定向地址
   */
  private String getBotRedirect(AppPublishDTO publish) {
    // 获取博特平台前端地址
    String boteWebUrl = SystemParameter.BOTE_WEB_URL.getValueFromEnv();
    Assert.hasText(boteWebUrl, "博特平台前端地址不能为空，请联系系统管理员");
    // 提取前端地址路径
    UriComponents components = UriComponentsBuilder.fromUriString(boteWebUrl).build();
    String urlPath = components.getPath();
    if (StringUtils.isEmpty(urlPath) || "/".equals(urlPath)) {
      urlPath = "";
    }
    else {
      urlPath = StringUtils.stripEnd(urlPath, "/");
    }
    // 构造重定向地址
    String redirect = urlPath + (publish.getBotId() != null ? "/#/driver/bot" : "/#/driver");
    return URLEncoder.encode(redirect, StandardCharsets.UTF_8);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteAppPublish(Long publishId) {
    apiAuthManageMapper.deleteAppPublish(publishId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<AppPublishDTO> queryAppPublishList(Long tenantId, String botName, Long botId) {
    return apiAuthManageMapper.selectAppPublishList(tenantId, botName, botId);
  }
}
