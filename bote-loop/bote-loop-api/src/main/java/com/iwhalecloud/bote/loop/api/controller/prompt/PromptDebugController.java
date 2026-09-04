package com.iwhalecloud.bote.loop.api.controller.prompt;

import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.prompt.debug.PromptDebugService;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.DebugStreamingRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Prompt调试控制器
 * 对应Thrift: PromptDebugService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/prompt/v1/prompts")
@RequiredArgsConstructor
@Tag(name = "提示词：调试")
public class PromptDebugController {
  private final PromptDebugService promptDebugService;

  /**
   * 调试流式
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/debug_streaming
   */
  @PostMapping(value = "/{promptId}/debug_streaming",
    produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @IgnoreSign
  public SseEmitter debugStreaming(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody DebugStreamingRequest request) {
    return promptDebugService.debugStreaming(request);
  }

  /**
   * 保存调试上下文
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/debug_context/save
   */
  @PostMapping("/{promptId}/debug_context/save")
  public ResultVO<SaveDebugContextResponse> saveDebugContext(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody SaveDebugContextRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptDebugService.saveDebugContext(request));
  }

  /**
   * 获取调试上下文
   * 对应Thrift: GET /api/prompt/v1/prompts/:prompt_id/debug_context/get
   */
  @GetMapping("/{promptId}/debug_context/get")
  public ResultVO<GetDebugContextResponse> getDebugContext(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestParam(name = "workspaceId") @Schema(description = "工作空间ID") Long workspaceId) {

    GetDebugContextRequest request = GetDebugContextRequest.builder()
      .promptId(promptId)
      .workspaceId(workspaceId)
      .build();

    return ResultVO.success(promptDebugService.getDebugContext(request));
  }

  /**
   * 列表调试历史
   * 对应Thrift: GET /api/prompt/v1/prompts/:prompt_id/debug_history/list
   */
  @GetMapping("/{promptId}/debug_history/list")
  public ResultVO<ListDebugHistoryResponse> listDebugHistory(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestParam("workspaceId") @Schema(description = "工作空间ID") Long workspaceId,
    @RequestParam("daysLimit") @Schema(description = "天数限制") Integer daysLimit,
    @RequestParam("pageSize") @Schema(description = "页面大小") Integer pageSize,
    @RequestParam(required = false) @Schema(description = "页面令牌") String pageToken) {

    ListDebugHistoryRequest request = ListDebugHistoryRequest.builder()
      .promptId(promptId)
      .workspaceId(workspaceId)
      .daysLimit(daysLimit)
      .pageSize(pageSize)
      .pageToken(pageToken)
      .build();

    return ResultVO.success(promptDebugService.listDebugHistory(request));
  }
}
