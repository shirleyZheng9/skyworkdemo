package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表提交请求DTO
 * 对应Thrift: ListCommitRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表提交请求DTO")
public class ListCommitRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "页面大小")
  private Integer pageSize;

  @Schema(description = "页面令牌")
  private String pageToken;

  @Schema(description = "是否正序")
  private Boolean asc;

  @Schema(description = "基础信息")
  private Base base;
}
