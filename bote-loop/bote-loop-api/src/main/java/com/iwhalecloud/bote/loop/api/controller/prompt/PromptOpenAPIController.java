package com.iwhalecloud.bote.loop.api.controller.prompt;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.loop.client.prompt.openapi.PromptOpenAPIService;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyRequest;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prompt OpenAPI控制器
 * 对应Thrift: PromptOpenAPIService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "v1/loop/prompts")
@RequiredArgsConstructor
@Tag(name = "提示词：OPENAPI")
public class PromptOpenAPIController {
  private final PromptOpenAPIService promptOpenAPIService;

  /**
   * 批量获取Prompt
   * 对应Thrift方法: BatchGetPromptByPromptKey
   *
   * @param request 批量获取请求
   * @return 批量获取响应
   */
  @PostMapping("/mget")
  public ResultVO<BatchGetPromptByPromptKeyResponse> batchGetPromptByPromptKey(@RequestBody BatchGetPromptByPromptKeyRequest request) {
    return ResultVO.success(promptOpenAPIService.batchGetPromptByPromptKey(request));
  }
}
