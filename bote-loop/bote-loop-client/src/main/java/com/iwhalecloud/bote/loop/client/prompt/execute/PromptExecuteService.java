package com.iwhalecloud.bote.loop.client.prompt.execute;

import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalRequest;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalResponse;

/**
 * Prompt执行服务接口
 * 对应Thrift: PromptExecuteService
 */
public interface PromptExecuteService {

  /**
   * 内部执行
   * 对应Thrift方法: ExecuteInternal
   */
  ExecuteInternalResponse executeInternal(ExecuteInternalRequest request);
}
