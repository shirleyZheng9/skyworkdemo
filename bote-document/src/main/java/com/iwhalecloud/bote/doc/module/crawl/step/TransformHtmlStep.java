package com.iwhalecloud.bote.doc.module.crawl.step;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.doc.module.crawl.step.transformer.ImageTransformer;
import com.iwhalecloud.bote.doc.module.crawl.step.transformer.MarkdownTransformer;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 步骤执行器: Transformer 处理器
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
public class TransformHtmlStep extends AbstractCrawlStep {

  private static final ImageTransformer imageTransformer = SpringUtil.getBean(ImageTransformer.class);
  private static final MarkdownTransformer markdownTransformer = SpringUtil.getBean(MarkdownTransformer.class);

  public TransformHtmlStep(String url, CrawlResult result) {
    super(url, result);
  }

  @Override
  protected ResultVO<String> doExecute() {
    try {
      // 1. 将 HTML 转换为 Markdown
      String markdown = markdownTransformer.execute(result.getContent(), url);

      // 2. 将图片转换为 base64（传递原始 URL 用于设置 Referer）
      // TODO 有些图片下载不了 403，下载不了的保留原来的图片地址
       markdown = imageTransformer.execute(markdown, result, url);

      // 3. LLM 优化 Markdown 内容 TODO

      result.setMarkdown(markdown);
      return ResultVO.success();
    }
    catch (Exception e) {
      logger.error("执行 Transformer 发生异常", e);
      return ResultVO.fail("执行 Transformer 发生异常: " + ExpUtil.getMsg(e));
    }
  }
}
