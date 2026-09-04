package com.iwhalecloud.bote.dto.model.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 天工 aimarket 模型记录
 *
 * @author jiangm
 * @since 2026-08-06
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AimarketModelRecord {
  private String id;
  private String name;
  private String brief;
  private String logoUrlId;
  private String logoUrl;
  private String modelType;
  private Double paramSpec;
  private String contextWindow;
  private String apiModelId;
  /** 1 表示已上架/可用 */
  private Integer status;
  private List<Map<String, Object>> marketTag;
  private List<Map<String, Object>> modelTag;
  private Object usageGuide;
  private String modelProviderCount;
  private Map<String, Object> modelProductPriceVo;
  private Integer sortOrder;
  private String createTime;
  private String updateTime;
}
