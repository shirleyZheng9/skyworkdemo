package com.iwhalecloud.bote.dto.search;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 网页定义
 *
 * @author qian.sisheng
 * @since 2026/01/20
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WebPageInfo {
  /** 主键 */
  private String id;
  /** 标题 */
  private String name;
  /** 访问链接 */
  private String url;
  /** 内容片段 */
  private String snippet;
  /** 内容总结 */
  private String summary;
  /** 网站名称 */
  private String siteName;
  /** 网站图标 */
  private String siteIcon;
  /** 日期 */
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date dateLastCrawled;
}
