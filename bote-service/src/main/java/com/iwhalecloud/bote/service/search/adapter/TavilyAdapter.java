package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.dto.search.params.TavilyParamsDTO;
import com.iwhalecloud.bote.dto.search.request.TavilySearchRequest;
import com.iwhalecloud.bote.dto.search.response.TavilySearchResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 联网搜索适配器 - Tavily
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Component
public class TavilyAdapter implements IWebSearchAdapter {

  private static final Logger logger = LoggerFactory.getLogger(TavilyAdapter.class);

  @Override
  public String getStrategyType() {
    return BaseConsts.WEB_SEARCH_TAVILY;
  }

  @Override
  public ResultVO<List<WebPageInfo>> search(String query) {
    String params = SystemParameter.WEB_SEARCH_TAVILY_PARAMS.getValueFromDb();
    if (StringUtils.isEmpty(params)) {
      return ResultVO.fail("联网搜索：Tavily 参数配置为空，请联系系统管理员");
    }
    TavilyParamsDTO tavilyParams = JsonUtil.parseJson(params, TavilyParamsDTO.class);
    if (tavilyParams == null) {
      return ResultVO.fail("联网搜索：Tavily 参数配置解析失败，请联系系统管理员");
    }
    if (StringUtils.isAnyEmpty(tavilyParams.getApiKey(), tavilyParams.getUrl())) {
      return ResultVO.fail("联网搜索：Tavily API Key 或 URL 配置为空，请联系系统管理员");
    }
    try {
      // 构建请求参数
      TavilySearchRequest request = new TavilySearchRequest();
      request.setApiKey(tavilyParams.getApiKey());
      request.setQuery(query);
      request.setSearchDepth(tavilyParams.getSearchDepth());
      request.setMaxResults(tavilyParams.getMaxResults());
      request.setIncludeAnswer(tavilyParams.getIncludeAnswer());
      request.setIncludeImages(tavilyParams.getIncludeImages());

      // 构建 HTTP 请求
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + tavilyParams.getApiKey());
      HttpEntity<TavilySearchRequest> requestEntity = new HttpEntity<>(request, headers);

      ResponseEntity<TavilySearchResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(tavilyParams.getUrl(), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<TavilySearchResponse>() {
        });

      // 解析响应
      TavilySearchResponse res = responseEntity.getBody();
      if (res != null) {
        if (!res.isSuccess()) {
          return ResultVO.fail(res.getErrorMessage());
        }
        // 转换结果为 WebPageInfo 列表
        List<WebPageInfo> webPageInfos = new ArrayList<>();
        if (res.getResults() != null) {
          res.getResults().forEach(result -> webPageInfos.add(result.toWebPageInfo()));
        }
        return ResultVO.success(webPageInfos);
      }
      return ResultVO.fail("联网搜索内容为空");
    }
    catch (Exception e) {
      logger.error("Tavily 联网搜索请求失败：query={}", query, e);
      return ResultVO.fail("联网搜索请求失败：" + e.getMessage());
    }
  }
}
