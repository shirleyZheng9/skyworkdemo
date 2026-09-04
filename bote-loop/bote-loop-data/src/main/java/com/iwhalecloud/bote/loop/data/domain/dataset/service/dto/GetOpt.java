package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

/**
 * 获取选项DTO
 * 迁移对应关系: Go语言service.GetOpt
 * - 功能: 获取数据时的选项配置
 * - 字段定义: 各种获取选项
 * <p>
 * Java实现说明:
 * - 对应Go的service.GetOpt结构体
 * - 使用Java类定义，包含获取选项字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go基本类型 -> Java基本类型
 */
public class GetOpt {

  private Boolean includeSchema;
  private Boolean includeItems;
  private Boolean includeVersions;
  private Integer limit;
  private Integer offset;

  public GetOpt() {
  }

  public GetOpt(Boolean includeSchema, Boolean includeItems, Boolean includeVersions, Integer limit, Integer offset) {
    this.includeSchema = includeSchema;
    this.includeItems = includeItems;
    this.includeVersions = includeVersions;
    this.limit = limit;
    this.offset = offset;
  }

  public Boolean getIncludeSchema() {
    return includeSchema;
  }

  public void setIncludeSchema(Boolean includeSchema) {
    this.includeSchema = includeSchema;
  }

  public Boolean getIncludeItems() {
    return includeItems;
  }

  public void setIncludeItems(Boolean includeItems) {
    this.includeItems = includeItems;
  }

  public Boolean getIncludeVersions() {
    return includeVersions;
  }

  public void setIncludeVersions(Boolean includeVersions) {
    this.includeVersions = includeVersions;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }
}
