package com.iwhalecloud.bote.doc.module.crawl.step.transformer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 转换器 - Markdown
 *
 * @author chen.linfa
 * @since 2026-02-21
 */
@Component
public class MarkdownTransformer {

  private final Logger logger = LoggerFactory.getLogger(MarkdownTransformer.class);

  public String execute(String content, String url) {
    if (StringUtils.isEmpty(content)) {
      return "";
    }
    Document doc = Jsoup.parse(content, url);
    Element body = doc.body();
    if (body == null) {
      return "";
    }
    StringBuilder markdown = new StringBuilder();
    processElement(url, body, markdown, 0);
    return markdown.toString().trim();

  }

  /**
   * 递归处理元素，转换为 Markdown
   */
  private void processElement(String url, Element element, StringBuilder markdown, int listLevel) {
    String tagName = element.tagName().toLowerCase();

    if (handleHeading(tagName, element, markdown, url)) {
      return;
    }
    if (handleList(tagName, element, markdown, url)) {
      return;
    }
    if (handleBlockquote(tagName, element, markdown, url)) {
      return;
    }
    if (handleParagraph(tagName, element, markdown, url)) {
      return;
    }
    if (handlePre(tagName, element, markdown)) {
      return;
    }
    if (handleInlineFormatting(tagName, element, markdown, url)) {
      return;
    }
    if (handleLinkOrImage(tagName, element, markdown, url)) {
      return;
    }

    processChildrenOrOwnText(url, element, markdown, listLevel);
  }

  private boolean handleHeading(String tagName, Element element, StringBuilder markdown, String url) {
    if (tagName.length() != 2 || tagName.charAt(0) != 'h') {
      return false;
    }
    char levelChar = tagName.charAt(1);
    if (levelChar < '1' || levelChar > '6') {
      return false;
    }

    int level = levelChar - '0';
    String headingText = processInlineElements(element, url);
    if (StringUtils.isNotBlank(headingText)) {
      markdown.append("#".repeat(level)).append(" ").append(headingText).append("\n\n");
    }
    return true;
  }

  private boolean handleParagraph(String tagName, Element element, StringBuilder markdown, String url) {
    if (!"p".equals(tagName)) {
      return false;
    }
    String paragraphText = processInlineElements(element, url);
    if (StringUtils.isNotBlank(paragraphText)) {
      markdown.append(paragraphText).append("\n\n");
    }
    return true;
  }

  private boolean handleList(String tagName, Element element, StringBuilder markdown, String url) {
    if (!"ul".equals(tagName) && !"ol".equals(tagName)) {
      return false;
    }

    boolean isOrdered = "ol".equals(tagName);
    List<Element> items = element.select("> li");
    for (int i = 0; i < items.size(); i++) {
      Element item = items.get(i);
      String prefix = isOrdered ? (i + 1) + ". " : "- ";
      String itemText = processInlineElements(item, url);
      if (StringUtils.isNotBlank(itemText)) {
        markdown.append(prefix).append(itemText).append("\n");
      }
    }
    markdown.append("\n");
    return true;
  }

  private boolean handleBlockquote(String tagName, Element element, StringBuilder markdown, String url) {
    if (!"blockquote".equals(tagName)) {
      return false;
    }
    String quoteText = processInlineElements(element, url);
    if (StringUtils.isNotBlank(quoteText)) {
      markdown.append("> ").append(quoteText.replace("\n", "\n> ")).append("\n\n");
    }
    return true;
  }

  private boolean handlePre(String tagName, Element element, StringBuilder markdown) {
    if (!"pre".equals(tagName)) {
      return false;
    }
    String codeText = element.text();
    if (StringUtils.isNotBlank(codeText)) {
      markdown.append("```\n").append(codeText).append("\n```\n\n");
    }
    return true;
  }

  private boolean handleInlineFormatting(String tagName, Element element, StringBuilder markdown, String url) {
    if (handleInlineCode(tagName, element, markdown)) {
      return true;
    }
    if (handleLineBreakOrHr(tagName, markdown)) {
      return true;
    }
    if (handleEmphasis(tagName, element, markdown, url)) {
      return true;
    }
    return false;
  }

  private boolean handleInlineCode(String tagName, Element element, StringBuilder markdown) {
    if (!"code".equals(tagName)) {
      return false;
    }
    // 内联代码，如果不在 pre 标签内
    Element parent = element.parent();
    if (parent == null || !"pre".equals(parent.tagName().toLowerCase())) {
      String inlineCode = element.text();
      if (StringUtils.isNotBlank(inlineCode)) {
        markdown.append("`").append(inlineCode).append("`");
      }
    }
    return true;
  }

  private boolean handleLineBreakOrHr(String tagName, StringBuilder markdown) {
    if ("br".equals(tagName)) {
      markdown.append("\n");
      return true;
    }
    if ("hr".equals(tagName)) {
      markdown.append("---\n\n");
      return true;
    }
    return false;
  }

  private boolean handleEmphasis(String tagName, Element element, StringBuilder markdown, String url) {
    if ("strong".equals(tagName) || "b".equals(tagName)) {
      String boldText = processInlineElements(element, url);
      if (StringUtils.isNotBlank(boldText)) {
        markdown.append("**").append(boldText).append("**");
      }
      return true;
    }

    if ("em".equals(tagName) || "i".equals(tagName)) {
      String italicText = processInlineElements(element, url);
      if (StringUtils.isNotBlank(italicText)) {
        markdown.append("*").append(italicText).append("*");
      }
      return true;
    }
    return false;
  }

