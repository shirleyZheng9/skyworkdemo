package com.iwhalecloud.bote.loop.client.prompt.execute.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.OverridePromptParamsDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ScenarioDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableValDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部执行请求DTO
 * 对应Thrift: ExecuteInternalRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "内部执行请求DTO")
public class ExecuteInternalRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "消息列表")
  private List<MessageDTO> messages;

  @Schema(description = "变量值列表")
  private List<VariableValDTO> variableVals;

  @Schema(description = "覆盖提示词参数")
  private OverridePromptParamsDTO overridePromptParams;

  @Schema(description = "场景信息")
  private ScenarioDTO scenario;

  @Schema(description = "基础信息")
  private Base base;
}
