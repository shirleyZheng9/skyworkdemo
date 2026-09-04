package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 联网搜索适配器接口
 *
 * @author qian.sisheng
 * @since 2026-01-21
 */
public interface IWebSearchAdapter {
  /**
   * 执行搜索
   *
   * @param query 查询内容
   * @return 搜索结果
   */
  ResultVO<List<WebPageInfo>> search(String query);

  /**
   * 获取策略类型
   *
   * @return 策略类型标识
   */
  String getStrategyType();
}
