package com.iwhalecloud.bote.doc.module.knowledge.strategy;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.enums.KnowledgeCatalogEnum;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeAccessDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeCatalogDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeFileDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeBasicDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeResourceDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.KnowledgeTokenDTO;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowResourceQueryRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.request.KnowledgeQueryRequest;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.KnowledgeDocContentResponse;
import com.iwhalecloud.bote.dto.knowledge.access.platform.response.ResultResponse;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeDocumentParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeFileQueryParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgePlatformApiService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgePlatformApiHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 知识中台查询策略实现
 *
 * @author lxs
 * @since 2025/07/14
 */
@Service
@RequiredArgsConstructor
public class KnowledgePlatformStrategy implements IKnowledgeAccessStrategy {
  private static final Logger logger = LoggerFactory.getLogger(KnowledgePlatformStrategy.class);

  @Value("${zhxy.apiUrl:}")
  private String apiUrl;

  @Value("${zhxy.defaultUserId:}")
  private String defaultUserId;

  /** 知识中台文档下载地址 */
  private static final String DOWN_LOAD_API_URL = "/open-api/download/";

  /** Markdown 中的图片地址匹配模式，用于替换地址。/tDocDocchainDocument/ 前面的前缀是知识中台服务器端配置的，不同环境可能不一样 */
  private static final Pattern imageUrlPattern = Pattern.compile("]\\([\\w/_]*/tDocDocchainDocument/docContent\\?");
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

  private final IKnowledgePlatformApiService knowledgePlatformApiService;
  private final KnowledgePlatformApiHelper platformApiHelper;
  private final IKnowledgePlatformApiService platformApiService;

  @Override
  public String getKnowledgeAccessType() {
    return KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM;
  }

  @Override
  public ResultVO<List<KnowledgeCatalogDTO>> queryKnowledgeCatalog() {
    // 知识中台的知识库目录默认返回
    List<KnowledgeCatalogDTO> catalogList = new ArrayList<>();
    for (KnowledgeCatalogEnum catalogEnum : KnowledgeCatalogEnum.getEnums()) {
      KnowledgeCatalogDTO catalog = new KnowledgeCatalogDTO();
      catalog.setParCatalogId(-1L);
      catalog.setCatalogId(catalogEnum.getCatalogId());
      catalog.setCatalogName(catalogEnum.getCatalogName());
      catalog.setCatalogType(KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM);
      catalogList.add(catalog);
    }

    return ResultVO.success(catalogList);
  }

  @Override
  public ResultVO<PageInfo<KnowledgeAccessDTO>> queryKnowledgeInfoPage(KnowledgeQueryParams queryParams) {
    Long catalogId = queryParams.getCatalogId();
    // 如果不是指定目录，则返回空
    if (!KnowledgeCatalogEnum.getCatalogs().contains(catalogId)) {
      return ResultVO.success();
    }
    ResultResponse<List<KnowledgeBasicDTO>> response;
    KnowledgeQueryRequest queryRequest = new KnowledgeQueryRequest();
    queryRequest.setPageIndex(queryParams.getPageNum());
    queryRequest.setPageSize(queryParams.getPageSize());
    try {
      // 查询我创建的
      if (KnowledgeCatalogEnum.MY_FOLDER.getCatalogId().equals(catalogId)) {
        response = knowledgePlatformApiService.queryMyKnowledgeBaseList(queryRequest);
      }
      // 查询他人分享的
      else if (KnowledgeCatalogEnum.OTHER_SHARE.getCatalogId().equals(catalogId)) {
        response = knowledgePlatformApiService.querySharedByOtherKnowledgeBaseList(queryRequest);
      }
      else {
        return ResultVO.fail("未知的目录");
      }
      // 返回结果
      if (response.isSuccess()) {
        PageInfo<KnowledgeAccessDTO> result = convertPageInfo(response, this::convertToKnowledgeAccess, queryParams.getPageNum(), queryParams.getPageSize());
        return ResultVO.success(result);
      }
      else {
        return ResultVO.fail(response.getMessage());
      }
    } catch (Exception e) {
      logger.error("分页查询知识库失败", e);
      return ResultVO.fail("分页查询知识库失败: " + e.getMessage());
    }
  }

