package com.iwhalecloud.bote.common.util;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppAggregateInfoDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppVersionDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAttrSpecDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpGatewayDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpPageInstDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpStandardServiceDTO;
import com.iwhalecloud.bote.dto.lcdp.query.LcdpQueryParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 灵犀API辅助类
 *
 * @author qian.sisheng
 * @since 2025-06-04
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class LcdpApiUtil {

  private static final Logger logger = LoggerFactory.getLogger(LcdpApiUtil.class);
  /** 灵犀平台API鉴权：系统编码 */
  private static final String LCDP_SYSTEM_CODE = "LCDP-CSRF-TOKEN";
  /** 灵犀平台API鉴权：令牌签名 */
  private static final String LCDP_SYSTEM_SECRET = "LCDP-SIGNATURE";
  /** 查询网关 */
  private static final String SELECT_GATEWAY_PATH = "/lcdp/platform/findAllPlatformModuleConfig";
  /** 查询应用版本 */
  private static final String SELECT_APP_VERSION_PATH  = "/lcdp/application/qryAppVersionListByMainAppId";
  /**查询页面实例列表(分页) */
  private static final String SELECT_PAGE_INST_PATH = "/lcdp/pageConfig/qryPageInstPage";
  /** 查询标准服务列表 */
  private static final String SELECT_STANDARD_SERVICE_PATH = "/lcdp/manager/standardService/qryStandardServicePage";
  /** 查询应用聚合信息 */
  private static final String SELECT_APP_AGGREGATE_PATH = "/lcdp/application/queryAppAggregateInfo";
  /** 获取静态数据列表（分页） */
  private static final String SELECT_ATTR_SPEC_PATH = "/lcdp/attr/qryAttrSpecPage";

  private LcdpApiUtil() {
  }

  /**
   * 查询静态数据列表（分页）
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  public static PageInfo<LcdpAttrSpecDTO> queryAttrSpecPage(LcdpQueryParams queryParams) {
    String url = getLcdpGatewayUrl() + SELECT_ATTR_SPEC_PATH;
    URI uri = buildUrl(null, null, url);
    return executeRequest(uri, HttpMethod.POST, queryParams, new ParameterizedTypeReference<ResultVO<PageInfo<LcdpAttrSpecDTO>>>() {
    }, queryParams.getAppId());
  }

  /**
   * 查询应用聚合信息(sql、页面、编排服务、平台服务、静态数据、对象详情)
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  public static LcdpAppAggregateInfoDTO queryAppAggregateInfo(LcdpQueryParams queryParams) {
    String url = getLcdpGatewayUrl() + SELECT_APP_AGGREGATE_PATH;
    URI uri = buildUrl(null, null, url);
    return executeRequest(uri, HttpMethod.POST, queryParams, new ParameterizedTypeReference<ResultVO<LcdpAppAggregateInfoDTO>>() {
    }, queryParams.getAppId());
  }

  /**
   * 查询网关列表
   *
   * @param tenantId 租户ID
   * @return 网关列表
   */
  public static List<LcdpGatewayDTO> queryGatewayList(Long tenantId) {
    String url = getLcdpGatewayUrl() + SELECT_GATEWAY_PATH;
    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put("tenantId", tenantId);
    URI uri = buildUrl(queryParams, null, url);
    return executeRequest(uri, HttpMethod.GET, null, new ParameterizedTypeReference<ResultVO<List<LcdpGatewayDTO>>>() {
    }, null);
  }

  /**
   * 查询编排服务列表
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  public static PageInfo<LcdpStandardServiceDTO> queryStandardServicePage(LcdpQueryParams queryParams) {
    String url = getLcdpGatewayUrl() + SELECT_STANDARD_SERVICE_PATH;
    URI uri = buildUrl(null, null, url);
    return executeRequest(uri, HttpMethod.POST, queryParams, new ParameterizedTypeReference<ResultVO<PageInfo<LcdpStandardServiceDTO>>>() {
    }, queryParams.getAppId());
  }

  /**
   * 查询页面实例列表
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  public static PageInfo<LcdpPageInstDTO> queryPageInstPage(LcdpQueryParams queryParams) {
    String url = getLcdpGatewayUrl() + SELECT_PAGE_INST_PATH;
    URI uri = buildUrl(null, null, url);
    return executeRequest(uri, HttpMethod.POST, queryParams, new ParameterizedTypeReference<ResultVO<PageInfo<LcdpPageInstDTO>>>() {
    }, queryParams.getAppId());
  }

  /**
   * 查询应用版本列表
   *
   * @param appId 应用ID
   * @return 应用版本列表
   */
  public static List<LcdpAppVersionDTO> queryAppVersionList(Long appId) {
    String url = getLcdpGatewayUrl() + SELECT_APP_VERSION_PATH;
    Map<String, Object> params = new HashMap<>();
    params.put("appId", appId);
    URI uri = buildUrl(params, null, url);
    return executeRequest(uri, HttpMethod.GET, null, new ParameterizedTypeReference<ResultVO<List<LcdpAppVersionDTO>>>() {
    }, appId);
  }

  private static <T> T executeRequest(URI url, HttpMethod method, @Nullable Object body, ParameterizedTypeReference<ResultVO<T>> typeReference,
                                      @Nullable Long appId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(LCDP_SYSTEM_CODE, SystemParameter.LCDP_AUTH_SYSTEM_CODE.getValueFromDb());
    headers.set(LCDP_SYSTEM_SECRET, SystemParameter.LCDP_AUTH_SYSTEM_SECRET.getValueFromDb());
    if (appId != null) {
      headers.set("APP-ID", String.valueOf(appId));
    }
    ResponseEntity<ResultVO<T>> responseEntity;
    try {
      HttpEntity<?> requestEntity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
      logger.info("Execute lcdp api request: url={}, method={}, body={}", url, method, body);
      responseEntity = HttpUtil.getRestTemplate().exchange(url, method, requestEntity, typeReference);
      logger.info("Execute lcdp api response: url={}, method={}, response={}", url, method, responseEntity);
      ResultVO<T> result = responseEntity.getBody();
      if (result == null) {
        throw new BssException("灵犀平台请求异常：响应为空");
      }
      if (!result.isSuccess()) {
        throw new BssException(result.getResultMsg());
      }
      return result.getResultObject();
    }
    catch (Exception e) {
      logger.error("Failed to execute request, message ={}", e.getMessage(), e);
      throw new BssException("灵犀平台请求异常：" + e.getMessage(), e);
    }
  }

  /**
   * 构建url
   *
   * @param queryParams 查询参数
   * @param pathParams 路径参数
   * @param url 请求地址
   * @return URI
   */
  private static URI buildUrl(@Nullable Map<String, Object> queryParams, @Nullable Map<String, Object> pathParams, String url) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
    if (MapUtils.isNotEmpty(queryParams)) {
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        Object value = entry.getValue();
        if (value == null) {
          continue;
        }
        if (value instanceof Collection) {
          builder.queryParam(entry.getKey(), (Collection<?>) value);
        }
        else {
          builder.queryParam(entry.getKey(), value);
        }
      }
    }
    if (MapUtils.isNotEmpty(pathParams)) {
      return builder.build(pathParams);
    }
    return builder.build().toUri();
  }

  private static String getLcdpGatewayUrl() {
    String url = SystemParameter.LCDP_GATEWAY_URL.getValueFromDb();
    if (StringUtils.isBlank(url)) {
      throw new BssException("未配置灵犀平台网关地址: LCDP_GATEWAY_URL");
    }
    return StringUtils.stripEnd(url, "/");
  }
}
