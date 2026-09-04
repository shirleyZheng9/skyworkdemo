package com.iwhalecloud.bote.adapter.juzhi2.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.adapter.juzhi2.helper.Juzhi2ClientHelper;
import com.iwhalecloud.bote.service.external.JuzhiDictClient;
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
 * 二级聚智 dict 项目团队专用的能力客户端
 *
 * @author wangtingyun
 * @since 2025-07-16
 */
@Component
@ConditionalOnBooleanProperty("juzhi2.dict.enabled")
public class Juzhi2DictClient implements JuzhiDictClient {

  private static final Logger logger = LoggerFactory.getLogger(Juzhi2DictClient.class);

  @Override
  public String parseFileContent(InputStream fileStream, String fileName) {
    // 上传文件
    String fileContentId = Juzhi2ClientHelper.uploadFile(fileStream, fileName, Juzhi2ClientHelper.getDictFileParseAssistantCode());

    // 获取调用工作流相关参数
    String assistantCode = Juzhi2ClientHelper.getDictFileParseAssistantCode();
    HttpUrl url = Juzhi2ClientHelper.signUrl(assistantCode);
    String traceId = Juzhi2ClientHelper.newTraceId();
    Map<String, Object> params = buildFileParseParams(traceId, assistantCode, fileContentId, fileName);

    // 接收工作流结果
    AtomicReference<String> reference = new AtomicReference<>();

    // on_message 回调处理器
    BiConsumer<String, JsonNode> messageHandler = (msgText, msgData) -> {
      JsonNode partsNode = msgData.path("payload").path("output").path("payload").path("fileContent");
      if (partsNode.isMissingNode() || partsNode.isNull()) {
        logger.warn("No parse result found: traceId={}, message={}, msgData={}", traceId, msgText, msgData);
      }
      else {
        reference.set(partsNode.asText());
      }
    };

    // 调用工作流
    logger.debug("Request juzhi dict file parse flow start: url={}", url);
    Juzhi2ClientHelper.invokeApiAndWait(traceId, url, params, messageHandler);
    logger.debug("Request juzhi dict file parse flow end.");
    if (reference.get() == null) {
      throw new BssException("未找到有效的文件解析内容结果");
    }
    return reference.get();
  }

  /**
   * 构造文件解析的参数
   */
  private Map<String, Object> buildFileParseParams(String traceId, String assistantCode, String fileContentId, String fileName) {
    Map<String, Object> startNodeParams = new HashMap<>();
    startNodeParams.put("file", fileContentId);
    startNodeParams.put("fileName", fileName);
    String startNodeId = Juzhi2ClientHelper.getDictFileParseStartNodeId();
    Map<String, Object> payload = ImmutableMap.of("input", ImmutableMap.of(startNodeId, startNodeParams));
    return Juzhi2ClientHelper.buildRequestParams(traceId, assistantCode, payload);
  }

}
