package com.iwhalecloud.bote.loop.client.prompt.debug;

import com.iwhalecloud.bote.loop.client.prompt.debug.dto.DebugStreamingRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.GetDebugContextResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.ListDebugHistoryResponse;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextRequest;
import com.iwhalecloud.bote.loop.client.prompt.debug.dto.SaveDebugContextResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Prompt调试服务接口
 * 对应Thrift: PromptDebugService
 */
public interface PromptDebugService {

  /**
   * 调试流式
   * 对应Thrift方法: DebugStreaming
   */
  SseEmitter debugStreaming(DebugStreamingRequest request);

  /**
   * 保存调试上下文
   * 对应Thrift方法: SaveDebugContext
   */
  SaveDebugContextResponse saveDebugContext(SaveDebugContextRequest request);

  /**
   * 获取调试上下文
   * 对应Thrift方法: GetDebugContext
   */
  GetDebugContextResponse getDebugContext(GetDebugContextRequest request);

  /**
   * 列表调试历史
   * 对应Thrift方法: ListDebugHistory
   */
  ListDebugHistoryResponse listDebugHistory(ListDebugHistoryRequest request);
}
