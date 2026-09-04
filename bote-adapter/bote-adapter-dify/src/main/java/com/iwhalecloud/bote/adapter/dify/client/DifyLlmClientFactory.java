package com.iwhalecloud.bote.adapter.dify.client;

import com.iwhalecloud.bote.adapter.dify.config.DifyLlmProperties;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.wrapper.LlmClientWrapper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Dify大语言模型客户端工厂类
 *
 * @author qian.sisheng
 * @since 2025-10-17
 */
@Component
@ConditionalOnBooleanProperty("llm.protocol.dify.enabled")
public class DifyLlmClientFactory implements LlmClientFactory {
  @Override
  public String getProtocolType() {
    return "dify";
  }

  @Override
  public void validate(LlmProperties properties) {
    String chatFlowSecret = MapUtils.getString(properties.getExt(), "chatFlowSecret");
    Assert.hasLength(chatFlowSecret, "chatFlowSecret 不能为空");
  }

  @Override
  public LlmClient create(LlmProperties properties) {
    validate(properties);
    DifyLlmProperties difyProperties = DifyLlmProperties.builder()
      .modelConfig(properties.getModelConfig())
      .chatFlowSecret(MapUtils.getString(properties.getExt(), "chatFlowSecret"))
      .build();
    return LlmClientWrapper.wrap(new DifyLlmClient(difyProperties));
  }

  @Override
  public int getOrder() {
    return 3000;
  }
}
