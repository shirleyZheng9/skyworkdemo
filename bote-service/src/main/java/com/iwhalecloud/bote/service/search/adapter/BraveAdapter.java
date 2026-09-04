package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.dto.search.params.BraveParamsDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 联网搜索适配器 - Brave
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Component
public class BraveAdapter implements IWebSearchAdapter {

  private static final Logger logger = LoggerFactory.getLogger(BraveAdapter.class);

  @Override
  public String getStrategyType() {
    return BaseConsts.WEB_SEARCH_BRAVE;
  }

  @Override
  public ResultVO<List<WebPageInfo>> search(String query) {
    // 从数据库获取参数配置
    String params = SystemParameter.WEB_SEARCH_BRAVE_PARAMS.getValueFromDb();
    BraveParamsDTO braveParams = null;
    if (StringUtils.isNotBlank(params)) {
      braveParams = JsonUtil.parseJson(params, BraveParamsDTO.class);
    }
    if (braveParams == null) {
      return ResultVO.fail("联网搜索：Brave 参数配置解析失败，请联系系统管理员");
    }
    if (StringUtils.isAnyEmpty(braveParams.getApiKey(), braveParams.getUrl())) {
      return ResultVO.fail("联网搜索：Brave API Key 或 URL 配置为空，请联系系统管理员");
    }

    try {
      // 构建请求 URL
      String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
      URI uri = URI.create(braveParams.getUrl() + "?q=" + encodedQuery + "&count=" + braveParams.getCount());
      
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      headers.set("X-Subscription-Token", braveParams.getApiKey());
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<String> responseEntity = HttpUtil.getRestTemplate()
        .exchange(uri, HttpMethod.GET, entity, String.class);
      
      String responseBody = responseEntity.getBody();
      if (!responseEntity.getStatusCode().is2xxSuccessful()) {
        return ResultVO.fail("调用 Brave API 返回错误：" + responseEntity.getStatusCode());
      }

      // 解析响应
      List<WebPageInfo> webPageInfos = parseBraveResponse(responseBody);
      if (webPageInfos.isEmpty()) {
        return ResultVO.fail("联网搜索内容为空");
      }
      return ResultVO.success(webPageInfos);
    }
    catch (Exception e) {
      logger.error("Brave 联网搜索请求失败：query={}", query, e);
      return ResultVO.fail("联网搜索请求失败：" + e.getMessage());
    }
  }

  /**
   * 解析 Brave 搜索结果
   * @param responseBody 响应体
   * @return WebPageInfo 列表
   */
  @SuppressWarnings("unchecked")
  private List<WebPageInfo> parseBraveResponse(String responseBody) {
    List<WebPageInfo> results = new ArrayList<>();
    if (StringUtils.isBlank(responseBody)) {
      return results;
    }
    
    try {
      Map<String, Object> body = JsonUtil.parseJson(responseBody, Map.class);
      if (body == null) {
        return results;
      }
      extractBraveResults(body, results);
    }
    catch (Exception e) {
      logger.error("解析 Brave 响应失败", e);
    }
    return results;
  }

  /**
   * 从 Brave 响应中提取结果
   */
  private void extractBraveResults(Map<String, Object> body, List<WebPageInfo> results) {
    Object web = body.get("web");
    if (!(web instanceof Map<?, ?> webMap)) {
      return;
    }
    Object res = webMap.get("results");
    if (!(res instanceof List<?> list)) {
      return;
    }
    for (Object item : list) {
      if (item instanceof Map<?, ?> m) {
        WebPageInfo info = new WebPageInfo();
        info.setName(String.valueOf(m.get("title")));
        info.setUrl(String.valueOf(m.get("url")));
        info.setSnippet(String.valueOf(m.get("description")));
        results.add(info);
      }
    }
  }
}
