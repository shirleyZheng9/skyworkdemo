package com.iwhalecloud.bote.adapter.juzhi1.client;

import com.iwhalecloud.bote.adapter.juzhi1.config.Juzhi1LlmProperties;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.LlmClientFactory;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.wrapper.LlmClientWrapper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 一级聚智大语言模型客户端工厂类
 *
 * @author bianjp
 * @since 2025-05-17
 */
@Component
@ConditionalOnBooleanProperty("llm.protocol.juzhi1.enabled")
public class Juzhi1LlmClientFactory implements LlmClientFactory {
  @Override
  public int getOrder() {
    return 1000;
  }

  @Override
  public String getProtocolType() {
    return "juzhi1";
  }

  @Override
  public void validate(LlmProperties properties) {
    Juzhi1LlmProperties juzhiProperties = convertProperties(properties);
    validateProperties(juzhiProperties);
  }

  @Override
  public LlmClient create(LlmProperties properties) {
    Juzhi1LlmProperties juzhiProperties = convertProperties(properties);
    validateProperties(juzhiProperties);
    return LlmClientWrapper.wrap(new Juzhi1LlmClient(juzhiProperties));
  }

  /**
   * 校验配置
   */
  private void validateProperties(Juzhi1LlmProperties properties) {
    Assert.hasLength(properties.getModel(), "模型名称不能为空");
    Assert.hasLength(properties.getFuncCode(), "能力编码不能为空");
    Assert.hasLength(properties.getCity(), "地市编码不能为空");
    Assert.hasLength(properties.getApplication(), "应用名称不能为空");
  }

  /**
   * 转换配置
   */
  private Juzhi1LlmProperties convertProperties(LlmProperties properties) {
    return Juzhi1LlmProperties.builder()
      .modelConfig(properties.getModelConfig())
      .funcCode(MapUtils.getString(properties.getExt(), "funcCode"))
      .city(MapUtils.getString(properties.getExt(), "city"))
      .application(MapUtils.getString(properties.getExt(), "application"))
      .model(properties.getModel())
      .temperature(properties.getTemperature())
      .contextLength(properties.getContextLength())
      .build();
  }
}
