package com.iwhalecloud.bote.llm.factory;

import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.adapter.OpenAiLlmClient;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.util.ModelValidationUtil;
import com.iwhalecloud.bote.llm.wrapper.LlmClientWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * OpenAI 大语言模型客户端工厂类
 *
 * @author bianjp
 * @since 2025-05-12
 */
@Component
@ConditionalOnBooleanProperty(name = "llm.protocol.openai.enabled", matchIfMissing = true)
public class OpenAiLlmClientFactory implements LlmClientFactory {

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public String getProtocolType() {
    return "openai";
  }

  @Override
  public void validate(LlmProperties properties) {
    ModelValidationUtil.validateUrl(properties.getUrl(), "接口地址");
    Assert.hasLength(properties.getModel(), "模型不能为空");
  }

  @Override
  public LlmClient create(LlmProperties properties) {
    return LlmClientWrapper.wrap(new OpenAiLlmClient(properties));
  }
}
