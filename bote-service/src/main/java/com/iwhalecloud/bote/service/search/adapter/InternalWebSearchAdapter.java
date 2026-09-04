package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.service.search.internal.core.SearchResultContainer;
import com.iwhalecloud.bote.service.search.internal.core.SearchService;
import com.iwhalecloud.bote.service.search.internal.model.SearchResult;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SearXNG 内部搜索引擎适配器
 *
 * @author zheng.ruixiang
 * @since 2026-04-13
 */
@Component
public class InternalWebSearchAdapter implements IWebSearchAdapter {
  private static final Logger logger = LoggerFactory.getLogger(InternalWebSearchAdapter.class);
  private final SearchService searchService;

  public InternalWebSearchAdapter() {
    this.searchService = new SearchService();
  }

  @Override
  public ResultVO<List<WebPageInfo>> search(String query) {
    try {
      SearchResultContainer container = searchService.search(query);

      List<WebPageInfo> results = container.getResults().stream()
        .map(this::convertToWebPageInfo)
        .collect(Collectors.toList());

      if (results.isEmpty()) {
        return ResultVO.fail("搜索结果为空");
      }
      return ResultVO.success(results);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("web search failed: {}", e.getMessage(), e);
      }
      return ResultVO.fail("搜索失败: " + e.getMessage());
    }
  }

  @Override
  public String getStrategyType() {
    return BaseConsts.WEB_SEARCH_INTERNAL;
  }

  public WebPageInfo convertToWebPageInfo(SearchResult result) {
    WebPageInfo info = new WebPageInfo();
    info.setName(result.getTitle());
    info.setUrl(result.getUrl());
    info.setSnippet(result.getContent());
    info.setSummary(result.getContent());

    if (result.getPublishedDate() != null) {
      info.setDateLastCrawled(Date.from(result.getPublishedDate()));
    }

    return info;
  }
}