  @Override
  public ResultVO<PageInfo<KnowledgeFileDTO>> queryKnowledgeFilePage(KnowledgeFileQueryParams queryParams) {
    // 组装参数
    KnowResourceQueryRequest queryRequest = new KnowResourceQueryRequest();
    queryRequest.setAssistantWid(queryParams.getKnowledgeId());
    queryRequest.setResourceNameKeyWord(queryParams.getDocName());
    queryRequest.setPageIndex(queryParams.getPageNum());
    queryRequest.setPageSize(queryParams.getPageSize());

    // 调知识中台查询资源接口
    try {
      ResultResponse<List<KnowledgeResourceDTO>> response = knowledgePlatformApiService.queryKnowResourceWithTagsPage(queryRequest);
      // 返回结果
      if (response.isSuccess()) {
        PageInfo<KnowledgeFileDTO> result = convertPageInfo(response, this::convertToKnowledgeFile, queryParams.getPageNum(), queryParams.getPageSize());
        return ResultVO.success(result);
      }
      else {
        return ResultVO.fail(response.getMessage());
      }
    } catch (Exception e) {
      logger.error("分页查询知识库文档失败", e);
      return ResultVO.fail("分页查询知识库文档失败: " + e.getMessage());
    }
  }

  @Override
  public ResultVO<KnowledgeDocumentDTO> getKnowledgeDocument(KnowledgeDocumentParams documentParams) {
    // 调知识中台接口
    String resourceType = KnowledgeConsts.DOCUMENT;
    String resourceWid = documentParams.getDocId().toString();
    ResultResponse<KnowledgeDocContentResponse> response = knowledgePlatformApiService.knowledgeDocContent(resourceType, resourceWid);
    if (response == null) {
      return ResultVO.fail("查询文档内容失败");
    }
    if (response.isSuccess()) {
      KnowledgeDocumentDTO document = new KnowledgeDocumentDTO();
      document.setDocId(Long.parseLong(resourceWid));
      document.setDocName(documentParams.getDocName());
      String content = response.getData().getContent();
      document.setContent(convertContent(content, response.getData().getRedFormat()));
      return ResultVO.success(document);
    }
    else {
      return ResultVO.fail(response.getMessage());
    }
  }