  private boolean handleLinkOrImage(String tagName, Element element, StringBuilder markdown, String url) {
    if ("a".equals(tagName)) {
      String href = element.attr("href");
      String linkText = processInlineElements(element, url);
      if (StringUtils.isNotBlank(href) && StringUtils.isNotBlank(linkText)) {
        String absoluteUrl = resolveUrl(href, url);
        if (absoluteUrl != null) {
          markdown.append("[").append(linkText).append("](").append(absoluteUrl).append(")");
        }
        else {
          markdown.append(linkText);
        }
      }
      else if (StringUtils.isNotBlank(linkText)) {
        markdown.append(linkText);
      }
      return true;
    }

    if ("img".equals(tagName)) {
      String src = element.attr("src");
      String alt = element.attr("alt");
      if (StringUtils.isNotBlank(src)) {
        String absoluteUrl = resolveUrl(src, url);
        if (absoluteUrl != null) {
          markdown.append("![").append(StringUtils.defaultString(alt)).append("](").append(absoluteUrl).append(")");
        }
      }
      return true;
    }
    return false;
  }

  private void processChildrenOrOwnText(String url, Element element, StringBuilder markdown, int listLevel) {
    // 对于其他元素，递归处理子元素
    for (Element child : element.children()) {
      processElement(url, child, markdown, listLevel);
    }
    // 如果元素没有子元素但有文本，处理文本
    if (element.children().isEmpty() && StringUtils.isNotBlank(element.text())) {
      String text = processInlineElements(element, url);
      if (StringUtils.isNotBlank(text)) {
        markdown.append(text);
      }
    }
  }

  /**
   * 处理内联元素（链接、图片、粗体、斜体等）
   */
  private String processInlineElements(Element element, String url) {
    StringBuilder text = new StringBuilder();
    for (Node node : element.childNodes()) {
      appendInlineNode(text, node, url);
    }
    return text.toString().trim();
  }

  private void appendInlineNode(StringBuilder text, Node node, String url) {
    if (node instanceof Element) {
      appendInlineElement(text, (Element) node, url);
      return;
    }
    if (node instanceof TextNode) {
      text.append(((TextNode) node).text());
    }
  }

  private void appendInlineElement(StringBuilder text, Element child, String url) {
    String tagName = child.tagName().toLowerCase();
    if (appendInlineEmphasis(text, child, tagName)) {
      return;
    }
    if (appendInlineCode(text, child, tagName)) {
      return;
    }
    if (appendInlineLink(text, child, tagName, url)) {
      return;
    }
    if (appendInlineImage(text, child, tagName, url)) {
      return;
    }
    if (appendInlineBr(text, tagName)) {
      return;
    }
    text.append(child.text());
  }

  private boolean appendInlineEmphasis(StringBuilder text, Element child, String tagName) {
    if ("strong".equals(tagName) || "b".equals(tagName)) {
      text.append("**").append(child.text()).append("**");
      return true;
    }
    if ("em".equals(tagName) || "i".equals(tagName)) {
      text.append("*").append(child.text()).append("*");
      return true;
    }
    return false;
  }

  private boolean appendInlineCode(StringBuilder text, Element child, String tagName) {
    if (!"code".equals(tagName)) {
      return false;
    }
    text.append("`").append(child.text()).append("`");
    return true;
  }

  private boolean appendInlineLink(StringBuilder text, Element child, String tagName, String url) {
    if (!"a".equals(tagName)) {
      return false;
    }
    String href = child.attr("href");
    String linkText = child.text();
    if (StringUtils.isNotBlank(href) && StringUtils.isNotBlank(linkText)) {
      String absoluteUrl = resolveUrl(href, url);
      if (absoluteUrl != null) {
        text.append("[").append(linkText).append("](").append(absoluteUrl).append(")");
      }
      else {
        text.append(linkText);
      }
    }
    else {
      text.append(linkText);
    }
    return true;
  }

  private boolean appendInlineImage(StringBuilder text, Element child, String tagName, String url) {
    if (!"img".equals(tagName)) {
      return false;
    }
    String src = child.attr("src");
    String alt = child.attr("alt");
    if (StringUtils.isNotBlank(src)) {
      String absoluteUrl = resolveUrl(src, url);
      if (absoluteUrl != null) {
        text.append("![").append(StringUtils.defaultString(alt)).append("](").append(absoluteUrl).append(")");
      }
    }
    return true;
  }

  private boolean appendInlineBr(StringBuilder text, String tagName) {
    if (!"br".equals(tagName)) {
      return false;
    }
    text.append("\n");
    return true;
  }

  /**
   * 解析 URL，将相对路径转换为绝对路径
   */
  private String resolveUrl(String relativeOrAbsolutePath, String url) {
    if (StringUtils.isBlank(relativeOrAbsolutePath)) {
      return null;
    }

    // 如果已经是绝对 URL，直接返回
    if (relativeOrAbsolutePath.startsWith("http://") || relativeOrAbsolutePath.startsWith("https://")) {
      return relativeOrAbsolutePath;
    }

    // 如果是 mailto: 或 javascript: 等协议，直接返回
    if (relativeOrAbsolutePath.contains(":")) {
      return relativeOrAbsolutePath;
    }

    // 如果是锚点链接，返回空
    if (relativeOrAbsolutePath.startsWith("#")) {
      return null;
    }

    try {
      URI baseUri = new URI(url);
      URI resolvedUri = baseUri.resolve(relativeOrAbsolutePath);
      return resolvedUri.normalize().toString();
    }
    catch (URISyntaxException e) {
      logger.warn("解析 URL 失败: {} (base: {})", relativeOrAbsolutePath, url, e);
      return null;
    }
  }
}
