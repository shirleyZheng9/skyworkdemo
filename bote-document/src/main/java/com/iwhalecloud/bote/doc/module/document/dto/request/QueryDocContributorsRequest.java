package com.iwhalecloud.bote.doc.module.document.dto.request;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询文档贡献者请求
 *
 * @author qian.sisheng
 * @since 2026/03/02
 */
@Getter
@Setter
@ToString
public class QueryDocContributorsRequest extends PageParams {
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "搜索内容")
  private String searchContent;
}
