package com.iwhalecloud.bote.doc.module.open.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查询组织下用户请求参数
 *
 * @author Aiqing
 * @since 2025/12/31
 */
@Getter
@Setter
@ToString
public class DcOrgUserListRequest {

  @Schema(description = "文档ID")
  @NotEmpty(message = "文档ID不能为空")
  private String documentId;
  @NotNull(message = "操作查询的用户ID")
  private Long optUserId;
  @Schema(description = "搜索关键词")
  private Long orgId;
}
