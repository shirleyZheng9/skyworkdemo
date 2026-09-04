package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentExportService;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainChunkDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainChunkDetailDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainDocumentDetailDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainDocumentWithContentDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainUploadFileDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.request.DocSplitRequest;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryChunkResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryChunkResponse.ChunkInfo;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.QueryDocmentResponse.DocmentPageInfo;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.SplitDocResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.UploadChatDocmentResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.UploadDocmentResponse;
import com.iwhalecloud.bote.dto.knowledge.docchain.response.UploadDocmentResponse.DocmentFileSrc;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * DocChain 文档配置辅助类
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocChainDocumentHelper {
  private static final Logger logger = LoggerFactory.getLogger(DocChainDocumentHelper.class);
  /**
   * Markdown 图片 {@code ](url)} 中的 DocChain read URL（{@code /v1/doc/read?} 前可有任意前缀如 {@code /docchain}）。
   * <ul>
   *   <li>用 {@code (?:[^()]|\([^()]*\))} 代替 {@code [^)]}，允许 URL 内出现<strong>单层</strong>成对括号，如 {@code path=media/R-C (1).jpg}</li>
   *   <li>{@code /v1/doc/read?} 之后对整体采用非贪婪，使结束的 {@code \)} 对齐 Markdown 语法右括号，而非文件名里第一个 {@code )}</li>
   *   <li>{@code (?!\\.\\./api/bote)} 避免对已替换为博特代理前缀的地址二次处理</li>
   *   <li><strong>限制：</strong>不支持括号嵌套（如 {@code a(b(c)d)}）；多图同行且 URL 中含 {@code )} 的极端排版可能误匹配，宜换行或对 path 做 URL 编码</li>
   * </ul>
   */
  private static final Pattern imageUrlPattern = Pattern.compile(
    "\\]\\(((?!\\.\\./api/bote)(?:[^()]|\\([^()]*\\))*?/v1/doc/read\\?(?:[^()]|\\([^()]*\\))*?)\\)");
  /**
   * HTML {@code <img>} 的 {@code src} 中的 DocChain read URL（{@code /v1/doc/read?} 前可有任意前缀如 {@code /llmdoc}）。
   * 使用与闭合引号相同的分组，避免 URL 内 {@code &} 等与属性引号混淆。
   */
  private static final Pattern htmlImgDocReadSrcPattern = Pattern.compile(
    "(?i)(<img\\b[^>]*?\\bsrc\\s*=\\s*)(['\"])((?!\\.\\./api/bote)(?:(?!\\2).)*?/v1/doc/read\\?(?:(?!\\2).)*)\\2");
  /** Markdown 表格地址匹配模式，用于去除替换。[table_1_0](table/1.html) */
  private static final Pattern tableUrlPattern = Pattern.compile("\\[table[^]]{0,100}]\\([^)]{0,100}\\)");
  /** Markdown 中的图片地址的path参数值 */
  private static final Pattern pathPattern = Pattern.compile("([&?])path=([^&]*)");

  private final DocChainProperties properties;
  private final DocChainLoginHelper loginHelper;
  private final IFileStoreService fileStoreService;
  private final IDocumentExportService documentExportService;

  public Long uploadChatDocument(Long tenantId, @Nullable Long fileId, @Nullable File file) {
    Assert.isTrue(fileId != null || file != null, "文件 ID 和文件不能同时同时为空");
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    Path tmpDir = null;
    try {
      if (file != null) {
        params.add("files", new FileSystemResource(file));
      }
      else {
        FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
        Assert.notNull(fileInfo, () -> "文件不存在: fileId=" + fileId);
        tmpDir = Files.createTempDirectory("bote-doc-");
        File tempFile = tmpDir.resolve(fileInfo.getFileName()).toFile();
        fileStoreService.downloadFile(fileInfo.getFileId(), tempFile.getAbsolutePath());
        params.add("files", new FileSystemResource(tempFile));
      }
      // 调用外系统接口
      HttpHeaders headers = loginHelper.buildHeader(tenantId);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);
      String url = properties.getUploadChatDocumentApiUrl(tenantId) + "?async_flag=false&session_id=" + IDUtils.nextId22();
      ResponseEntity<UploadChatDocmentResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<UploadChatDocmentResponse>() {
        });
      UploadChatDocmentResponse res = responseEntity.getBody();
      if (res == null) {
        throw new BssException("DocChain 上传对话文档信息失败，结果为空");
      }
      if (BooleanUtils.isNotTrue(res.getSuccess())) {
        throw new BssException("DocChain 上传对话文档信息失败，" + res.getErr());
      }
      return res.getData().get(0).getId();
    }
    catch (IOException e) {
      throw new BssException("构造知识库文件失败: " + e.getMessage(), e);
    }
    finally {
      if (tmpDir != null) {
        FileUtils.deleteQuietly(tmpDir.toFile());
      }
    }
  }

  /**
   * 上传文档
   *
   * @param tenantId 租户 ID
   * @param topicId 主题 ID
   * @param fileIds 文件 ID 列表
   * @return 文件与文档关系
   */
  public Map<Long, Long> uploadDocument(Long tenantId, Long topicId, List<Long> fileIds) {
    List<FileInfoVO> fileInfos = fileStoreService.getFileInfoByIds(fileIds);
    MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    Path tmpDir = null;
    try {
      // 构造入参
      tmpDir = Files.createTempDirectory("bote-doc-");
      String fileTypes = BaseSystemParameter.ALLOW_DOCCHAIN_UPLOAD_FILE_TYPE.getValueFromDb();
      String[] allowFileTypes = fileTypes.split(",");
      for (FileInfoVO info : CollectionUtils.emptyIfNull(fileInfos)) {
        // 文件格式校验
        String fileType = StringUtils.substringAfterLast(info.getFileName(), ".");
        if (!ArrayUtils.contains(allowFileTypes, StringUtils.lowerCase(fileType))) {
          throw BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toException(fileType);
        }
        File file = tmpDir.resolve(info.getFileName()).toFile();
        fileStoreService.downloadFile(info.getFileId(), file.getAbsolutePath());
        params.add("files", new FileSystemResource(file));
      }

      // 调用外系统接口
      HttpHeaders headers = loginHelper.buildHeader(tenantId);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);
      String url = properties.getUploadDocumentApiUrl(tenantId) + "?topic_id=" + topicId;
      ResponseEntity<UploadDocmentResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<UploadDocmentResponse>() {
        });
      UploadDocmentResponse res = responseEntity.getBody();
      if (res != null && CollectionUtils.isNotEmpty(res.getFileSrc())) {
        Map<Long, Long> mapping = new HashMap<>(fileIds.size());
        for (DocmentFileSrc dto : res.getFileSrc()) {
          FileInfoVO fileInfo = IterableUtils.find(fileInfos, p -> p.getFileName().equals(dto.getPath()));
          if (fileInfo != null) {
            mapping.put(fileInfo.getFileId(), dto.getId());
          }
        }
        return mapping;
      }
      return Collections.emptyMap();
    }
    catch (IOException e) {
      throw new BssException("构造知识库文件失败: " + e.getMessage(), e);
    }
    finally {
      if (tmpDir != null) {
        FileUtils.deleteQuietly(tmpDir.toFile());
      }
    }
  }

  /**
   * 删除文档
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   */
  public void deleteDocument(Long tenantId, Long docId) {
    Map<String, Object> params = new HashMap<>(2);
    params.put("id", docId);
    params.put("operation", "delete");
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(params, headers);
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getUpdateDocumentApiUrl(tenantId), HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "err", "");
      throw new BssException("DocChain 删除文档失败: " + errMsg);
    }
  }

  /**
   * 重新构建文档
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   */
  public void redoDocument(Long tenantId, Long docId) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getRedoDocumentApiUrl(tenantId) + "?doc_id=" + docId + "&task=all";
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "err", "");
      throw new BssException("DocChain 重新构建文档失败: " + errMsg);
    }
  }

  /**
   * 部分格式文件，不支持自动触发生成摘要环节，需要人工处理，例如 xls,xlsx
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   */
  public void summaryDocument(Long tenantId, Long docId) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getSummaryDocumentApiUrl(tenantId) + "?doc_id=" + docId;
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "err", "");
      throw new BssException("DocChain 生成摘要失败: " + errMsg);
    }
  }

  /**
   * 查询文档列表
   *
   * @param tenantId 租户 ID
   * @return 文档列表
   */
  public List<QueryDocmentResponse.DocmentInfo> queryDocument(Long tenantId, Long topicId) {
    String url = properties.getQueryDocumentListApiUrl(tenantId) + "?topic_id=" + topicId;
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    List<QueryDocmentResponse.DocmentInfo> documents = HttpUtil.get(url, null, new ParameterizedTypeReference<List<QueryDocmentResponse.DocmentInfo>>() {
    }, headers);
    return ListUtils.emptyIfNull(documents);
  }

  public DocmentPageInfo queryDocumentPage(Long tenantId, Long topicId, String keyword, int pageNum, int pageSize) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getQueryDocumentApiUrl(tenantId) + "?page=" + pageNum + "&size=" + pageSize;
    if (Objects.nonNull(topicId)) {
      url = url + "&topic_id=" + topicId;
    }
    if (StringUtils.isNotEmpty(keyword)) {
      url = url + "&keyword=" + keyword;
    }
    ResponseEntity<QueryDocmentResponse> responseEntity = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<QueryDocmentResponse>() {
      });
    QueryDocmentResponse res = responseEntity.getBody();
    if (res == null) {
      throw new BssException("DocChain 查询文档列表失败，结果为空");
    }
    if (!BooleanUtils.isTrue(res.getSuccess())) {
      throw new BssException("DocChain 查询文档列表失败: " + res.getErr());
    }
    return res.getData();
  }

  /**
   * 获取文档信息和文档内容
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   * @return 文档信息，不存在时抛异常
   */
  public DocChainDocumentWithContentDTO getDocumentWithContent(Long tenantId, Long docId, Long spaceId) {
    DocChainDocumentDetailDTO detail = getDocumentDetail(tenantId, docId);
    String content = readDocument(tenantId, docId, null);
    DocChainDocumentWithContentDTO document = new DocChainDocumentWithContentDTO();
    document.setDocId(docId);
    document.setDocName(detail.getPath());
    document.setContent(content);
    return document;
  }

  /**
   * 获取文档详情
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   * @return 文档详情，不存在时抛异常
   */
  public DocChainDocumentDetailDTO getDocumentDetail(Long tenantId, Long docId) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    // 请求体为文档 ID 列表
    List<Long> docIds = Collections.singletonList(docId);
    HttpEntity<?> requestEntity = new HttpEntity<>(docIds, headers);
    String url = properties.getDocumentDetailApiUrl(tenantId);
    ResponseEntity<JsonNode> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, JsonNode.class);
    JsonNode body = responseEntity.getBody();
    if (body == null || body.isNull()) {
      logger.error("Failed to get DocChain document detail, no response body: tenantId={}, docId={}, status={}, headers={}",
        tenantId, docId, responseEntity.getStatusCode().value(), responseEntity.getHeaders());
      throw new BssException("查询 DocChain 文档详情失败，响应为空");
    }
    // 报错时可能会以 200 状态码返回，需要检查响应体类型
    else if (body.isObject()) {
      String error = body.path("err").asText("");
      if (StringUtils.isEmpty(error)) {
        error = body.path("error_code").asText("未知错误");
        logger.error("Failed to get DocChain document detail: tenantId={}, docId={}, response={}", tenantId, docId, body);
      }
      throw new BssException("查询 DocChain 文档详情失败: " + error);
    }
    // 成功时应该返回数组
    else if (!body.isArray()) {
      logger.error("Failed to get DocChain document detail, invalid response body: tenantId={}, docId={}, body={}", tenantId, docId, body);
      throw new BssException("查询 DocChain 文档详情失败，响应类型错误");
    }
    List<DocChainDocumentDetailDTO> documents = JsonUtil.convert(body, new TypeReference<>() {
    });
    DocChainDocumentDetailDTO document = IterableUtils.find(documents, d -> docId.equals(d.getId()));
    if (document == null) {
      throw new BssException("DocChain 文档不存在: docId=" + docId);
    }
    return document;
  }

  /**
   * 查询文档内容，返回 Markdown 格式
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   * @param readFormat 阅读格式
   * @return Markdown 格式的文档内容
   */
  public String readDocument(Long tenantId, Long docId, String readFormat) {
    if (StringUtils.isEmpty(readFormat)) {
      readFormat = KnowledgeConsts.DEFAULT_READ_FORMAT_MARKDOWN;
    }
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getReadDocumentApiUrl(tenantId) + "?doc_id=" + docId + "&read_format=" + readFormat;
    ResponseEntity<String> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.GET, requestEntity, String.class);
    String content = responseEntity.getBody();
    if (StringUtils.isEmpty(content)) {
      throw new BssException("DocChain 查询文档信息失败，结果为空");
    }
    if (KnowledgeConsts.DEFAULT_READ_FORMAT_MARKDOWN.equals(readFormat)) {
      // 将图片地址替换为我们的转发接口地址，以解决跨域和鉴权问题
      content = replaceImageUrlsWithEncodedPath(content, tenantId);
      // 去除docChain用于解析的表格地址, 如[table_0_1](table/1.html)
      return tableUrlPattern.matcher(content).replaceAll("");
    }
    else if (KnowledgeConsts.READ_FORMAT_HTML.equals(readFormat)) {
      // 使用正则表达式移除 <head> 标签及其内容
      return content.replaceAll("(?s)<head>.*?</head>", "");
    }
    else {
      return content;
    }
  }

  /**
   * 替换 Markdown {@code ![...](url)} 与 HTML {@code <img src="...">} 中的 DocChain read URL，并对 path 参数进行 URL 编码
   *
   * @param content Markdown / HTML 混合内容
   * @param tenantId 租户 ID
   * @return 替换后的内容
   */
  public String replaceImageUrlsWithEncodedPath(String content, Long tenantId) {
    if (StringUtils.isEmpty(content)) {
      return content;
    }
    StringBuilder result = new StringBuilder();
    Matcher mdMatcher = imageUrlPattern.matcher(content);
    while (mdMatcher.find()) {
      String newUrl = rewriteDocReadUrlToBote(mdMatcher.group(1), tenantId);
      mdMatcher.appendReplacement(result, Matcher.quoteReplacement("](" + newUrl + ")"));
    }
    mdMatcher.appendTail(result);
    content = result.toString();

    result = new StringBuilder();
    Matcher imgMatcher = htmlImgDocReadSrcPattern.matcher(content);
    while (imgMatcher.find()) {
      String newUrl = rewriteDocReadUrlToBote(imgMatcher.group(3), tenantId);
      String quote = imgMatcher.group(2);
      imgMatcher.appendReplacement(result, Matcher.quoteReplacement(imgMatcher.group(1) + quote + newUrl + quote));
    }
    imgMatcher.appendTail(result);
    return result.toString();
  }

  /**
   * 将 DocChain 的 {@code /.../v1/doc/read?} 地址转为博特代理前缀，并对 {@code path} 查询参数编码。
   */
  private String rewriteDocReadUrlToBote(String originalUrl, Long tenantId) {
    String newUrl = originalUrl.replaceFirst(".*/v1/doc/read\\?", "../api/bote/docchain/v1/doc/read?tenantId=" + tenantId + "&");
    return encodePathParameter(newUrl);
  }

  /**
   * 对URL中的path参数值进行编码
   *
   * @param url 需要编码的URL
   * @return 编码后的URL
   */
  private String encodePathParameter(String url) {
    // 查找path参数
    Matcher pathMatcher = pathPattern.matcher(url);
    StringBuilder result = new StringBuilder();
    while (pathMatcher.find()) {
      try {
        // 获取分隔符（&或?）
        String separator = pathMatcher.group(1);
        // 对path参数值进行URL编码，但只编码必要的字符
        String pathValue = pathMatcher.group(2);
        // 替换原始path值为编码后的值
        pathMatcher.appendReplacement(result, separator + "path=" + URLEncoder.encode(pathValue, StandardCharsets.UTF_8));
      }
      catch (Exception e) {
        // 如果处理失败，则返回原始匹配
        pathMatcher.appendReplacement(result, pathMatcher.group(0));
      }
    }
    pathMatcher.appendTail(result);
    return result.toString();
  }

  /**
   * 获取文档块信息
   *
   * @param tenantId 租户 ID
   * @param docId 文档 ID
   * @param chunkId 文档块 ID
   * @return 文档块信息
   */
  @Nullable
  public DocChainChunkDTO getDocumentChunk(Long tenantId, Long docId, Long chunkId) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getDocChunkRelGetApiUrl(tenantId) + "?doc_id=" + docId + "&chunk_id=" + chunkId;
    ResponseEntity<QueryChunkResponse> responseEntity = HttpUtil.getRestTemplate().exchange(url, HttpMethod.GET, requestEntity, QueryChunkResponse.class);
    QueryChunkResponse response = responseEntity.getBody();
    if (response == null) {
      return null;
    }
    DocChainChunkDTO chunk = new DocChainChunkDTO();
    chunk.setDocId(docId);
    chunk.setPreChunk(createDocChainChunk(chunk, response.getPreChunk(), tenantId));
    chunk.setChunk(createDocChainChunk(chunk, response.getChunk(), tenantId));
    chunk.setNextChunk(createDocChainChunk(chunk, response.getNextChunk(), tenantId));
    return chunk;
  }

  private DocChainChunkDetailDTO createDocChainChunk(DocChainChunkDTO chunk, ChunkInfo chunkInfo, Long tenantId) {
    if (chunkInfo == null) {
      return null;
    }
    DocChainChunkDetailDTO chunkDetail = new DocChainChunkDetailDTO();
    chunkDetail.setChunkId(chunkInfo.getChunkId());
    if (StringUtils.isNotEmpty(chunkInfo.getContent())) {
      String content = replaceImageUrlsWithEncodedPath(chunkInfo.getContent(), tenantId);
      chunkDetail.setChunkContent(tableUrlPattern.matcher(content).replaceAll(""));
    }
    if (StringUtils.isNotEmpty(chunkInfo.getHeadingChain()) && chunkInfo.getHeadingChain().contains("#")) {
      String[] split = StringUtils.split(chunkInfo.getHeadingChain(), '#');
      chunkDetail.setChunkTitle(split[1]);
      chunk.setDocName(split[0]);
    }
    else {
      chunk.setDocName(chunkInfo.getHeadingChain());
    }
    if (chunkInfo.isImage()) {
      chunkDetail.setChunkUrl(
        "../api/bote/docchain/v1/doc/read?tenantId=" + tenantId + "&doc_id=" + chunkInfo.getDocId() + "&read_format=path&path=" + chunkInfo.getUrl());
    }
    chunkDetail.setChunkType(chunkInfo.getType());
    if (StringUtils.isEmpty(chunkDetail.getChunkType()) && chunkDetail.getChunkId() >= 1000000L) {
      chunkDetail.setChunkType("text");
    }
    return chunkDetail;
  }

  public List<String> uploadAndReadDocument(Long tenantId, @Nullable Long fileId, @Nullable File file) {
    Long docId = uploadChatDocument(tenantId, fileId, file);
    return Arrays.asList(StringUtils.split(readDocument(tenantId, docId, null), '\n'));
  }

  /**
   * 上传文档 d
   *
   * @param tenantId 租户 ID
   * @param topicId 主题 ID
   * @param dcDocumentIds 文档库文档 ID 列表
   * @return 文件与文档关系
   */
  public DocChainUploadFileDTO uploadDocumentNew(Long tenantId, Long topicId, List<String> dcDocumentIds) {
    DocChainUploadFileDTO dto = new DocChainUploadFileDTO();
    if (CollectionUtils.isEmpty(dcDocumentIds)) {
      dto.setDocIds(Collections.emptyMap());
      dto.setFileSizes(Collections.emptyMap());
      return dto;
    }
    Map<String, FileInfoVO> tempMap = new HashMap<>(dcDocumentIds.size());
    Map<String, Long> documentfileSize = new HashMap<>(dcDocumentIds.size());
    List<FileInfoVO> fileInfos = new ArrayList<>();
    dcDocumentIds.forEach(dcDocumentId -> {
      FileInfoVO fileInfo = documentExportService.exportDocument(dcDocumentId);
      if (fileInfo != null) {
        fileInfos.add(fileInfo);
        tempMap.put(dcDocumentId, fileInfo);
        documentfileSize.put(dcDocumentId, fileInfo.getFileSize());
      }
    });
    if (CollectionUtils.isEmpty(fileInfos)) {
      dto.setDocIds(Collections.emptyMap());
      dto.setFileSizes(Collections.emptyMap());
      return dto;
    }
    Map<Long, Long> longLongMap = uploadDocumentHandle(tenantId, topicId, fileInfos);
    Map<String, Long> resultMap = new HashMap<>();
    if (MapUtils.isEmpty(longLongMap)) { // 容错处理
      dto.setDocIds(Collections.emptyMap());
      dto.setFileSizes(Collections.emptyMap());
      return dto;
    }
    tempMap.forEach((k, v) -> {
      resultMap.put(k, longLongMap.get(v.getFileId()));
    });
    dto.setDocIds(resultMap);
    dto.setFileSizes(documentfileSize);
    return dto;
  }

  private Map<Long, Long> uploadDocumentHandle(Long tenantId, Long topicId, List<FileInfoVO> fileInfos) {
    MultiValueMap<String, Object> paramds = new LinkedMultiValueMap<>();
    Path tmpDir = null;
    try {
      // 构造入参
      tmpDir = Files.createTempDirectory("bote-doc-");
      String fileTypes = BaseSystemParameter.ALLOW_DOCCHAIN_UPLOAD_FILE_TYPE.getValueFromDb();
      String[] allowFileTypes = fileTypes.split(",");
      for (FileInfoVO info : CollectionUtils.emptyIfNull(fileInfos)) {
        // 文件格式校验
        String fileType = StringUtils.substringAfterLast(info.getFileName(), ".");
        if (!ArrayUtils.contains(allowFileTypes, StringUtils.lowerCase(fileType))) {
          throw BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toException(fileType);
        }
        File file = tmpDir.resolve(info.getFileName()).toFile();
        fileStoreService.downloadFile(info.getFileId(), file.getAbsolutePath());
        paramds.add("files", new FileSystemResource(file));
      }

      // 调用外系统接口
      HttpHeaders headers = loginHelper.buildHeader(tenantId);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      HttpEntity<?> requestEntity = new HttpEntity<>(paramds, headers);
      String url = properties.getUploadDocumentApiUrl(tenantId) + "?topic_id=" + topicId;
      ResponseEntity<UploadDocmentResponse> responseEntity = HttpUtil.getRestTemplate()
        .exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<UploadDocmentResponse>() {
        });
      UploadDocmentResponse responseBody = responseEntity.getBody();
      if (responseBody != null && CollectionUtils.isNotEmpty(responseBody.getFileSrc())) {
        Map<Long, Long> mappding = new HashMap<>(fileInfos.size());
        for (UploadDocmentResponse.DocmentFileSrc dto : responseBody.getFileSrc()) {
          FileInfoVO fileInfo = IterableUtils.find(fileInfos, p -> p.getFileName().equals(dto.getPath()));
          if (fileInfo != null) {
            mappding.put(fileInfo.getFileId(), dto.getId());
          }
        }
        return mappding;
      }
      return Collections.emptyMap();
    }
    catch (IOException e) {
      throw new BssException("构造知识库文件失败: " + e.getMessage(), e);
    }
    finally {
      if (tmpDir != null) {
        FileUtils.deleteQuietly(tmpDir.toFile());
      }
    }
  }

  /**
   * 同步文档分页
   *
   * @param tenantId 租户 ID
   * @return 文档分页结果
   */
  public SplitDocResponse syncDocSplit(Long tenantId, DocSplitRequest request) {
    try {
      String asyncSplitApiUrl = properties.getSyncSplitApiUrl(tenantId);
      // 构造请求头
      HttpHeaders headers = loginHelper.buildHeader(tenantId);
      headers.setContentType(MediaType.APPLICATION_JSON);
      // 构造请求数据
      Map<String, Object> params = new HashMap<>();
      // 使用默认主题
      params.put("topic_id", StringUtils.isNotEmpty(request.getTopicId()) ? request.getTopicId() : "1");
      params.put("content", request.getContent());
      params.put("title", request.getTitle());
      params.put("convert_pdf", request.getConvertPdf() != null ? request.getConvertPdf() : false);
      params.put("summary", request.getSummary() != null ? request.getSummary() : false);
      params.put("pdf_split_model", StringUtils.isNotEmpty(request.getPdfSplitModel()) ? request.getPdfSplitModel() : "vlm");
      // 发送请求
      SplitDocResponse splitDocResponse = HttpUtil.post(asyncSplitApiUrl, params, new ParameterizedTypeReference<>() {
      }, headers);
      if (splitDocResponse == null) {
        throw new BssException("文档拆分失败: 响应结果为空");
      }
      if (!"1".equals(splitDocResponse.getCode())) {
        throw new BssException("文档拆分失败: " + splitDocResponse.getErr());
      }
      return splitDocResponse;
    }
    catch (Exception e) {
      logger.error("Failed to split doc, message = {}", e.getMessage(), e);
      throw new BssException("文档拆分失败: " + e.getMessage(), e);
    }
  }
}
