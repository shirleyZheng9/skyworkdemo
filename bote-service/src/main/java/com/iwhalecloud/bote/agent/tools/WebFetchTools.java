package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.doc.module.crawl.CrawlStepFactory;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 网页抓取工具。
 * <p>
 * 基于 {@link CrawlStepFactory#fetchMarkdownContent(String)}，对指定 URL 进行爬取与清洗，
 * 返回结构化的 Markdown 正文及元信息。适用于文章、文档等网页正文抓取。
 * 不适用于源码/仓库类链接（如 github.com、gitee.com），推理时应由模型选择其他工具，不在本类内做 URL 过滤。
 * </p>
 *
 * @author chen.linfa
 * @since 2026-03-12
 */
public final class WebFetchTools {

  private WebFetchTools() {
  }

  @Tool(
    name = "web_fetch",
    description = "Fetch a web page by URL and return cleaned markdown content. Use ONLY for article or documentation pages (e.g. blog posts, docs, news). Do NOT use for source code or repository URLs (github.com, gitee.com, gitlab.com, bitbucket.org)—for those do not call web_fetch; use code or repository tools instead. Parameters: url (required)."
  )
  public static Map<String, Object> webFetch(@ToolParam(description = "URL of the page to fetch") String url) {
    Assert.hasLength(url, "Error: url is required");
    try {
      ResultVO<CrawlResult> result = CrawlStepFactory.fetchMarkdownContent(url.trim());
      if (!result.isSuccess()) {
        throw new ToolExecutionException("Error: " + result.getResultMsg());
      }
      CrawlResult crawlResult = result.getResultObject();
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("url", url);
      out.put("title", crawlResult.getTitle());
      out.put("markdown", crawlResult.getMarkdown());
      return out;
    }
    catch (IllegalArgumentException e) {
      // 来自断言的参数校验异常（例如 URL 不合法）
      throw new ToolExecutionException("Error: Invalid URL: " + e.getMessage(), e);
    }
    catch (ToolExecutionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ToolExecutionException("Error fetching url: " + e.getMessage(), e);
    }
  }
}

