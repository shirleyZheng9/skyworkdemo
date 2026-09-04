package com.iwhalecloud.bote.service.chat;

import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.SearchResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 对话服务
 *
 * @author Admin
 */
public interface IChatService {

  /**
   * 对话补全
   *
   * <p>只支持流式会话</p>
   *
   * @param request 请求参数
   * @param sseEmitter 推送器
   */
  void completions(ChatRequestDTO request, SseEmitter sseEmitter);

  /**
   * AI 门户通用搜索
   *
   * @param params 查询条件
   * @return 结果
   */
  ResultVO<SearchResultDTO> search(SearchQueryParams params);
}
