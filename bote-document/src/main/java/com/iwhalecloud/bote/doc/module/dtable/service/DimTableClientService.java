package com.iwhalecloud.bote.doc.module.dtable.service;

import com.iwhalecloud.bote.doc.config.properties.DimTableServerProperties;
import com.iwhalecloud.bote.doc.module.dtable.dto.DimTableCreateRO;
import com.iwhalecloud.bote.doc.module.dtable.dto.DimTableCreateResultDTO;
import com.iwhalecloud.bote.doc.module.dtable.dto.ResponseData;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 *
 * @author Aiqing
 * @since 2026/1/9
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DimTableClientService implements InitializingBean {

  public static final String REQUEST_API_KEY_HEADER = "X-Internal-Api-Key";
  public static final String URL_CREATE_DOCUMENT_TABLE = "/internal/space/createWithDefaultDatasheet";
  public static final String URL_QUERY_SPACE_FIRST_NODE = "/internal/spaces/{spaceId}/firstNode";
  private static final Logger logger = LoggerFactory.getLogger(DimTableClientService.class);
  private final DimTableServerProperties dimTableServerProperties;
  private RestClient restClient;


  /**
   * 创建文档关联的多维表格空间
   *
   * @param documentId 文档ID
   * @param ownerId 文档所有者ID
   * @return 创建结果
   */
  public DimTableCreateResultDTO createDimTableSpace(String documentId, Long ownerId) {
    DimTableCreateRO createRO = new DimTableCreateRO();
    createRO.setBindEntityId(documentId);
    createRO.setSpaceType("DOCUMENT");
    createRO.setOwnerId(ownerId);
    createRO.setName(String.format("文档%s空间", documentId));
    ResponseData<DimTableCreateResultDTO> resultVO = restClient.post()
      .uri(URL_CREATE_DOCUMENT_TABLE)
      .body(createRO)
      .retrieve()
      .body(new ParameterizedTypeReference<>() {
      });
    if (resultVO == null || !Boolean.TRUE.equals(resultVO.getSuccess())) {
      logger.error("创建多维表格异常, owerId:{}, documentId:{}, error:{}", ownerId, documentId,
        resultVO == null ? "null" : resultVO.getMessage());
      throw new BssException("创建多维表格异常");
    }
    return resultVO.getData();
  }

  /**
   * 查询文档关联的多维表格空间内的第一个表格
   *
   * @param spaceId 空间ID
   * @return 表格Id
   */
  public String queryDocumentSpaceFirstNodeId(String spaceId) {
    ResponseData<String> resultVO = restClient.get()
      .uri(URL_QUERY_SPACE_FIRST_NODE, spaceId)
      .retrieve()
      .body(new ParameterizedTypeReference<>() {
      });
    if (resultVO == null || !Boolean.TRUE.equals(resultVO.getSuccess())) {
      logger.error("调用多维表格异常 queryDocumentSpaceFirstNode error, spaceId:{}, error:{}", spaceId,
        resultVO == null ? "null" : resultVO.getMessage());
      throw new BssException("调用多维表格异常");
    }
    return resultVO.getData();
  }

  @SuppressWarnings("PMD.CloseResource")
  @Override
  public void afterPropertiesSet() throws Exception {
    RequestConfig requestConfig = RequestConfig.custom()
      .setConnectionRequestTimeout(10, TimeUnit.SECONDS)
      .setResponseTimeout(20, TimeUnit.SECONDS)
      .build();
    CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    this.restClient = RestClient.builder()
      .baseUrl(dimTableServerProperties.getServerUrl())
      .defaultHeader(REQUEST_API_KEY_HEADER, dimTableServerProperties.getApiKey())
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      .requestFactory(requestFactory)
      .build();
  }
}
