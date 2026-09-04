package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.util.DocChainApiUtil;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.config.properties.DocChainProperties;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocChainTopicParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.request.DocChainTopicRequest;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * DocChain 主题配置辅助类
 *
 * @author chen.linfa
 * @since 2024-10-16
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class DocChainTopicHelper {
  private static final Logger logger = LoggerFactory.getLogger(DocChainTopicHelper.class);

  private final DocChainProperties properties;
  private final DocChainLoginHelper loginHelper;

  /**
   * 查询主题列表
   *
   * @param tenantId 租户 ID
   * @return 主题列表
   */
  public List<Map<String, Object>> queryTopic(Long tenantId) {
    HttpEntity<?> requestEntity = buildRequestEntity(tenantId, null);
    ResponseEntity<List<Map<String, Object>>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getQueryTopicsApiUrl(tenantId), HttpMethod.GET, requestEntity,
        new ParameterizedTypeReference<List<Map<String, Object>>>() {
        });
    if (responseEntity.getBody() == null) {
      throw new BssException("DocChain 查询主题失败，结果为空");
    }
    return responseEntity.getBody();
  }

  /**
   * 创建主题
   *
   * @return 主题 ID
   */
  public Long createTopic(Long tenantId, DocChainTopicRequest request) {
    Assert.isNull(request.getTopicId(), "主题 ID 必须为空");
    JsonNode requestBody = buildUpdateTopicParams(request, "create");
    JsonNode res = callUpdateTopicApi(tenantId, requestBody, "create", "创建");
    JsonNode topicIdNode = res.path("data").path("id");
    if (!topicIdNode.isIntegralNumber()) {
      logger.error("Failed to create topic: request={}, response={}", requestBody, res);
      throw new BssException("DocChain 创建主题失败");
    }
    return topicIdNode.asLong();
  }

  /**
   * 修改主题
   */
  public void modifyTopic(Long tenantId, DocChainTopicRequest request) {
    Assert.notNull(request.getTopicId(), "主题 ID 不能为空");
    JsonNode requestBody = buildUpdateTopicParams(request, "modify");
    callUpdateTopicApi(tenantId, requestBody, "modify", "修改");
  }

  /**
   * 删除主题
   */
  public void deleteTopic(Long tenantId, Long topicId) {
    JsonNode requestBody = JsonUtil.getObjectMapper().createObjectNode()
      .put("operation", "delete")
      .put("id", topicId);
    callUpdateTopicApi(tenantId, requestBody, "delete", "删除");
  }

  /**
   * 调用更新主题接口，包括新增、修改、删除
   *
   * <p>请求参数使用 JsonNode 以便打印日志时自动转为 JSON 字符串</p>
   */
  private JsonNode callUpdateTopicApi(Long tenantId, JsonNode requestBody, String operationCode, String operationText) {
    HttpEntity<?> requestEntity = buildRequestEntity(tenantId, requestBody);
    JsonNode res;
    try {
      String apiUrl = properties.getUpdateTopicApiUrl(tenantId);
      res = HttpUtil.getRestTemplate().exchange(apiUrl, HttpMethod.POST, requestEntity, JsonNode.class).getBody();
    }
    catch (HttpStatusCodeException e) {
      String responseBody = e.getResponseBodyAsString();
      logger.error("Failed to {} topic: request={}, status={}, response={}", operationCode, requestBody, e.getStatusCode().value(), responseBody);
      String errorMsg = DocChainApiUtil.extractErrorMsg(e, responseBody);
      if (StringUtils.isNotEmpty(errorMsg) && errorMsg.contains("topic name exists in this user")) {
        throw new BssException("DocChain " + operationText + "主题失败: 知识库名称已存在", e);
      }
      throw new BssException("DocChain " + operationText + "主题失败: " + errorMsg, e);
    }
    catch (Exception e) {
      logger.error("Failed to {} topic: request={}", operationCode, requestBody, e);
      throw new BssException("DocChain " + operationText + "主题失败: " + ExpUtil.getMsg(e), e);
    }

    // 不大可能出现
    if (res == null) {
      logger.error("Failed to {} topic, empty response: request={}", operationCode, requestBody);
      throw new BssException("DocChain " + operationText + "主题失败，响应为空");
    }
    // 有些报错会通过 200 状态码返回，比如 topic 数量超出限制
    if (!res.path("success").asBoolean()) {
      logger.error("Failed to {} topic: request={}, response={}", operationCode, requestBody, res);
      throw new BssException("DocChain " + operationText + "主题失败: " + res.path("err").asText(""));
    }
    return res;
  }

  /**
   * 构造创建、修改主题的请求参数
   */
  private JsonNode buildUpdateTopicParams(DocChainTopicRequest request, String operation) {
    Long topicId = request.getTopicId();
    String topicName = request.getTopicName();
    String description = request.getComment();
    String strategy = request.getStrategy();
    // 优先使用请求参数中的策略
    if (StringUtils.isNotEmpty(strategy)) {
      Map<String, Object> extra = JsonUtil.parseJsonRequired(strategy, new TypeReference<Map<String, Object>>() {
      });
      extra.put("comment", description);
      Map<String, Object> params = new HashMap<>();
      params.put("operation", operation);
      params.put("id", topicId);
      params.put("name", topicName);
      params.put("extra", extra);
      return JsonUtil.convert(params, JsonNode.class);
    }
    // 未指定时使用默认策略
    String template = BaseSystemParameter.DOCCHAIN_TOPIC_API_REQUEST.getValueFromDb();
    Map<String, Object> params = new HashMap<>(4);
    params.put("operation", operation);
    params.put("topicId", topicId);
    params.put("topicName", topicName);
    params.put("comment", description);
    String content = FreemarkerUtil.process(template, params);
    return JsonUtil.readTree(content);
  }

  public List<String> getModelList(Long tenantId) {
    HttpEntity<?> requestEntity = buildRequestEntity(tenantId, null);
    ResponseEntity<List<String>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getModelListApiUrl(tenantId), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<String>>() {
      });
    if (responseEntity.getBody() == null) {
      throw new BssException("DocChain 查询大模型列表失败，结果为空");
    }
    return responseEntity.getBody();
  }

  /**
   * 获取DocChain主题参数
   */
  public DocChainTopicParamsDTO getDocchainTopicConfigParams(Long tenantId) {
    HttpEntity<?> requestEntity = buildRequestEntity(tenantId, null);
    ResponseEntity<DocChainTopicParamsDTO> responseEntity = HttpUtil.getRestTemplate()
      .exchange(properties.getSplitParamsApiUrl(tenantId), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<>() {
      });
    return responseEntity.getBody();
  }

  /**
   * 重新构建主题下的所有文档
   */
  public void redoDocuments(Long tenantId, Long topicId) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<?> requestEntity = new HttpEntity<>(headers);
    String url = properties.getRedoFilesApiUrl(tenantId) + "?topic_id=" + topicId;
    ResponseEntity<Map<String, Object>> responseEntity = HttpUtil.getRestTemplate()
      .exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
      });
    boolean success = MapUtils.getBooleanValue(responseEntity.getBody(), "success", false);
    if (!success) {
      String errMsg = MapUtils.getString(responseEntity.getBody(), "msg", "");
      throw new BssException("重新构建知识库下所有文档失败: " + errMsg);
    }
  }

  /**
   * 构造请求对象
   */
  private HttpEntity<?> buildRequestEntity(Long tenantId, @Nullable Object body) {
    HttpHeaders headers = loginHelper.buildHeader(tenantId);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
      return new HttpEntity<>(body, headers);
    }
    return new HttpEntity<>(headers);
  }

}

