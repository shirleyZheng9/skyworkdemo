package com.iwhalecloud.bote.doc.module.crawl.step.helper;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 提取文章 - 辅助工具类
 *
 * @author chen.linfa
 * @since 2026-02-21
 */
@Component
public class ProcessArticleHelper {

  private final Logger logger = LoggerFactory.getLogger(ProcessArticleHelper.class);

  /**
   * 提取并处理文章内容
   */
  public Document extractAndProcessArticle(Document doc, String url) {
    Element articleContent = extractArticleContent(doc);
    if (articleContent != null) {
      return createArticleDocument(doc, articleContent, url);
    }
    else {
      removeNonArticleContent(doc);
      return doc;
    }
  }

  /**
   * 提取文章主体内容
   */
  private Element extractArticleContent(Document doc) {
    // 先尝试使用常见的文章容器选择器
    Element article = findArticleBySelectors(doc);
    if (article != null) {
      return article;
    }

    // 如果没有找到，尝试找到包含最多段落和文本的div
    return findBestArticleDiv(doc);
  }

  /**
   * 通过选择器查找文章内容
   *
   * @param doc 文档对象
   * @return 文章内容元素，如果未找到则返回null
   */
  private Element findArticleBySelectors(Document doc) {
    // @formatter:off
    String[] articleSelectors = {
      "article",
      "main",
      ".article-content",
      ".article-body",
      ".article-main",
      ".post-content",
      ".post-body",
      ".entry-content",
      ".entry-body",
      ".content",
      ".main-content",
      "[role='article']",
      "[itemprop='articleBody']",
      ".article",
      ".post",
      ".entry"
    };
    // @formatter:on

    for (String selector : articleSelectors) {
      Element article = doc.selectFirst(selector);
      if (article != null && article.text().trim().length() > 100) {
        logger.debug("找到文章内容容器: {}", selector);
        return article;
      }
    }
    return null;
  }

  /**
   * 查找最佳文章div
   *
   * @param doc 文档对象
   * @return 最佳文章div，如果未找到则返回null
   */
  @SuppressWarnings("PMD.LooseCoupling")
  private Element findBestArticleDiv(Document doc) {
    Elements divs = doc.select("div");
    Element bestDiv = null;
    int maxParagraphs = 0;
    int maxTextLength = 0;

    for (Element div : divs) {
      if (isNonArticleDiv(div)) {
        continue;
      }

      Elements paragraphs = div.select("p");
      String text = div.text().trim();

      if (paragraphs.size() >= 3 && text.length() > 200) {
        if (isBetterDiv(paragraphs.size(), text.length(), maxParagraphs, maxTextLength)) {
          maxParagraphs = paragraphs.size();
          maxTextLength = text.length();
          bestDiv = div;
        }
      }
    }

    if (bestDiv != null) {
      logger.debug("找到最佳文章内容容器，包含 {} 个段落，文本长度: {}", maxParagraphs, maxTextLength);
    }
    return bestDiv;
  }

  /**
   * 创建只包含文章内容的文档
   */
  private Document createArticleDocument(Document originalDoc, Element articleContent, String url) {
    Element titleElement = extractArticleTitle(originalDoc, articleContent);
    Document articleDoc = new Document(url);
    Element newBody = articleDoc.body();

    if (titleElement != null) {
      newBody.appendChild(titleElement);
    }

    String articleHtml = articleContent.html();
    if (StringUtils.isNotBlank(articleHtml)) {
      Document tempDoc = Jsoup.parseBodyFragment(articleHtml, url);
      for (Element child : tempDoc.body().children()) {
        newBody.appendChild(child.clone());
      }
    }

    return articleDoc;
  }

  /**
   * 提取文章标题
   *
   * @param doc 文档对象
   * @param articleContent 文章内容元素
   * @return 标题元素，如果未找到则返回null
   */
  private Element extractArticleTitle(Document doc, Element articleContent) {
    // @formatter:off
    String[] titleSelectors = {
      "h1.article-title",
      "h1.post-title",
      "h1.entry-title",
      "h1.title",
      "article h1",
      "main h1",
      ".article-title",
      ".post-title",
      ".entry-title",
      "[itemprop='headline']",
      "h1"
    };
    // @formatter:on
    // 优先在文章内容中查找标题
    if (articleContent != null) {
      Element title = findTitleInElement(articleContent, titleSelectors);
      if (title != null) {
        // 文章容器里已经包含标题（通常是首个 h1），如果我们再额外生成一个 h1，
        // Markdown 转换时就会出现两次标题。这里在生成新标题前，先把原始标题从正文中移除。
        removeTitleFromArticleContent(articleContent, title);
        return createTitleElement(title);
      }
    }
    // 如果文章内容中没有找到，在整个文档中查找
    Element title = findTitleInDocument(doc, titleSelectors);
    return title != null ? createTitleElement(title) : null;
  }

  /**
   * 从文章内容中移除已识别的标题节点，避免标题重复输出。
   *
   * <p>仅移除标题节点本身；若其父容器因此变为空容器，会顺带清理空容器。</p>
   */
  private void removeTitleFromArticleContent(Element articleContent, Element title) {
    if (articleContent == null || title == null) {
      return;
    }
    // title 必须属于 articleContent 的子树
    if (title.parents().stream().noneMatch(articleContent::equals) && !articleContent.equals(title.parent())) {
      return;
    }

    Element parent = title.parent();
    title.remove();

    // 清理被移除标题后留下的空壳容器（最多向上清理 3 层，避免误删正文结构）
    int cleaned = 0;
    while (parent != null && !articleContent.equals(parent) && cleaned < 3) {
      boolean hasNoChildren = parent.children().isEmpty();
      boolean hasNoText = StringUtils.isBlank(parent.text());
      if (hasNoChildren && hasNoText) {
        Element next = parent.parent();
        parent.remove();
        parent = next;
        cleaned++;
      }
      else {
        break;
      }
    }
  }

