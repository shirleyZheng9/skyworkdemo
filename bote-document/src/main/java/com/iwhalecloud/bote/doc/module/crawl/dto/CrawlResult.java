package com.iwhalecloud.bote.doc.module.crawl.dto;

import java.util.List;

import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 爬取 URL 结果
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString
public class CrawlResult {
  /** 是否成功 */
  private Boolean isSuccess;

  /** 错误信息 */
  private String errorMsg;

  /** 原始內容 */
  private String htmlContent;

  /** 生成的标题 */
  private String title;

  /** 提取的图片链接 */
  private List<String> imageUrls;

  /** 图片类文件资源 */
  private List<FileInfoVO> fileInfos;

  /** 内容（清理后的 HTML） */
  private String content;

  /** Markdown 格式内容 */
  private String markdown;
}

