package com.iwhalecloud.bote.service.search;

import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 联网搜索服务
 *
 * @author chen.linfa
 * @since 2025-03-14
 */
public interface IWebSearchService {
  /**
   * 联网搜索
   *
   * @param query 查询内容
   * @return 搜索结果
   */
  ResultVO<List<WebPageInfo>> run(String query);
}
