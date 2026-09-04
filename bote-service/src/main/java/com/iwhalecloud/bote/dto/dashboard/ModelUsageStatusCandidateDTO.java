package com.iwhalecloud.bote.dto.dashboard;

/** 汇总表模型状态校准项。 */
public class ModelUsageStatusCandidateDTO {
  /** 汇总记录所属租户。 */
  private Long tenantId;
  /** 待校准模型 ID。 */
  private Long modelId;
  /** 当前状态：ACTIVE、DISABLED、WAITING、DELETED 或 UNKNOWN。 */
  private String modelStatus;
  /** 当前来源：PLATFORM、CUSTOM 或 UNKNOWN。 */
  private String modelSource;
  /** 当前可识别的产品系列；无法识别时为空。 */
  private String productType;

  public Long getTenantId() {
    return tenantId;
  }

  public void setTenantId(Long tenantId) {
    this.tenantId = tenantId;
  }

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public String getModelStatus() {
    return modelStatus;
  }

  public void setModelStatus(String modelStatus) {
    this.modelStatus = modelStatus;
  }

  public String getModelSource() {
    return modelSource;
  }

  public void setModelSource(String modelSource) {
    this.modelSource = modelSource;
  }

  public String getProductType() {
    return productType;
  }

  public void setProductType(String productType) {
    this.productType = productType;
  }
}
