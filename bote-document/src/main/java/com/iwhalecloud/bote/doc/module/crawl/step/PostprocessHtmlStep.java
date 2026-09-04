package com.iwhalecloud.bote.doc.module.crawl.step;

import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 步骤执行器：后处理
 * <p>运行后处理器来针对特定网站进行优化</p>
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
public class PostprocessHtmlStep extends AbstractCrawlStep {

  public PostprocessHtmlStep(String url, CrawlResult result) {
    super(url, result);
  }

  @Override
  protected ResultVO<String> doExecute() {
    /**
     * ### 1.1 YouTube 后处理器 TODO
     *
     * 位置：`apps/api/src/scraper/scrapeURL/postprocessors/youtube.ts`
     *
     * 功能：
     * - 检测 YouTube 视频 URL
     * - 提取视频标题、描述、字幕等元数据
     * - 如果可用，提取视频字幕内容（`youtubeTranscriptContent`）
     *
     * 触发条件：
     * - URL 匹配 YouTube 域名（youtube.com, youtu.be, m.youtube.com）
     */

    return ResultVO.success();
  }

}
