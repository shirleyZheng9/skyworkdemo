package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CommitInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.user.UserInfoDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表提交响应DTO
 * 对应Thrift: ListCommitResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListCommitResponse {

  @Schema(description = "提交信息列表")
  private List<CommitInfoDTO> promptCommitInfos;

  @Schema(description = "用户列表")
  private List<UserInfoDetailDTO> users;

  @Schema(description = "是否还有更多")
  private Boolean hasMore;

  @Schema(description = "下一页令牌")
  private String nextPageToken;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
