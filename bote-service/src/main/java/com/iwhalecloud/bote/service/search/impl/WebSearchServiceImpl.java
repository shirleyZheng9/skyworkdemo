package com.iwhalecloud.bote.service.search.impl;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bote.service.search.IWebSearchService;
import com.iwhalecloud.bote.service.search.adapter.IWebSearchAdapter;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

/**
 * 联网搜索服务实现
 *
 * @author chen.linfa
 * @since 2025-03-14
 */
@Service
public class WebSearchServiceImpl implements IWebSearchService {

  private final Map<String, IWebSearchAdapter> adapterMap;

  public WebSearchServiceImpl(List<IWebSearchAdapter> adapters) {
    this.adapterMap = adapters.stream()
      .collect(Collectors.toMap(IWebSearchAdapter::getStrategyType, Function.identity()));
  }

  @Override
  public ResultVO<List<WebPageInfo>> run(String query) {
    // 先检查开关
    if (BooleanUtils.isNotTrue(SystemParameter.WEB_SEARCH_ENABLED.getBooleanValueFromDb())) {
      return ResultVO.fail("联网搜索: 联网搜索未开启，请联系系统管理员");
    }
    String strategy = SystemParameter.WEB_SEARCH_STRATEGY.getValueFromDb();
    IWebSearchAdapter adapter = adapterMap.get(strategy);
    if (adapter == null) {
      return ResultVO.fail("未知联网搜索策略：" + strategy);
    }
    return adapter.search(query);
  }
}
