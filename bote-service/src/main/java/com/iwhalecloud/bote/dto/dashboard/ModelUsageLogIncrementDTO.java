package com.iwhalecloud.bote.dto.dashboard;

import java.util.Date;

/** 汇总任务一次读取的轻量模型调用日志，不包含请求、响应等大字段。 */
public class ModelUsageLogIncrementDTO {
  /** 日志主键，同时作为同一入库时间内的稳定排序字段。 */
  private Long logId;
  /** 调用发生的租户，用于数据隔离和汇总表联合主键。 */
  private Long tenantId;
  /** 实际被调用的模型 ID。 */
  private Long modelId;
  /** 调用发生时的模型名称快照，模型删除后仍可用于审计。 */
  private String modelName;
  /** 定时汇总时从模型主表解析出的产品系列快照。 */
  private String productType;
  /** 日志实际入库时间，作为增量消费游标。 */
  private Date createdTime;

  public Long getLogId() {
    return logId;
  }

  public void setLogId(Long logId) {
    this.logId = logId;
  }

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

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }
}
