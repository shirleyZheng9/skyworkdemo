package com.iwhalecloud.bote.adapter.juzhi2.client;

import com.iwhalecloud.bote.adapter.juzhi2.config.Juzhi2LlmProperties;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.wrapper.LlmClientWrapper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 二级聚智大语言模型客户端工厂类
 *
 * @author bianjp
 * @since 2025-05-12
 */
@Component
@ConditionalOnBooleanProperty("llm.protocol.juzhi2.enabled")
public class Juzhi2LlmClientFactory implements LlmClientFactory {
  @Override
  public int getOrder() {
    return 2000;
  }

  @Override
  public String getProtocolType() {
    return "juzhi2";
  }

  @Override
  public void validate(LlmProperties properties) {
    String assistantCode = MapUtils.getString(properties.getExt(), "assistantCode");
    String startNodeId = MapUtils.getString(properties.getExt(), "startNodeId");
    Assert.hasLength(assistantCode, "assistantCode 不能为空");
    Assert.hasLength(startNodeId, "startNodeId 不能为空");
  }

  @Override
  public LlmClient create(LlmProperties properties) {
    validate(properties);
    Juzhi2LlmProperties juzhiProperties = Juzhi2LlmProperties.builder()
      .modelConfig(properties.getModelConfig())
      .assistantCode(MapUtils.getString(properties.getExt(), "assistantCode"))
      .startNodeId(MapUtils.getString(properties.getExt(), "startNodeId"))
      .contextLength(properties.getContextLength())
      .build();
    return LlmClientWrapper.wrap(new Juzhi2LlmClient(juzhiProperties));
  }
}