  @Override
  @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "XSS_SERVLET"})
  @SuppressWarnings("PMD.GuardLogStatement")
  public void downloadKnowledgeDoc(Long docId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    URI targetUrl = null;
    try {
      targetUrl = buildRequestUrl(docId);
      ClientHttpRequest request = createRequest(httpServletRequest, targetUrl);
      // 手动执行请求，不使用 RestTemplate 的辅助方法以方便转发失败响应
      try (ClientHttpResponse response = request.execute()) {
        setResponse(httpServletResponse, response);
      }
    } catch (Exception e) {
      logger.warn("Failed to proxy DocChain request: params={}, url={}", httpServletRequest.getQueryString(), targetUrl, e);
      try {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        httpServletResponse.setContentType("text/plain;charset=UTF-8");
        httpServletResponse.getWriter().write(ExpUtil.getMsg(e));
      }
      catch (Exception e2) {
        logger.debug("Failed to send error", e2);
      }
    }
  }

  private void setResponse(HttpServletResponse httpServletResponse, ClientHttpResponse response) throws IOException {
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

  private ClientHttpRequest createRequest(HttpServletRequest httpServletRequest, URI url) throws IOException {
    ClientHttpRequest request = HttpUtil.getRestTemplate().getRequestFactory().createRequest(url, HttpMethod.GET);
    HttpHeaders headers = platformApiHelper.buildHeader(url.getPath());
    String extUserId = SessionUtil.getLoginInfo().getExtUserId();
    // 如果没有(如API免鉴权调用)，则使用默认用户ID
    if (StringUtils.isEmpty(extUserId)) {
      extUserId = defaultUserId;
    }
    ResultResponse<KnowledgeTokenDTO> knowledgeToken = platformApiService.getKnowledgeTokenByUserId(extUserId);
    if (knowledgeToken.isSuccess()) {
      KnowledgeTokenDTO data = knowledgeToken.getData();
      Pair<String, String> tokenPair = Pair.of(data.getTokenName(), data.getToken());
      headers.set(tokenPair.getLeft(), tokenPair.getRight());
    }
    request.getHeaders().putAll(headers);
    // 透传请求头
    for (String name : PROXY_REQUEST_HEADERS) {
      String value = httpServletRequest.getHeader(name);
      if (StringUtils.isNotEmpty(value)) {
        request.getHeaders().set(name, value);
      }
    }
    return request;
  }

  private URI buildRequestUrl(Long docId) {
    String serviceUrl = apiUrl + DOWN_LOAD_API_URL + docId;
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl);
    return builder.build().toUri();
  }

  private String convertContent(String content, String readFormat) {
    if (StringUtils.isEmpty(content)) {
      throw new BssException("DocChain 查询文档信息失败，结果为空");
    }
    if (StringUtils.isEmpty(readFormat)) {
      readFormat = KnowledgeConsts.DEFAULT_READ_FORMAT_MARKDOWN;
    }
    if (KnowledgeConsts.DEFAULT_READ_FORMAT_MARKDOWN.equals(readFormat)) {
      // 将图片地址替换为我们的转发接口地址，以解决跨域和鉴权问题
      String imageUrl = "](../api/bote/knowledge/access/getKnowledgeDoc";
      return imageUrlPattern.matcher(content).replaceAll(imageUrl);
    }
    else if (KnowledgeConsts.READ_FORMAT_HTML.equals(readFormat)) {
      // 使用正则表达式移除 <head> 标签及其内容
      return content.replaceAll("(?s)<head>.*?</head>", "");
    }
    else {
      return content;
    }
  }

  private static <T, R> PageInfo<R> convertPageInfo(ResultResponse<List<T>> response, Function<T, R> converter, Integer pageNum, Integer pageSize) {
    PageInfo<R> result = new PageInfo<>();
    result.setPageNum(pageNum);
    result.setPageSize(pageSize);
    result.setPages(response.getTotalPage());
    result.setTotal(response.getTotalSize());
    result.setSize(response.getData().size());
    result.setPrePage(pageNum == 1 ? 0 : pageNum - 1);
    result.setNextPage(response.isNextPage() ? pageNum + 1 : 0);
    result.setList(response.getData().stream().map(converter).collect(Collectors.toList()));
    return result;
  }

  /**
   * 将 KnowledgeBasicDTO 转换为 KnowledgeAccessDTO
   */
  private KnowledgeAccessDTO convertToKnowledgeAccess(KnowledgeBasicDTO knowledgeBase) {
    KnowledgeAccessDTO access = new KnowledgeAccessDTO();
    access.setKnowledgeId(Long.valueOf(knowledgeBase.getWid()));
    access.setKnowledgeName(knowledgeBase.getName());
    access.setKnowledgeType(KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM);
    access.setKnowledgeDesc(knowledgeBase.getSummary());
    access.setKnowledgeIcon(knowledgeBase.getIcon());
    access.setTopicId(knowledgeBase.getTopicId());
    access.setFileCounts(knowledgeBase.getSubResourceCount());
    return access;
  }

  /**
   * 将 KnowledgeResourceDTO 转换为 KnowledgeFileDTO
   */
  private KnowledgeFileDTO convertToKnowledgeFile(KnowledgeResourceDTO knowledgeBase) {
    KnowledgeFileDTO file = new KnowledgeFileDTO();
    file.setKnowledgeId(knowledgeBase.getAssistantWid());
    file.setResourceWid(knowledgeBase.getWid());
    file.setResourceType(knowledgeBase.getResourceType());
    file.setDocId(knowledgeBase.getDocId());
    file.setDocName(knowledgeBase.getResourceName());
    file.setDoctype(knowledgeBase.getResourceType());
    file.setDocSize(knowledgeBase.getResourceSize());
    return file;
  }
}
