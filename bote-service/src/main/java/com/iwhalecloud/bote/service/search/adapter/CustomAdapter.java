package com.iwhalecloud.bote.service.search.adapter;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.GroovyUtil;
import com.iwhalecloud.bote.dto.search.WebPageInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 自定义 Groovy 脚本适配器
 *
 * @author qian.sisheng
 * @since 2026-01-20
 */
@Component
public class CustomAdapter implements IWebSearchAdapter {

  private static final Logger logger = LoggerFactory.getLogger(CustomAdapter.class);

  @Override
  public String getStrategyType() {
    return BaseConsts.WEB_SEARCH_CUSTOM;
  }

  @Override
  public ResultVO<List<WebPageInfo>> search(String query) {
    try {
      String script = SystemParameter.WEB_SEARCH_SCRIPT.getValueFromDb();
      if (StringUtils.isEmpty(script)) {
        return ResultVO.fail("联网搜索：自定义脚本为空，请联系系统管理员");
      }
      List<WebPageInfo> result = GroovyUtil.invoke(script, null, "search", query);
      return ResultVO.success(result);
    }
    catch (Exception e) {
      logger.error("自定义脚本联网搜索失败: query={}", query, e);
      return ResultVO.fail("联网搜索失败: " + e.getMessage());
    }
  }
}
