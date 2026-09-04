package com.iwhalecloud.bote.doc.module.crawl.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 步骤执行器：爬取抽象类
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
public abstract class AbstractCrawlStep {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final String url;

  protected final CrawlResult result;

  protected AbstractCrawlStep(String url, CrawlResult result) {
    this.url = url;
    this.result = result;
  }

  /**
   * 执行步骤
   *
   * @return 是否跳出
   */
  public boolean execute() {
    ResultVO<String> res = doExecute();
    if (!res.isSuccess()) {
      result.setIsSuccess(false);
      result.setErrorMsg(res.getResultMsg());
      return true;
    }
    result.setIsSuccess(true);
    return false;
  }

  /**
   * 执行步骤，子类实现
   */
  protected abstract ResultVO<String> doExecute();
}
