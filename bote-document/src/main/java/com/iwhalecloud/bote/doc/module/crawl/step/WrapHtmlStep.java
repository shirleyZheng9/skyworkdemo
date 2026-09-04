package com.iwhalecloud.bote.doc.module.crawl.step;

import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 步骤执行器：封装处理结果
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
public class WrapHtmlStep extends AbstractCrawlStep {

  public WrapHtmlStep(String url, CrawlResult result) {
    super(url, result);
  }

  @Override
  protected ResultVO<String> doExecute() {
    // 清理临时变量
    result.setHtmlContent(null);
    result.setContent(null);
    return ResultVO.success();
  }

}
