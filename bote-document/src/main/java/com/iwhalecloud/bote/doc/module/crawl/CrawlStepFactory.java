package com.iwhalecloud.bote.doc.module.crawl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.Assert;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.doc.module.crawl.enums.CrawlStepType;
import com.iwhalecloud.bote.doc.module.crawl.step.AbstractCrawlStep;
import com.iwhalecloud.bote.doc.module.crawl.step.CleanHtmlStep;
import com.iwhalecloud.bote.doc.module.crawl.step.FetchRawHtmlStep;
import com.iwhalecloud.bote.doc.module.crawl.step.PostprocessHtmlStep;
import com.iwhalecloud.bote.doc.module.crawl.step.TransformHtmlStep;
import com.iwhalecloud.bote.doc.module.crawl.step.WrapHtmlStep;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 爬取步骤执行器工厂类
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
public final class CrawlStepFactory {
  private CrawlStepFactory() {
  }

  /** 步骤执行器注册中心 */
  private static final Map<CrawlStepType, BiFunction<String, CrawlResult, AbstractCrawlStep>> registry = new HashMap<>(16);

  static {
    // 注册步骤执行器
    registry.put(CrawlStepType.FETCH, FetchRawHtmlStep::new);
    registry.put(CrawlStepType.POSTPROCESS, PostprocessHtmlStep::new);
    registry.put(CrawlStepType.CLEAN, CleanHtmlStep::new);
    registry.put(CrawlStepType.TRANSFORM, TransformHtmlStep::new);
    registry.put(CrawlStepType.WRAP, WrapHtmlStep::new);
  }

  /**
   * 构造步骤执行器实例
   *
   * @param url 页面链接
   * @return 步骤执行器实例
   */
  private static AbstractCrawlStep create(String url, CrawlResult result, CrawlStepType stepType) {
    BiFunction<String, CrawlResult, AbstractCrawlStep> factory = registry.get(stepType);
    if (factory == null) {
      throw new BssException("未知的爬取步骤类型: stepType=" + stepType);
    }
    return factory.apply(url, result);
  }

  /**
   * 爬取 URL 内容，markdown 格式输出
   *
   * @param url 链接
   * @return 结果
   */
  public static ResultVO<CrawlResult> fetchMarkdownContent(String url) {
    // 校验链接的合法性
    url = url.trim();
    Assert.hasText(url, "链接不能为空");
    Assert.isTrue(HttpUtil.isValid(url), "链接不合法");

    List<CrawlStepType> types = Arrays.asList(CrawlStepType.FETCH, CrawlStepType.POSTPROCESS, CrawlStepType.CLEAN, CrawlStepType.TRANSFORM,
      CrawlStepType.WRAP);
    CrawlResult result = new CrawlResult();
    for (CrawlStepType type : types) {
      AbstractCrawlStep runner = create(url, result, type);
      boolean skip = runner.execute();
      if (skip) {
        break;
      }
    }
    if (BooleanUtils.isTrue(result.getIsSuccess())) {
      return ResultVO.success(result);
    }
    else {
      return ResultVO.fail(result.getErrorMsg());
    }
  }
}
