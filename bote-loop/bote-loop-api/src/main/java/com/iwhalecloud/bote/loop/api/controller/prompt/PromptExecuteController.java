package com.iwhalecloud.bote.loop.api.controller.prompt;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.prompt.execute.PromptExecuteService;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalRequest;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prompt执行控制器
 * 对应Thrift: PromptExecuteService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/prompt/v1/execute")
@RequiredArgsConstructor
@Tag(name = "提示词：执行")
public class PromptExecuteController {
  private final PromptExecuteService promptExecuteService;

  /**
   * 内部执行
   * 对应Thrift: ExecuteInternal
   */
  @PostMapping("/internal")
  public ResultVO<ExecuteInternalResponse> executeInternal(@RequestBody ExecuteInternalRequest request) {
    return ResultVO.success(promptExecuteService.executeInternal(request));
  }
}
