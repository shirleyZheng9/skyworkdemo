package com.iwhalecloud.bote.llm.client.config;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 嵌入模型配置
 *
 * @author bianjp
 * @since 2024-12-23
 */
@Getter
@Setter
@ToString(callSuper = true)
public class EmbeddingProperties extends AbstractModelProperties {
  /** 单次请求允许的最大文本行数量 */
  private Integer batchSize;

  @Builder
  public EmbeddingProperties(String url, String apiKey, String model, List<HeaderItem> headers) {
    super(null, url, apiKey, model, headers);
  }
}
