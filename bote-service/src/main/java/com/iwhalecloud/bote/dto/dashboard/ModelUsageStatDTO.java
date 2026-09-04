package com.iwhalecloud.bote.dto.dashboard;

/** 模型累计使用量查询结果。 */
public class ModelUsageStatDTO {
  private Long modelId;
  private String modelName;
  private String productType;
  private Long count;
  /** 1 表示已从模型主表识别出产品系列；0 表示应合并到“自建/其他”。 */
  private Integer separatelyDisplayable;

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getProductType() {
    return productType;
  }

  public void setProductType(String productType) {
    this.productType = productType;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public Integer getSeparatelyDisplayable() {
    return separatelyDisplayable;
  }

  public void setSeparatelyDisplayable(Integer separatelyDisplayable) {
    this.separatelyDisplayable = separatelyDisplayable;
  }
}
