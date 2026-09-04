package com.iwhalecloud.bote.loop.client.prompt.openapi;

import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyRequest;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyResponse;

/**
 * Prompt OpenAPI服务接口
 * 对应Thrift: PromptOpenAPIService
 */
public interface PromptOpenAPIService {

  /**
   * 批量获取Prompt
   * 对应Thrift方法: BatchGetPromptByPromptKey
   *
   * @param request 批量获取请求
   * @return 批量获取响应
   */
  BatchGetPromptByPromptKeyResponse batchGetPromptByPromptKey(
    BatchGetPromptByPromptKeyRequest request);
}
