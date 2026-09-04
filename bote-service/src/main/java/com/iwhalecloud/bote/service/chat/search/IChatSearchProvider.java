package com.iwhalecloud.bote.service.chat.search;

import com.iwhalecloud.bote.dto.chat.SearchResultDTO;
import com.iwhalecloud.bote.dto.chat.query.SearchQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * AI 门户通用搜索
 *
 * @author chen.linfa
 * @since 2025-10-14
 */
public interface IChatSearchProvider {
  /**
   *
   * @param params
   * @return
   */
  ResultVO<SearchResultDTO> search(SearchQueryParams params);
}