  /**
   * 在元素中查找标题
   *
   * @param element 元素
   * @param selectors 选择器数组
   * @return 标题元素，如果未找到则返回null
   */
  private Element findTitleInElement(Element element, String[] selectors) {
    for (String selector : selectors) {
      Element title = element.selectFirst(selector);
      if (title != null && StringUtils.isNotBlank(title.text())) {
        return title;
      }
    }
    return null;
  }

  /**
   * 创建标题元素
   *
   * @param title 原始标题元素
   * @return 新的h1元素
   */
  private Element createTitleElement(Element title) {
    Element h1 = new Element("h1");
    h1.text(title.text().trim());
    h1.attr("style", "font-size: 24px; font-weight: bold; margin: 20px 0; line-height: 1.4;");
    return h1;
  }

  /**
   * 在文档中查找标题
   *
   * @param doc 文档对象
   * @param selectors 选择器数组
   * @return 标题元素，如果未找到则返回null
   */
  private Element findTitleInDocument(Document doc, String[] selectors) {
    for (String selector : selectors) {
      Element title = doc.selectFirst(selector);
      if (title != null && StringUtils.isNotBlank(title.text()) && !isTitleInNavigation(title)) {
        return title;
      }
    }
    return null;
  }

  /**
   * 移除非文章内容
   *
   * @param doc 文档对象
   */
  private void removeNonArticleContent(Document doc) {
    Element body = doc.body();
    if (body == null) {
      return;
    }

    // 移除所有明显不是文章内容的元素
    // @formatter:off
    String[] nonArticleSelectors = {
      "header",
      "footer",
      "nav",
      "aside",
      ".sidebar",
      ".side-bar",
      ".sidebox",
      ".navigation",
      ".navbar",
      ".menu",
      ".breadcrumb",
      ".breadcrumbs",
      ".pagination",
      ".page-nav",
      ".comment",
      ".comments",
      ".social-share",
      ".share",
      ".related",
      ".recommend",
      ".advertisement",
      ".ads",
      ".ad",
      ".widget",
      ".widget-box",
      ".newsletter",
      ".subscribe",
      ".cookie",
      ".popup",
      ".modal"
    };
    // @formatter:on
    for (String selector : nonArticleSelectors) {
      body.select(selector).remove();
    }

    // 移除包含特定关键词的元素
    removeElementsByKeywords(doc);
  }

  /**
   * 移除包含特定关键词的元素
   *
   * @param doc 文档对象
   */
  public void removeElementsByKeywords(Document doc) {
    // 需要移除的关键词列表（中文和英文）
    // @formatter:off
    String[] keywords = {
      "sidebar",
      "side-bar",
      "sidebox",
      "side-box",
      "related",
      "recommend",
      "recommendation",
      "related-articles",
      "related-posts",
      "advertisement",
      "ad",
      "ads",
      "ad-banner",
      "ad-container",
      "ad-wrapper",
      "comment",
      "comments",
      "comment-box",
      "comment-list",
      "comment-count",
      "comment-num",
      "social",
      "share",
      "share-box",
      "share-buttons",
      "share-count",
      "like",
      "likes",
      "like-count",
      "thumbs",
      "favorite",
      "view-count",
      "read-count",
      "views",
      "reads",
      "pv",
      "navigation",
      "nav",
      "navbar",
      "menu",
      "footer",
      "header",
      "site-header",
      "page-header",
      "widget",
      "widget-box",
      "widget-area",
      "newsletter",
      "subscribe",
      "subscription",
      "cookie",
      "popup",
      "modal",
      "overlay",
      "侧边栏", "相关", "推荐", "广告", "评论", "评论数", "评论量",
      "分享", "分享数", "点赞", "点赞数", "喜欢", "收藏",
      "阅读", "阅读数", "阅读量", "浏览量", "浏览数", "查看数",
      "导航", "页脚", "页头", "订阅", "弹窗", "悬浮", "工具栏", "操作栏"
    };
    // @formatter:on
    for (String keyword : keywords) {
      // 通过class选择器移除（不区分大小写）
      doc.select("[class*='" + keyword + "' i]").remove();
      // 通过id选择器移除（不区分大小写）
      doc.select("[id*='" + keyword + "' i]").remove();
    }
  }

  /**
   * 判断是否为更好的 div
   */
  private boolean isBetterDiv(int paragraphs, int textLength, int maxParagraphs, int maxTextLength) {
    return paragraphs > maxParagraphs || (paragraphs == maxParagraphs && textLength > maxTextLength);
  }

  /**
   * 判断标题是否在导航或侧边栏中
   */
  private boolean isTitleInNavigation(Element title) {
    Element parent = title.parent();
    if (parent == null) {
      return false;
    }

    String parentClass = parent.className().toLowerCase();
    String parentId = parent.id().toLowerCase();
    // @formatter:off
    String[] navKeywords = {"nav", "sidebar", "menu"};
    // @formatter:on
    for (String keyword : navKeywords) {
      if (parentClass.contains(keyword) || parentId.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断是否为非文章 div
   */
  private boolean isNonArticleDiv(Element div) {
    String className = div.className().toLowerCase();
    String id = div.id().toLowerCase();
    // @formatter:off
    String[] nonArticleKeywords = {"sidebar", "nav", "footer", "header", "ad", "comment"};
    // @formatter:on
    for (String keyword : nonArticleKeywords) {
      if (className.contains(keyword) || id.contains(keyword)) {
        return true;
      }
    }
    return false;
  }
}
