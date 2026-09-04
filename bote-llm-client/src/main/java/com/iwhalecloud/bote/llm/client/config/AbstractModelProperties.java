package com.iwhalecloud.bote.llm.client.config;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bote.llm.client.dto.ModelConfigInfoDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 大模型客户端配置抽象类
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString
public abstract class AbstractModelProperties {
  /** 模型配置信息，供 LlmClientInterceptor 使用 */
  protected ModelConfigInfoDTO modelConfig;
  /** 接口地址 */
  protected String url;
  /** 密钥 */
  protected String apiKey;
  /** 模型名称 */
  protected String model;
  /** 自定义请求头列表 */
  protected List<HeaderItem> headers;

  public AbstractModelProperties() {
  }

  public AbstractModelProperties(ModelConfigInfoDTO modelConfig, String url, String apiKey, String model, List<HeaderItem> headers) {
    this.modelConfig = modelConfig;
    this.url = url;
    this.apiKey = apiKey;
    this.model = model;
    // 忽略值为空的请求头
    this.headers = CollectionUtils.isEmpty(headers) ? List.of() : headers.stream().filter(h -> StringUtils.isNotEmpty(h.getValue())).toList();
  }
}
