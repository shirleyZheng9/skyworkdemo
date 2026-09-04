package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交草稿响应DTO
 * 对应Thrift: CommitDraftResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitDraftResponse {
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
