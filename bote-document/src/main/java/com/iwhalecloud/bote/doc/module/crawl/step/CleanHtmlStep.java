package com.iwhalecloud.bote.doc.module.crawl.step;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.doc.module.crawl.dto.CandidateMetricsDTO;
import com.iwhalecloud.bote.doc.module.crawl.dto.CrawlResult;
import com.iwhalecloud.bote.doc.module.crawl.step.helper.ProcessArticleHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.SpringUtil;

/**
 * 步骤执行器: HTML 清理和转换
 *
 * @author chen.linfa
 * @since 2026-01-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class CleanHtmlStep extends AbstractCrawlStep {

  /** 需要删除的标签和元素（广告、导航、无关内容） */
  private static final String DEFAULT_REMOVE_ELEMENTS = "script, style, iframe, nav, footer, aside, form, input, button, select, textarea, "
    + ".advertisement, .ads, .ad, .ad-banner, .ad-container, .ad-wrapper, .ad-box, "
    + ".sidebar, .side-bar, .sidebox, .menu, .navigation, .nav, .navbar, .nav-menu, " + ".breadcrumb, .breadcrumbs, .pagination, .page-nav, .pager, "
    + ".comment, .comments, .comment-box, .comment-list, .comment-area, " + ".social-share, .share, .share-box, .share-buttons, .social-media, "
    + ".related, .recommend, .recommendation, .related-articles, .related-posts, " + ".hot, .popular, .trending, .most-viewed, "
    + ".tags, .tag-list, .tag-cloud, .tag-box, " + ".author-info, .author-box, .author-card, .author-profile, "
    + ".meta, .meta-info, .post-meta, .article-meta, " + ".header, .site-header, .page-header, " + ".widget, .widget-box, .widget-area, "
    + ".newsletter, .subscribe, .subscription, " + ".cookie-notice, .cookie-banner, .cookie-consent, " + ".popup, .modal, .overlay, .lightbox, "
    + "[hidden], [style*='display:none'], [style*='display: none'], [style*='visibility:hidden']";

  private static final ProcessArticleHelper articleHelper = SpringUtil.getBean(ProcessArticleHelper.class);

  public CleanHtmlStep(String url, CrawlResult result) {
    super(url, result);
  }

  @Override
  protected ResultVO<String> doExecute() {
    String content = result.getHtmlContent();
    try {
      Document doc = Jsoup.parse(content, url);
      // 尝试提取文档标题
      extractTitle(doc);
      // 先移除所有明显不需要的元素
      removeUnwantedElements(doc);

      // 提取并处理文章内容
      doc = articleHelper.extractAndProcessArticle(doc, url);

      // 清理文档
      cleanDocument(doc);

      // 处理图片和链接
      String baseDomain = extractBaseDomain(url);
      String baseDirUrl = extractBaseDirUrl(url);
      processImages(doc, baseDomain, baseDirUrl);
      processLinks(doc, baseDomain, baseDirUrl);

      // 包装并返回
      result.setContent(wrapBodyContent(doc));
    }
    catch (Exception e) {
      logger.warn("清理HTML内容时发生异常，{}", e.getMessage(), e);
      return ResultVO.fail("清理HTML内容时发生异常，" + ExpUtil.getMsg(e));
    }
    return ResultVO.success();
  }

  private void extractTitle(Document doc) {
    String title = doc.title();
    if (StringUtils.isNotBlank(title)) {
      // 清理标题，移除特殊字符
      title = title.trim().replaceAll("[\\r\\n\\t]", " ").replaceAll("\\s+", " ");
      if (title.length() > 100) {
        title = title.substring(0, 100) + "...";
      }
      result.setTitle(title);
    }
  }

  /**
   * 包装body内容
   *
   * @param doc 文档对象
   * @return 包装后的HTML字符串
   */
  private String wrapBodyContent(Document doc) {
    Element body = doc.body();
    if (body != null) {
      String bodyHtml = body.html();
      return
        "<div style=\"max-width: 800px; margin: 0 auto; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif; font-size: 14px; line-height: 1.6; color: #333;\">"
          + bodyHtml + "</div>";
    }
    return doc.html();
  }

  /**
   * 处理图片
   *
   * @param doc 文档对象
   * @param baseDomain 基础域名
   * @param baseDirUrl 基础目录URL
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void processImages(Document doc, String baseDomain, String baseDirUrl) {
    // 提取图片类型的 URL，方便后续处理
    List<String> imageUrls = new ArrayList<>();
    result.setImageUrls(imageUrls);
    Elements images = doc.select("img");

    // 需要移除的隐藏图片列表
    List<Element> imagesToRemove = new ArrayList<>();

    for (Element img : images) {
      if (isHiddenImage(img)) {
        imagesToRemove.add(img);
        continue;
      }

      String src = extractImageSrc(img);
      if (StringUtils.isBlank(src)) {
        // 如果所有属性都为空，记录警告但不移除图片（可能是占位符）
        if (logger.isDebugEnabled()) {
          logger.debug("图片元素没有有效的 src 属性: {}", img.outerHtml().length() > 100 ? img.outerHtml().substring(0, 100) + "..." : img.outerHtml());
        }
        continue;
      }

      String normalizedSrc = normalizeImageSrcAndSetAttr(img, src, baseDomain, baseDirUrl);
      if (StringUtils.isBlank(normalizedSrc)) {
        continue;
      }

      imageUrls.add(normalizedSrc);
      preserveAndEnhanceImageStyle(img);
    }

    // 移除隐藏的图片
    for (Element img : imagesToRemove) {
      img.remove();
    }

    logger.debug("提取到 {} 张图片", imageUrls.size());
  }

  /**
   * 提取图片地址（兼容懒加载：data-src / data-lazy-src / data-original）。
   */
  private String extractImageSrc(Element img) {
    String src = img.attr("data-src");
    if (StringUtils.isBlank(src)) {
      src = img.attr("src");
    }
    if (StringUtils.isBlank(src)) {
      src = img.attr("data-lazy-src");
    }
    if (StringUtils.isBlank(src)) {
      src = img.attr("data-original");
    }
    return src;
  }

  /**
   * 规范化图片地址，并回写到 img 的 src 属性。
   */
  private String normalizeImageSrcAndSetAttr(Element img, String src, String baseDomain, String baseDirUrl) {
    if (StringUtils.isBlank(src)) {
      return null;
    }
    // data URI：直接使用
    if (src.startsWith("data:")) {
      ensureImgSrc(img, src);
      return src;
    }

    String normalized = src;
    if (!isAbsoluteUrl(normalized)) {
      String absoluteUrl = resolveUrl(normalized, baseDomain, baseDirUrl);
      if (StringUtils.isNotBlank(absoluteUrl)) {
        normalized = absoluteUrl;
      }
    }
    ensureImgSrc(img, normalized);
    return normalized;
  }

  private void ensureImgSrc(Element img, String src) {
    if (img == null || StringUtils.isBlank(src)) {
      return;
    }
    // 兼容：如果原来使用的是 data-src，需要更新到 src 属性
    if (img.hasAttr("data-src") || !img.hasAttr("src") || !src.equals(img.attr("src"))) {
      img.attr("src", src);
    }
  }

  /**
   * 保留图片的原始尺寸样式，并添加响应式样式
   *
   * @param img 图片元素
   */
  private void preserveAndEnhanceImageStyle(Element img) {
    if (img == null) {
      return;
    }
    String originalWidth = img.attr("width");
    String originalHeight = img.attr("height");
    String originalStyle = img.attr("style");
    String merged = buildEnhancedImageStyle(originalStyle, originalWidth, originalHeight);
    img.attr("style", merged);
  }

  private String buildEnhancedImageStyle(String originalStyle, String width, String height) {
    String lower = StringUtils.defaultString(originalStyle).toLowerCase();
    StringBuilder sb = new StringBuilder();

    appendExistingStyle(sb, originalStyle);
    appendSizeFromAttrIfMissing(sb, "width", width, lower);
    appendSizeFromAttrIfMissing(sb, "height", height, lower);
    appendResponsiveDefaults(sb, height, lower);
    return sb.toString();
  }

  private void appendExistingStyle(StringBuilder sb, String originalStyle) {
    if (sb == null || StringUtils.isBlank(originalStyle)) {
      return;
    }
    String cleaned = stripConflictingImageStyles(originalStyle);
    if (StringUtils.isBlank(cleaned)) {
      return;
    }
    sb.append(cleaned);
    if (!cleaned.endsWith(";")) {
      sb.append("; ");
    }
    else {
      sb.append(" ");
    }
  }

  private String stripConflictingImageStyles(String style) {
    if (StringUtils.isBlank(style)) {
      return "";
    }
    return style
      .replaceAll("(?i)display\\s*:\\s*[^;]+;?", "")
      .replaceAll("(?i)max-width\\s*:\\s*[^;]+;?", "")
      .replaceAll("(?i)height\\s*:\\s*auto;?", "")
      .trim();
  }

  private void appendSizeFromAttrIfMissing(StringBuilder sb, String prop, String val, String lowerStyle) {
    if (sb == null || StringUtils.isBlank(val) || StringUtils.isBlank(prop)) {
      return;
    }
    if (StringUtils.isNotBlank(lowerStyle) && lowerStyle.contains(prop)) {
      return;
    }
    sb.append(prop).append(": ").append(val);
    if (val.matches("\\d+")) {
      sb.append("px");
    }
    sb.append("; ");
  }

  private void appendResponsiveDefaults(StringBuilder sb, String heightAttr, String lowerStyle) {
    if (sb == null) {
      return;
    }
    sb.append("max-width: 100%; ");
    boolean hasHeightInStyle = StringUtils.isNotBlank(lowerStyle) && lowerStyle.contains("height");
    if (StringUtils.isBlank(heightAttr) && !hasHeightInStyle) {
      sb.append("height: auto; ");
    }
    sb.append("display: block; margin: 10px auto;");
  }

  /**
   * 检查图片是否是隐藏的
   *
   * @param img 图片元素
   * @return 如果是隐藏的图片返回 true，否则返回 false
   */
  private boolean isHiddenImage(Element img) {
    if (img == null) {
      return false;
    }
    String style = img.attr("style");
    return hasHiddenAttribute(img)
      || hasHiddenInlineStyle(style)
      || hasHiddenAncestorStyle(img)
      || isTinyPlaceholderByAttributes(img)
      || isTinyPlaceholderByStyle(style);
  }

  private boolean hasHiddenAttribute(Element img) {
    return img != null && img.hasAttr("hidden");
  }

  private boolean hasHiddenInlineStyle(String style) {
    if (StringUtils.isBlank(style)) {
      return false;
    }
    String s = style.toLowerCase();
    return s.contains("display:none")
      || s.contains("display: none")
      || s.contains("visibility:hidden")
      || s.contains("visibility: hidden")
      || s.contains("opacity:0")
      || s.contains("opacity: 0")
      || s.contains("opacity:0.0")
      || s.contains("opacity: 0.0");
  }

  private boolean hasHiddenAncestorStyle(Element img) {
    if (img == null) {
      return false;
    }
    Element parent = img.parent();
    while (parent != null && !"body".equals(parent.tagName())) {
      String parentStyle = parent.attr("style");
      if (StringUtils.isNotBlank(parentStyle)) {
        String s = parentStyle.toLowerCase();
        if (s.contains("display:none") || s.contains("display: none") || s.contains("visibility:hidden") || s.contains("visibility: hidden")) {
          return true;
        }
      }
      parent = parent.parent();
    }
    return false;
  }

  private boolean isTinyPlaceholderByAttributes(Element img) {
    if (img == null) {
      return false;
    }
    String width = img.attr("width");
    String height = img.attr("height");
    if (StringUtils.isBlank(width) || StringUtils.isBlank(height)) {
      return false;
    }
    try {
      int w = Integer.parseInt(width.trim());
      int h = Integer.parseInt(height.trim());
      return w <= 1 && h <= 1;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean isTinyPlaceholderByStyle(String style) {
    if (StringUtils.isBlank(style)) {
      return false;
    }
    String s = style.toLowerCase();
    boolean tinyWidth = s.contains("width:1px") || s.contains("width: 1px") || s.contains("width:0px") || s.contains("width: 0px");
    boolean tinyHeight = s.contains("height:1px") || s.contains("height: 1px") || s.contains("height:0px") || s.contains("height: 0px");
    return tinyWidth && tinyHeight;
  }

  /**
   * 处理链接
   *
   * @param doc 文档对象
   * @param baseDomain 基础域名
   * @param baseDirUrl 基础目录URL
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void processLinks(Document doc, String baseDomain, String baseDirUrl) {
    Elements links = doc.select("a[href]");
    for (Element link : links) {
      String href = link.attr("href");
      if (StringUtils.isNotBlank(href) && !isAbsoluteUrl(href) && !href.startsWith("#")) {
        String absoluteUrl = resolveUrl(href, baseDomain, baseDirUrl);
        if (absoluteUrl != null) {
          link.attr("href", absoluteUrl);
        }
      }
      String existingStyle = link.attr("style");
      String linkStyle = "color: #0066cc; text-decoration: underline;";
      link.attr("style", StringUtils.isNotBlank(existingStyle) ? existingStyle + " " + linkStyle : linkStyle);
    }
  }

  /**
   * 检查是否是绝对 URL
   *
   * @param url URL 字符串
   * @return 如果是绝对 URL 返回 true，否则返回 false
   */
  private boolean isAbsoluteUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return false;
    }
    return isHttpAbsoluteUrl(url) || isProtocolRelativeUrl(url) || isDomainOnlyUrl(url);
  }

  private boolean isHttpAbsoluteUrl(String url) {
    return url.startsWith("http://") || url.startsWith("https://");
  }

  private boolean isProtocolRelativeUrl(String url) {
    return url.startsWith("//");
  }

  /**
   * 形如：i2.chinanews.com.cn/path/to/image.jpg（缺少协议但看起来像域名开头）
   */
  private boolean isDomainOnlyUrl(String url) {
    if (url.startsWith("/") || !url.contains(".") || !url.contains("/")) {
      return false;
    }
    int firstSlash = url.indexOf('/');
    if (firstSlash <= 0) {
      return false;
    }
    String domainPart = url.substring(0, firstSlash);
    if (!domainPart.contains(".") || domainPart.length() <= 3) {
      return false;
    }
    return domainPart.matches("^[a-zA-Z0-9][a-zA-Z0-9.-]*[a-zA-Z0-9]$");
  }

  /**
   * 解析URL，将相对路径转换为绝对路径
   *
   * @param relativeOrAbsolutePath 待解析的URL（可能是相对路径或以/开头的绝对路径）
   * @param baseDomain 基础域名（协议+域名+端口，不含路径）
   * @param baseDirUrl 基础目录URL（协议+域名+目录路径，不含文件名）
   * @return 解析后的绝对URL
   */
  private String resolveUrl(String relativeOrAbsolutePath, String baseDomain, String baseDirUrl) {
    if (StringUtils.isBlank(relativeOrAbsolutePath)) {
      return null;
    }

    if (relativeOrAbsolutePath.startsWith("data:")) {
      return relativeOrAbsolutePath;
    }

    if (isAbsoluteUrl(relativeOrAbsolutePath)) {
      return normalizeAlreadyAbsoluteUrl(relativeOrAbsolutePath, baseDirUrl);
    }

    if (relativeOrAbsolutePath.startsWith("/")) {
      return joinBaseDomain(baseDomain, relativeOrAbsolutePath);
    }

    return resolveRelativeUrl(baseDirUrl, relativeOrAbsolutePath);
  }

  private String normalizeAlreadyAbsoluteUrl(String url, String baseDirUrl) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    if (isProtocolRelativeUrl(url)) {
      return addSchemeForProtocolRelative(url, baseDirUrl);
    }
    if (!isHttpAbsoluteUrl(url) && isDomainOnlyUrl(url)) {
      return "https://" + url;
    }
    return url;
  }

  private String addSchemeForProtocolRelative(String url, String baseDirUrl) {
    try {
      URI baseUri = new URI(baseDirUrl);
      String scheme = baseUri.getScheme();
      if (StringUtils.isNotBlank(scheme)) {
        return scheme + ":" + url;
      }
    }
    catch (URISyntaxException e) {
      logger.warn("解析协议相对 URL 失败: {}", url, e);
    }
    return url;
  }

  private String joinBaseDomain(String baseDomain, String absolutePath) {
    String cleanBaseDomain = stripTrailingSlash(baseDomain);
    return cleanBaseDomain + absolutePath;
  }

  private String stripTrailingSlash(String s) {
    if (StringUtils.isBlank(s)) {
      return "";
    }
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }

  private String resolveRelativeUrl(String baseDirUrl, String relativePath) {
    try {
      URI resolvedUri = new URI(baseDirUrl).resolve(relativePath);
      return normalizeUrlSlashes(resolvedUri.normalize().toString());
    }
    catch (URISyntaxException e) {
      logger.warn("解析相对URL失败: {}, 使用baseDirUrl拼接", relativePath, e);
      String cleanBaseDirUrl = ensureTrailingSlash(baseDirUrl);
      return normalizeUrlSlashes(cleanBaseDirUrl + relativePath);
    }
  }

  private String ensureTrailingSlash(String s) {
    if (StringUtils.isBlank(s)) {
      return "";
    }
    return s.endsWith("/") ? s : s + "/";
  }

  private String normalizeUrlSlashes(String url) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    // 规范化URL，去除多余的斜杠（但保留协议部分的//）
    return url.replaceAll("([^:])//+", "$1/");
  }

  /**
   * 从URL中提取协议和域名部分（例如：https://www.example.com:8080）
   *
   * @param url 完整的URL
   * @return 协议和域名部分
   */
  private String extractBaseDomain(String url) {
    try {
      URI uri = new URI(url);
      return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
    }
    catch (URISyntaxException e) {
      logger.warn("提取基础域名失败: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 从URL中提取协议、域名和目录路径部分（例如：https://www.example.com/path/）
   *
   * @param url 完整的URL
   * @return 协议、域名和目录路径部分
   */
  private String extractBaseDirUrl(String url) {
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      String host = uri.getHost();
      int port = uri.getPort();
      String path = uri.getPath();

      StringBuilder baseDir = new StringBuilder();
      baseDir.append(scheme).append("://").append(host);
      if (port != -1) {
        baseDir.append(":").append(port);
      }

      if (StringUtils.isNotBlank(path)) {
        // 确保路径以 / 结尾，表示目录
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
          baseDir.append(path.substring(0, lastSlash + 1));
        }
        else {
          baseDir.append("/"); // 如果没有斜杠，也视为根目录
        }
      }
      else {
        baseDir.append("/");
      }
      return baseDir.toString();
    }
    catch (URISyntaxException e) {
      logger.warn("提取基础目录URL失败: {}", e.getMessage());
      return extractBaseDomain(url); // 失败时退回到只使用域名
    }
  }

  /**
   * 清理文档
   *
   * @param doc 文档对象
   */
  private void cleanDocument(Document doc) {
    articleHelper.removeElementsByKeywords(doc);
    // 提取文章主体后，进一步清除正文里夹带的“相关新闻/推荐阅读”等非正文区块（常见于腾讯新闻等站点）
    removeNonArticleExtraBlocks(doc);
    doc.select("p:empty, div:empty, span:empty, li:empty, td:empty, th:empty").remove();
    removeWhitespaceOnlyElements(doc);
  }

  /**
   * 移除文章正文中夹带的非正文区块（如“相关新闻/推荐阅读/更多内容”等）。
   *
   * <p>说明：这类内容经常被放在 article/main 容器内部，class/id 不一定包含 related/recommend，
   * 仅靠关键词删除不够。这里采用“关键词 + 链接密度”启发式删除，尽量避免误删正文段落。</p>
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void removeNonArticleExtraBlocks(Document doc) {
    if (doc == null || doc.body() == null) {
      return;
    }

    // 典型“非正文”提示词（标题/提示行）
    // @formatter:off
    String[] sectionKeywords = {
      "相关新闻", "相关内容", "相关报道", "相关链接",
      "推荐阅读", "相关阅读", "延伸阅读", "更多精彩", "更多内容", "更多新闻",
      "你可能还喜欢", "猜你喜欢", "热门推荐", "热点推荐",
      "新闻精选", "精选", "推荐", "更多", "相关文章", "相关"
    };
    // @formatter:on

    Elements candidates = doc.select("section, aside, nav, ul, ol, div, table");
    List<Element> toRemove = new ArrayList<>();

    for (Element el : candidates) {
      if (shouldRemoveNonArticleExtraBlock(el, sectionKeywords)) {
        toRemove.add(el);
      }
    }

    for (Element el : toRemove) {
      el.remove();
    }
  }

  private boolean shouldRemoveNonArticleExtraBlock(Element el, String[] sectionKeywords) {
    if (el == null) {
      return false;
    }
    if (isRootContainer(el)) {
      return false;
    }
    if (looksLikeMainContent(el)) {
      return false;
    }

    CandidateMetricsDTO m = buildCandidateMetrics(el);
    if (m.getLinkCount() < 3) {
      return false;
    }

    boolean hitKeyword = hitSectionKeyword(el, sectionKeywords);
    boolean shouldRemove = matchesGeneralNonArticleRules(m, hitKeyword);
    if (shouldRemove) {
      return true;
    }
    return matchesFallbackWidgetRules(el, m.getLinkCount());
  }

  private boolean isRootContainer(Element el) {
    String tag = el.tagName();
    return "body".equalsIgnoreCase(tag) || "html".equalsIgnoreCase(tag);
  }

  private boolean looksLikeMainContent(Element el) {
    int pCount = countParagraphs(el);
    int textLen = safeLength(el.text());
    return pCount >= 3 && textLen >= 400;
  }

  private int countParagraphs(Element el) {
    if (el == null) {
      return 0;
    }
    return el.select("> p").size() + el.select("p").size();
  }

  private CandidateMetricsDTO buildCandidateMetrics(Element el) {
    int pCount = countParagraphs(el);
    int textLen = safeLength(el.text());
    List<Element> links = el.select("a[href]");
    int linkCount = links.size();
    int liCount = el.select("li").size();
    int imgCount = el.select("img").size();
    int anchorWithNoTextCount = countAnchorsWithNoTextButHasMedia(links);
    int linkTextLen = computeLinkTextLength(links);
    double linkDensity = textLen == 0 ? 0d : (double) linkTextLen / (double) textLen;
    int nonLinkTextLen = Math.max(0, textLen - linkTextLen);
    CandidateMetricsDTO dto = new CandidateMetricsDTO();
    dto.setPCount(pCount);
    dto.setTextLen(textLen);
    dto.setLinkCount(linkCount);
    dto.setLiCount(liCount);
    dto.setImgCount(imgCount);
    dto.setLinkDensity(linkDensity);
    dto.setNonLinkTextLen(nonLinkTextLen);
    dto.setAnchorWithNoTextCount(anchorWithNoTextCount);
    return dto;
  }

  private int computeLinkTextLength(List<Element> links) {
    int sum = 0;
    if (links == null) {
      return sum;
    }
    for (Element a : links) {
      sum += safeLength(a.text());
    }
    return sum;
  }

  private boolean hitSectionKeyword(Element el, String[] sectionKeywords) {
    boolean hitKeyword = containsAny(el.text(), sectionKeywords);
    if (hitKeyword) {
      return true;
    }
    Element prev = previousMeaningfulSibling(el);
    return prev != null && containsAny(prev.text(), sectionKeywords);
  }

  private boolean matchesGeneralNonArticleRules(CandidateMetricsDTO m, boolean hitKeyword) {
    return isListWidget(m)
      || isMediaListWidget(m)
      || isHighLinkRatioWidget(m)
      || isKeywordWidget(m, hitKeyword);
  }

  private boolean isListWidget(CandidateMetricsDTO m) {
    return m.getLiCount() >= 6
      && m.getLinkCount() >= 6
      && m.getPCount() <= 1
      && m.getTextLen() <= 3000;
  }

  private boolean isMediaListWidget(CandidateMetricsDTO m) {
    return m.getLinkCount() >= 6
      && (m.getImgCount() >= 3 || m.getAnchorWithNoTextCount() >= 4)
      && m.getPCount() <= 1
      && m.getNonLinkTextLen() <= 120
      && m.getTextLen() <= 4000;
  }

  private boolean isHighLinkRatioWidget(CandidateMetricsDTO m) {
    return m.getLinkCount() >= 8
      && m.getPCount() <= 1
      && m.getTextLen() <= 4000
      && (m.getLinkDensity() >= 0.65 || m.getNonLinkTextLen() <= 80);
  }

  private boolean isKeywordWidget(CandidateMetricsDTO m, boolean hitKeyword) {
    if (!hitKeyword) {
      return false;
    }
    return m.getLinkCount() >= 3
      && m.getPCount() <= 2
      && m.getTextLen() <= 5000
      && (m.getLiCount() >= 3 || m.getLinkCount() >= 6 || m.getNonLinkTextLen() <= 200);
  }

  private boolean matchesFallbackWidgetRules(Element el, int linkCount) {
    if (el == null || linkCount < 3) {
      return false;
    }
    String cls = StringUtils.defaultString(el.className()).toLowerCase();
    String id = StringUtils.defaultString(el.id()).toLowerCase();
    return cls.contains("selected_news")
      || cls.contains("news_list")
      || cls.contains("newsrecommend")
      || "jxxw".equals(id)
      || "changelist".equals(id)
      || el.selectFirst(".selected_news_wrapper, .news_list_ul, .newsRecommendTitle, #jxxw, #changelist") != null;
  }

  private int safeLength(String s) {
    return s == null ? 0 : s.trim().length();
  }

  private boolean containsAny(String text, String[] keywords) {
    if (StringUtils.isBlank(text) || keywords == null || keywords.length == 0) {
      return false;
    }
    String t = text.trim();
    for (String k : keywords) {
      if (StringUtils.isBlank(k)) {
        continue;
      }
      if (t.contains(k)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 统计：a 标签文本为空但包含媒体（img/svg/i 等）或明显是卡片点击区域的数量。
   */
  private int countAnchorsWithNoTextButHasMedia(List<Element> links) {
    if (links == null || links.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (Element a : links) {
      if (a == null) {
        continue;
      }
      String t = a.text();
      if (StringUtils.isBlank(t)) {
        // 常见：a 里包 img / svg / i，或者作为卡片的空链接
        if (a.selectFirst("img, svg, i, video") != null || !a.children().isEmpty()) {
          count++;
        }
      }
    }
    return count;
  }

  /**
   * 获取上一个“有意义”的兄弟节点（跳过空白/空文本节点）。
   */
  private Element previousMeaningfulSibling(Element el) {
    if (el == null) {
      return null;
    }
    Element prev = el.previousElementSibling();
    int guard = 0;
    while (prev != null && guard < 5) {
      if (StringUtils.isNotBlank(prev.text()) || !prev.children().isEmpty()) {
        return prev;
      }
      prev = prev.previousElementSibling();
      guard++;
    }
    return null;
  }

  /**
   * 移除不需要的元素
   */
  private void removeUnwantedElements(Document doc) {
    doc.select(DEFAULT_REMOVE_ELEMENTS).remove();
    doc.select("video, audio, source, track, canvas").remove();

    // 移除社交互动元素（点赞、评论数、阅读数等）
    removeSocialInteractionElements(doc);

    // 移除其他隐藏的内容
    removeHiddenElements(doc);
  }

  /**
   * 移除社交互动元素（点赞、评论数、阅读数、分享等）
   *
   * @param doc 文档对象
   */
  private void removeSocialInteractionElements(Document doc) {
    // 移除点赞相关的元素
    // @formatter:off
    String[] likeSelectors = {
      ".like", ".likes", ".like-count", ".likeCount", ".like_count",
      ".thumbs-up", ".thumbsUp", ".thumbs_up", ".thumb-up", ".thumbUp",
      ".favorite", ".favorites", ".fav", ".star", ".stars",
      "[class*='like' i]", "[class*='thumb' i]", "[class*='favorite' i]",
      "[id*='like' i]", "[id*='thumb' i]", "[id*='favorite' i]"
    };
    // @formatter:on
    for (String selector : likeSelectors) {
      doc.select(selector).remove();
    }

    // 移除评论数相关的元素
    // @formatter:off
    String[] commentCountSelectors = {
      ".comment-count", ".commentCount", ".comment_count", ".comments-count",
      ".comment-num", ".commentNum", ".comment_num",
      "[class*='comment-count' i]", "[class*='comment-num' i]",
      "[id*='comment-count' i]", "[id*='comment-num' i]"
    };
    // @formatter:on
    for (String selector : commentCountSelectors) {
      doc.select(selector).remove();
    }

    // 移除阅读数/浏览数相关的元素
    // @formatter:off
    String[] viewCountSelectors = {
      ".view-count", ".viewCount", ".view_count", ".views", ".view",
      ".read-count", ".readCount", ".read_count", ".reads", ".read",
      ".pv", ".pv-count", ".pvCount", ".visit", ".visits",
      "[class*='view-count' i]", "[class*='read-count' i]", "[class*='pv' i]",
      "[id*='view-count' i]", "[id*='read-count' i]", "[id*='pv' i]"
    };
    // @formatter:on
    for (String selector : viewCountSelectors) {
      doc.select(selector).remove();
    }

    // 移除分享相关的元素（更全面的选择器）
    // @formatter:off
    String[] shareSelectors = {
      ".share-count", ".shareCount", ".share_count", ".share-num",
      ".social-share", ".socialShare", ".social_share",
      "[class*='share-count' i]", "[class*='share-num' i]",
      "[id*='share-count' i]", "[id*='share-num' i]"
    };
    // @formatter:on
    for (String selector : shareSelectors) {
      doc.select(selector).remove();
    }

    // 移除包含数字和特定关键词的元素（如"点赞 123"、"评论 456"等）
    removeElementsByTextPattern(doc);

    // 移除腾讯新闻网特定的社交元素
    removeTencentNewsElements(doc);
  }

  /**
   * 通过文本模式移除元素（如包含"点赞"、"评论"、"阅读"等关键词且包含数字的元素）
   *
   * @param doc 文档对象
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void removeElementsByTextPattern(Document doc) {
    // 查找所有可能包含社交互动信息的元素
    Elements elements = doc.select("span, div, p, em, strong, b, i");
    List<Element> elementsToRemove = new ArrayList<>();

    for (Element element : elements) {
      String text = element.text().trim();
      if (StringUtils.isBlank(text)) {
        continue;
      }

      // 检查是否包含社交互动关键词和数字
      // @formatter:off
      String[] socialKeywords = {
        "点赞", "喜欢", "收藏", "关注", "粉丝",
        "评论", "回复", "讨论",
        "阅读", "浏览", "查看", "访问", "阅读量", "浏览量",
        "分享", "转发", "收藏",
        "like", "likes", "thumbs", "favorite", "star",
        "comment", "comments", "reply", "replies",
        "view", "views", "read", "reads", "pv", "visit", "visits",
        "share", "shares", "forward"
      };
      // @formatter:on

      String lowerText = text.toLowerCase();
      boolean hasKeyword = false;
      for (String keyword : socialKeywords) {
        if (lowerText.contains(keyword.toLowerCase())) {
          hasKeyword = true;
          break;
        }
      }

      // 如果包含关键词且包含数字，可能是社交互动信息
      if (hasKeyword && text.matches(".*\\d+.*")) {
        // 进一步检查：如果文本很短（通常社交互动信息都很短）且包含数字
        if (text.length() < 50 && text.matches(".*[\\d万千万亿]+.*")) {
          elementsToRemove.add(element);
        }
      }
    }

    // 移除匹配的元素
    for (Element element : elementsToRemove) {
      element.remove();
    }
  }

  /**
   * 移除腾讯新闻网特定的社交元素
   *
   * @param doc 文档对象
   */
  private void removeTencentNewsElements(Document doc) {
    // 腾讯新闻网特定的选择器
    // @formatter:off
    String[] tencentSelectors = {
      ".qq-like", ".qq-like-count", ".qq-comment", ".qq-comment-count",
      ".qq-share", ".qq-share-count", ".qq-view", ".qq-view-count",
      ".article-like", ".article-comment", ".article-share",
      ".news-like", ".news-comment", ".news-share",
      "[class*='qq-' i]", "[class*='tencent-' i]",
      "[id*='qq-' i]", "[id*='tencent-' i]"
    };
    // @formatter:on
    for (String selector : tencentSelectors) {
      doc.select(selector).remove();
    }
  }

  /**
   * 移除隐藏的元素（通过 CSS 或其他方式隐藏的内容）
   *
   * @param doc 文档对象
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void removeHiddenElements(Document doc) {
    // 查找所有可能隐藏的元素
    Elements allElements = doc.select("*");
    List<Element> elementsToRemove = new ArrayList<>();

    for (Element element : allElements) {
      if (isHiddenElement(element)) {
        elementsToRemove.add(element);
      }
    }

    // 移除隐藏的元素
    for (Element element : elementsToRemove) {
      element.remove();
    }
  }

  private boolean isHiddenElement(Element element) {
    if (element == null) {
      return false;
    }
    String tagName = element.tagName();
    if ("body".equals(tagName) || "html".equals(tagName)) {
      return false;
    }
    return element.hasAttr("hidden")
      || hasHiddenInlineStyleForElement(element)
      || hasHiddenClassName(element)
      || isAriaHidden(element);
  }

  private boolean hasHiddenInlineStyleForElement(Element element) {
    String style = element.attr("style");
    if (StringUtils.isBlank(style)) {
      return false;
    }
    String s = style.toLowerCase();
    return s.contains("display:none")
      || s.contains("display: none")
      || s.contains("visibility:hidden")
      || s.contains("visibility: hidden");
  }

  private boolean hasHiddenClassName(Element element) {
    String className = element.className();
    if (StringUtils.isBlank(className)) {
      return false;
    }
    String s = className.toLowerCase();
    return s.contains("hidden")
      || s.contains("hide")
      || s.contains("invisible")
      || s.contains("sr-only")
      || s.contains("screen-reader");
  }

  private boolean isAriaHidden(Element element) {
    String ariaHidden = element.attr("aria-hidden");
    return "true".equalsIgnoreCase(ariaHidden);
  }

  /**
   * 移除只包含空白文本的元素
   *
   * @param doc 文档对象
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private void removeWhitespaceOnlyElements(Document doc) {
    // 查找所有可能只包含空白文本的元素
    Elements elements = doc.select("p, div, span, li, td, th");
    for (Element element : elements) {
      String text = element.text().trim();
      // 如果元素只包含空白或换行符，且没有子元素，则移除
      if (text.isEmpty() && element.children().isEmpty()) {
        element.remove();
      }
    }
  }

}
