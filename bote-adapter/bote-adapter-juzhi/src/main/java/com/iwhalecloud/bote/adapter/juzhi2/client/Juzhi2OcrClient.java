package com.iwhalecloud.bote.adapter.juzhi2.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.adapter.juzhi2.helper.Juzhi2ClientHelper;
import com.iwhalecloud.bote.service.external.JuzhiOcrClient;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * 二级聚智OCR客户端
 *
 * @author wangtingyun
 * @since 2025-06-25
 */
@Component
@ConditionalOnBooleanProperty("juzhi2.ocr.enabled")
public class Juzhi2OcrClient implements JuzhiOcrClient {

  private static final Logger logger = LoggerFactory.getLogger(Juzhi2OcrClient.class);

  @Override
  public Object parseFile(InputStream fileStream, String fileName) {
    // 上传文件
    String fileContentId = uploadFile(fileStream, fileName);
    // 调用文件解析工作流
    Map<String, Object> startNodeParams = new HashMap<>();
    startNodeParams.put("file", fileContentId);
    startNodeParams.put("type", "1");
    startNodeParams.put("fileName", fileName);
    return invokeOcrChat(startNodeParams, "fileOutput");
  }

  @Override
  public String parseVoice(InputStream fileStream, String fileName) {
    // 上传文件
    String fileContentId = uploadFile(fileStream, fileName);
    // 调用文件解析工作流
    Map<String, Object> startNodeParams = new HashMap<>();
    startNodeParams.put("file", fileContentId);
    startNodeParams.put("type", "2");
    startNodeParams.put("fileName", fileName);
    return invokeOcrChat(startNodeParams, "asrOutput");
  }

  @Override
  public Object parseImage(InputStream fileStream, String fileName) {
    // 上传文件
    String fileContentId = uploadFile(fileStream, fileName);
    // 调用文件解析工作流
    Map<String, Object> startNodeParams = new HashMap<>();
    startNodeParams.put("file", fileContentId);
    startNodeParams.put("type", "3");
    startNodeParams.put("fileName", fileName);
    return invokeOcrChat(startNodeParams, "ocrOutput");
  }

  /**
   * 调用聚智文件解析工作流
   */
  private String invokeOcrChat(Map<String, Object> startNodeParams, String resultNodeName) {
    // 获取调用工作流相关参数
    String assistantCode = Juzhi2ClientHelper.getOcrAssistantCode();
    HttpUrl url = Juzhi2ClientHelper.signUrl(assistantCode);
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildFileParseParams(startNodeParams, traceId, assistantCode);
    AtomicReference<String> reference = new AtomicReference<>();

    // on_message 回调处理器
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      JsonNode partsNode = msgData.path("payload").path("output").path("payload").path(resultNodeName);
      if (partsNode.isMissingNode() || partsNode.isNull()) {
        logger.warn("No parse result found: traceId={}, message={}, msgData={}", traceId, msgText, msgData);
      }
      else {
        reference.set(partsNode.asText());
      }
    };

    // 调用工作流
    logger.debug("Request juzhi file parse flow start: url={}", url);
    Juzhi2ClientHelper.invokeApiAndWait(traceId, url, params, messageHandler);
    logger.debug("Request juzhi file parse flow end.");
    if (reference.get() == null) {
      throw new BssException("未找到有效的文件解析内容结果");
    }
    return reference.get();
  }

  /**
   * 构造文件解析的参数
   */
  private Map<String, Object> buildFileParseParams(Map<String, Object> startNodeParams, String traceId, String assistantCode) {
    String startNodeId = Juzhi2ClientHelper.getOcrStartNodeId();
    Map<String, Object> payload = ImmutableMap.of("input", ImmutableMap.of(startNodeId, startNodeParams));
    return Juzhi2ClientHelper.buildRequestParams(traceId, assistantCode, payload);
  }


  /**
   * 上传文件到聚智平台
   */
  private String uploadFile(InputStream fileStream, String fileName) {
    return Juzhi2ClientHelper.uploadFile(fileStream, fileName, Juzhi2ClientHelper.getOcrAssistantCode());
  }

}
