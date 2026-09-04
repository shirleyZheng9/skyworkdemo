package com.iwhalecloud.bote.loop.data.domain.tag.service.dto;

import com.iwhalecloud.bote.loop.data.domain.tag.entity.TagKey;
import com.iwhalecloud.bote.loop.data.pkg.pagination.PageResult;
import java.util.List;

/**
 * 标签搜索结果DTO
 * 迁移对应关系: Go语言service.ITagService.SearchTags的返回值
 * - 功能: 存储标签搜索结果
 * - 字段定义: 标签键列表和分页结果
 * <p>
 * Java实现说明:
 * - 对应Go的service.ITagService.SearchTags返回值
 * - 使用Java类定义，包含搜索结果字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go切片类型 -> Java列表
 */
public class TagSearchResult {

  private List<TagKey> tagKeys;
  private PageResult pageResult;

  public TagSearchResult() {
  }

  public TagSearchResult(List<TagKey> tagKeys, PageResult pageResult) {
    this.tagKeys = tagKeys;
    this.pageResult = pageResult;
  }

  public List<TagKey> getTagKeys() {
    return tagKeys;
  }

  public void setTagKeys(List<TagKey> tagKeys) {
    this.tagKeys = tagKeys;
  }

  public PageResult getPageResult() {
    return pageResult;
  }

  public void setPageResult(PageResult pageResult) {
    this.pageResult = pageResult;
  }
}
