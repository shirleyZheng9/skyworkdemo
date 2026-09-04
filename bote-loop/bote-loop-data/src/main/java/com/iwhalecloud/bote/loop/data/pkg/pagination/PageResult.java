package com.iwhalecloud.bote.loop.data.pkg.pagination;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分页结果DTO
 * 迁移对应关系: Go语言pagination.PageResult
 * - 功能: 存储分页查询结果
 * - 字段定义: 分页信息
 * <p>
 * Java实现说明:
 * - 对应Go的pagination.PageResult结构体
 * - 使用Java类定义，包含分页结果字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go基本类型 -> Java基本类型
 */
public class PageResult {

  @JsonProperty("cursor")
  private String cursor;

  @JsonProperty("has_next")
  private Boolean hasNext;

  @JsonProperty("total")
  private Long total;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_num")
  private Integer pageNum;

  public PageResult() {
  }

  public PageResult(String cursor, Boolean hasNext, Long total, Integer pageSize, Integer pageNum) {
    this.cursor = cursor;
    this.hasNext = hasNext;
    this.total = total;
    this.pageSize = pageSize;
    this.pageNum = pageNum;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public Boolean getHasNext() {
    return hasNext;
  }

  public void setHasNext(Boolean hasNext) {
    this.hasNext = hasNext;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public Integer getPageNum() {
    return pageNum;
  }

  public void setPageNum(Integer pageNum) {
    this.pageNum = pageNum;
  }
}
