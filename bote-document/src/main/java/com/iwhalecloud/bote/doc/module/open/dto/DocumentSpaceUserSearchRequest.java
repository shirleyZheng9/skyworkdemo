package com.iwhalecloud.bote.doc.module.open.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档所属空间用户搜索请求参数
 *
 * @author Aiqing
 * @since 2025/12/31
 */
@Getter
@Setter
@ToString
public class DocumentSpaceUserSearchRequest {

  @Schema(description = "文档ID")
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;
  @Schema(description = "搜索关键词")
  private String keyword;
}
