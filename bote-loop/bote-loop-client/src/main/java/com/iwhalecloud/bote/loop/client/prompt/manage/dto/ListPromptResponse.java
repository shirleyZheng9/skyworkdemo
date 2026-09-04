package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.user.UserInfoDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表Prompt响应DTO
 * 对应Thrift: ListPromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表Prompt响应DTO")
public class ListPromptResponse {

  @Schema(description = "提示词列表")
  private List<PromptDTO> prompts;

  @Schema(description = "用户列表")
  private List<UserInfoDetailDTO> users;

  @Schema(description = "总数")
  private Long total;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
