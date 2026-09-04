package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainLoginHelper;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainDocumentWithContentDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map.Entry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * DocChain 接口代理
 *
 * <p>解决解决前端直接访问 DocChain 接口无法鉴权的问题。</p>
 *
 * @author bianjp
 * @since 2024-11-30
 */
@RestController
@RequestMapping(path = CommonConsts.API_PREFIX + "docchain/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库：DocChain 接口转发")
public class DocChainProxyController {
  private static final Logger logger = LoggerFactory.getLogger(DocChainProxyController.class);
  /** 透传的请求头 */
  private static final String[] PROXY_REQUEST_HEADERS = new String[]{
    HttpHeaders.ACCEPT,
    HttpHeaders.IF_MODIFIED_SINCE,
    HttpHeaders.IF_NONE_MATCH,
  };
  /** 透传的响应头 */
  private static final String[] PROXY_RESPONSE_HEADERS = new String[]{
    HttpHeaders.CONTENT_DISPOSITION,
    HttpHeaders.LAST_MODIFIED,
    HttpHeaders.ETAG,
  };

  private final DocChainProperties docChainProperties;
  private final DocChainLoginHelper docChainLoginHelper;
  private final DocChainDocumentHelper docChainDocumentHelper;

  /**
   * 读取资源
   */
  @GetMapping("v1/doc/read")
  @Operation(summary = "读取资源")
  @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "XSS_SERVLET"})
  @SuppressWarnings("PMD.GuardLogStatement")
  public void read(@RequestParam("tenantId") Long tenantId,
                   HttpServletRequest httpServletRequest,
                   HttpServletResponse httpServletResponse) {
    URI targetUrl = null;
    try {
      targetUrl = buildProxyRequestUrl(tenantId, httpServletRequest);
      ClientHttpRequest request = createProxyRequest(tenantId, httpServletRequest, targetUrl);
      // 手动执行请求，不使用 RestTemplate 的辅助方法以方便转发失败响应
      try (ClientHttpResponse response = request.execute()) {
        proxyResponse(httpServletResponse, response);
      }
    }
    catch (Exception e) {
      logger.warn("Failed to proxy DocChain request: params={}, url={}", httpServletRequest.getQueryString(), targetUrl, e);
      try {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        httpServletResponse.setContentType("text/plain;charset=UTF-8");
        httpServletResponse.getWriter().write(ExpUtil.getMsg(e));
      }
      catch (Exception e2) {
        // 忽略异常，多半是客户端关闭了连接，或者已发送了部分响应
        logger.debug("Failed to send error", e2);
      }
    }
  }

  /**
   * 构造 DocChain 接口地址
   */
  private URI buildProxyRequestUrl(Long tenantId, HttpServletRequest httpServletRequest) {
    // 带上除 tenantId 外的所有 URL 参数
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(docChainProperties.getReadDocumentApiUrl(tenantId));
    for (Entry<String, String[]> entry : httpServletRequest.getParameterMap().entrySet()) {
      if (!"tenantId".equals(entry.getKey())) {
        builder.queryParam(entry.getKey(), (Object[]) entry.getValue());
      }
    }
    return builder.encode().build().toUri();
  }

  /**
   * 构造请求对象
   */
  private ClientHttpRequest createProxyRequest(Long tenantId, HttpServletRequest httpServletRequest, URI url) throws IOException {
    ClientHttpRequest request = HttpUtil.getRestTemplate().getRequestFactory().createRequest(url, HttpMethod.GET);
    request.getHeaders().set(HttpHeaders.COOKIE, docChainLoginHelper.getCookie(tenantId));
    // 透传请求头
    for (String name : PROXY_REQUEST_HEADERS) {
      String value = httpServletRequest.getHeader(name);
      if (StringUtils.isNotEmpty(value)) {
        request.getHeaders().set(name, value);
      }
    }
    return request;
  }

  /**
   * 转发响应，失败时也转发
   */
  private void proxyResponse(HttpServletResponse httpServletResponse, ClientHttpResponse response) throws IOException {
    httpServletResponse.setStatus(response.getStatusCode().value());
    httpServletResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
    long contentLength = response.getHeaders().getContentLength();
    if (contentType != null) {
      httpServletResponse.setContentType(contentType);
    }
    if (contentLength > 0) {
      httpServletResponse.setContentLengthLong(contentLength);
    }
    // 透传响应头
    for (String name : PROXY_RESPONSE_HEADERS) {
      String value = response.getHeaders().getFirst(name);
      if (StringUtils.isNotEmpty(value)) {
        httpServletResponse.setHeader(name, value);
      }
    }
    try (InputStream inputStream = response.getBody()) {
      IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
    }
  }

  @GetMapping("getDocument")
  @Operation(summary = "获取文档")
  public ResultVO<DocChainDocumentWithContentDTO> getDocument(@RequestParam("tenantId") Long tenantId, @RequestParam("docId") Long docId, @RequestParam(name = "spaceId", required = false) Long spaceId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(docId, "文档 ID 不能为空");
    return ResultVO.success(docChainDocumentHelper.getDocumentWithContent(tenantId, docId, spaceId));
  }

  @GetMapping("getDocumentChunk")
  @Operation(summary = "获取文档")
  public ResultVO<DocChainChunkDTO> getDocumentChunk(@RequestParam("tenantId") Long tenantId, @RequestParam("docId") Long docId,
    @RequestParam("chunkId") Long chunkId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(docId, "文档 ID 不能为空");
    Assert.notNull(chunkId, "文档块 ID 不能为空");
    return ResultVO.success(docChainDocumentHelper.getDocumentChunk(tenantId, docId, chunkId));
  }
}
