package com.iwhalecloud.bote.doc.module.person.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 我的文档查询参数
 *
 * @author yangran
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString
@Schema(description = "我的文档查询参数")
public class MyDocumentQueryParams extends TenantBaseRO {
  @Schema(description = "排序方式：name-按名称，updateTime-按更新时间，默认updateTime")
  private String sortBy = "updateTime";

  @Schema(description = "排序顺序：desc-倒序，asc-正序，默认desc")
  private String sortOrder = "desc";

  @Schema(description = "搜索关键词，支持文档名称模糊搜索")
  private String keyword;

  @JsonIgnore
  private String libraryId;

}
