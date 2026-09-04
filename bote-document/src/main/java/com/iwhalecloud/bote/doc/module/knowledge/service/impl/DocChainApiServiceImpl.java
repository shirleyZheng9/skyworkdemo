package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocChainApiService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainLoginHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * docChain接口代理服务实现
 *
 * @author qian.sisheng
 * @since 2025-05-14
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocChainApiServiceImpl implements IDocChainApiService {
  private final Logger logger = LoggerFactory.getLogger(DocChainApiServiceImpl.class);

  private final DocChainProperties properties;
  private final DocChainLoginHelper loginHelper;

  @Override
  public ResponseEntity<Object> queryTopicList(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getQueryTopicsApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> updateTopic(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getUpdateTopicApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> queryDocList(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getQueryDocumentListApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> queryDocPage(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getQueryDocumentApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> syncSplit(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getSyncSplitApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> asyncSplit(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getAsyncSplitApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> batchUpload(MultipartFile[] files, Map<String, Object> params) {
    MultiValueMap<String, Resource> multiParams = new LinkedMultiValueMap<>();
    multiParams.put("files", Arrays.stream(files).map(MultipartFile::getResource).toList());
    URI uri = buildUrl(params, null, properties.getUploadDocumentApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, multiParams, MediaType.MULTIPART_FORM_DATA, Object.class);
  }

  @Override
  public ResponseEntity<?> readDocument(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getReadDocumentApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, byte[].class);
  }

  @Override
  public ResponseEntity<Object> querySplitParams(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getSplitParamsApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> queryDocumentDetail(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getDocumentDetailApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, MapUtils.getObject(params, "doc_ids"), MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> deleteDocument(Map<String, Object> params) {
    String url = properties.getDeleteDocumentApiUrl(TenantIdUtil.getTenantId()) + "/{doc_id}";
    URI uri = buildUrl(null, params, url);
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> updateDocument(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getUpdateDocumentApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> queryChunksList(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getDocChunkListApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> addDocChunk(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getDocChunkCreateApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> deleteDocChunk(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getDeleteDocChunkApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> updateDocChunk(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getUpdateDocChunkApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> findDocChunk(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getDocGetChunkApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.GET, null, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> recall(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getRecallApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> rerank(Map<String, Object> params) {
    URI uri = buildUrl(null, null, properties.getRerankApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, params, MediaType.APPLICATION_JSON, Object.class);
  }

  @Override
  public ResponseEntity<Object> chatUpload(MultipartFile file, Map<String, Object> params) {
    MultiValueMap<String, Object> multiParams = new LinkedMultiValueMap<>();
    multiParams.set("file", file.getResource());
    URI uri = buildUrl(params, null, properties.getChatUploadApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, multiParams, MediaType.MULTIPART_FORM_DATA, Object.class);
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResponseEntity<Object> imgSearch(Map<String, Object> params) {
    URI uri = buildUrl(params, null, properties.getImgSearchApiUrl(TenantIdUtil.getTenantId()));
    return executeRequest(uri, HttpMethod.POST, null, MediaType.APPLICATION_JSON, Object.class);
  }

  /**
   * 执行请求
   *
   * @param url 请求地址
   * @param method 请求方法
   * @param body 请求体
   * @param contentType 请求体类型
   * @param clazz 响应类型
   * @return 结果
   */
  private <T> ResponseEntity<T> executeRequest(URI url, HttpMethod method, Object body, MediaType contentType, Class<T> clazz) {
    Long tenantId = TenantIdUtil.getTenantId();
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    if (contentType != null) {
      headers.setContentType(contentType);
    }
    headers.setAccept(Collections.singletonList(MediaType.ALL));
    ResponseEntity<T> result;
    try {
      HttpEntity<?> requestEntity = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
      logger.debug("DocChain proxy request: url={}, method={}, tenantId={}, params={}", url, method, tenantId, body);
      result = HttpUtil.getRestTemplate().exchange(url, method, requestEntity, clazz);
      logger.debug("DocChain proxy response: url={}, method={}, tenantId={}, response={}", url, method, tenantId, result.getBody());
    }
    catch (Exception e) {
      logger.error("Failed to execute request, message ={}", e.getMessage(), e);
      throw new BssException("DocChain 请求异常", e);
    }
    return result;
  }

  /**
   * 构建url
   *
   * @param queryParams 查询参数
   * @param pathParams 路径参数
   * @param url 请求地址
   * @return URI
   */
  public URI buildUrl(@Nullable Map<String, Object> queryParams, @Nullable Map<String, Object> pathParams, String url) {
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
    builder.encode();
    if (MapUtils.isNotEmpty(pathParams)) {
      return builder.build(pathParams);
    }
    return builder.build().toUri();
  }

}
