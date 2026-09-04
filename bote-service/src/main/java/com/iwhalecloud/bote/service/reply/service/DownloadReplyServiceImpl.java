package com.iwhalecloud.bote.service.reply.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.cache.ReplyDownloadCache;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import com.iwhalecloud.bote.mapper.chat.SessionMsgTextMapper;
import com.iwhalecloud.bote.service.reply.IDownloadReplyService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.vladsch.flexmark.docx.converter.DocxRenderer;
import com.vladsch.flexmark.docx.converter.DocxRenderer.Builder;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 下载回复服务
 *
 * @author zyt
 * @since 2025-05-14
 */
@Service
@RequiredArgsConstructor
public class DownloadReplyServiceImpl implements IDownloadReplyService {
  /**
   * 预编译的正则表达式：在表格前添加空行
   * 匹配模式：仅当上一行非表格且非空时，在表格前添加空行（支持前导空格，避免表格之间插空行）
   */
  private static final Pattern TABLE_SPACING_PATTERN = Pattern.compile(
    "(?m)(^(?!\\s*\\|).*\\S.*)\\r?\\n(?=^\\s*\\|)",
    Pattern.MULTILINE
  );

  /** flexmark 解析和渲染的配置选项 */
  private static final DataHolder FLEXMARK_OPTIONS = new MutableDataSet()
    .set(Parser.EXTENSIONS, Arrays.asList(
      DefinitionExtension.create(),
      EmojiExtension.create(),
      FootnoteExtension.create(),
      StrikethroughSubscriptExtension.create(),
      InsExtension.create(),
      SuperscriptExtension.create(),
      TablesExtension.create(),
      TocExtension.create(),
      SimTocExtension.create(),
      WikiLinkExtension.create()
    ))
    .set(DocxRenderer.SUPPRESS_HTML, true);

  private final SessionMsgTextMapper sessionMsgTextMapper;
  private final ReplyDownloadCache replyDownloadCache;
  private final DocChainImageResolverFactory docChainImageResolverFactory;

  @Override
  public ReplyDownloadInfoDTO getDownloadInfo(Long tenantId, String msgId) {
    ReplyDownloadInfoDTO downloadInfo;
    // 聊天窗口中消息 ID 是整数，调试工作流时消息 ID 为 uuid
    if (StringUtils.isNumeric(msgId)) {
      Long msgIdLong = Long.parseLong(msgId);
      downloadInfo = sessionMsgTextMapper.selectDownloadInfoByMsgId(msgIdLong);
      if (downloadInfo != null && StringUtils.isNotEmpty(downloadInfo.getParagraphGroup())) {
        // 段落类型的回复，需要按顺序收集所有关联段落
        List<String> paragraphs = sessionMsgTextMapper.selectParagraphReply(downloadInfo.getTransactionId(), downloadInfo.getParagraphGroup());
        String content = String.join("\n", paragraphs);
        downloadInfo.setContent(content);
      }
    }
    else {
      downloadInfo = replyDownloadCache.get(tenantId, msgId);
    }
    Assert.notNull(downloadInfo, "消息不存在");
    Assert.hasLength(downloadInfo.getFileType(), "文件下载类型不能为空");
    Assert.hasLength(downloadInfo.getContent(), "文件下载内容不能为空");
    return downloadInfo;
  }

  @Override
  public void generateExcelByMarkdown(String markdownContent, OutputStream outputStream) throws IOException {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      MutableDataSet options = new MutableDataSet();
      options.set(Parser.EXTENSIONS, Collections.singletonList(TablesExtension.create()));

      // 转换Markdown为HTML（支持表格解析）
      String html = HtmlRenderer.builder(options)
        .build()
        .render(Parser.builder(options).build().parse(markdownContent));

      // 创建样式预定义
      Map<String, HorizontalAlignment> alignMap = new HashMap<>();
      alignMap.put("left", HorizontalAlignment.LEFT);
      alignMap.put("center", HorizontalAlignment.CENTER);
      alignMap.put("right", HorizontalAlignment.RIGHT);

      XSSFSheet sheet = workbook.createSheet("Sheet1");
      int rowNum = 0;

      for (Element table : Jsoup.parse(html).select("table")) {
        // 处理表头
        for (Element headerRow : table.select("thead tr")) {
          XSSFRow xssfRow = sheet.createRow(rowNum++);
          int cellIdx = 0;

          for (Element th : headerRow.select("th")) {
            XSSFCell cell = xssfRow.createCell(cellIdx++);
            cell.setCellValue(th.text());

            String align = th.attr("align").toLowerCase();
            XSSFCellStyle style = createCellStyle(workbook,
              alignMap.getOrDefault(align, HorizontalAlignment.CENTER), true);
            cell.setCellStyle(style);
          }
        }

        // 处理表格主体
        for (Element bodyRow : table.select("tbody tr")) {
          XSSFRow xssfRow = sheet.createRow(rowNum++);
          int cellIdx = 0;

          for (Element td : bodyRow.select("td")) {
            XSSFCell cell = xssfRow.createCell(cellIdx++);
            cell.setCellValue(td.hasText() ? td.text() : "");

            String align = td.attr("align").toLowerCase();
            XSSFCellStyle style = createCellStyle(workbook,
              alignMap.getOrDefault(align, HorizontalAlignment.LEFT), false);
            cell.setCellStyle(style);
          }
        }

        rowNum++; // 表格间添加空行
      }

      if (sheet.getRow(0) != null) {
        // 智能列宽调整（限制最大宽度）
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
          sheet.autoSizeColumn(i);
          int maxWidth = Math.min(sheet.getColumnWidth(i) + 1024, 40 * 256);
          sheet.setColumnWidth(i, maxWidth);
        }
      }

      workbook.write(outputStream);
    }
  }

  /**
   * 创建并返回一个自定义样式的单元格样式对象
   * write by zyt 2025.5.14 11455972
   *
   * @param workbook 表示Excel工作簿的对象，用于创建样式和字体
   * @param alignment 指定文本的水平对齐方式
   * @param isBold 指示是否需要加粗字体的布尔值
   * @return 返回创建的自定义单元格样式对象
   */
  private XSSFCellStyle createCellStyle(XSSFWorkbook workbook, HorizontalAlignment alignment, boolean isBold) {
    XSSFCellStyle style = workbook.createCellStyle();
    style.setAlignment(alignment);
    if (isBold) {
      XSSFFont font = workbook.createFont();
      font.setBold(true);
      style.setFont(font);
    }
    return style;
  }

  @Override
  public void generateExcelByJson(String content, OutputStream outputStream) throws IOException {
    // 1. 使用 JsonUtil 解析 JSON 内容，获取 list 字段的二维数组结构
    JsonNode jsonNode = JsonUtil.readTree(content);
    JsonNode listNode = jsonNode.get("list");
    Assert.notNull(listNode, "缺少 list 属性");
    Assert.isTrue(listNode.isArray(), "list 不是数组");
    Assert.isTrue(!listNode.isEmpty(), "list 数组不能为空");

    // 2. 创建 Excel 工作簿和工作表
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Sheet1");

      // 3. 填充数据行
      int rowIndex = 0;
      for (JsonNode rowNode : listNode) {
        Assert.isTrue(rowNode.isArray(), "list 中的元素必须是数组");
        Row dataRow = sheet.createRow(rowIndex++);
        int colIndex = 0;
        for (JsonNode cellNode : rowNode) {
          Cell cell = dataRow.createCell(colIndex++);
          String value = cellNode.isNull() ? "" : cellNode.asText("");
          cell.setCellValue(value);
        }
      }

      // 4. 输出到输出流
      workbook.write(outputStream);
    }
  }

  @Override
  public void generateDocxByMarkdown(@Nullable Long tenantId, String markdownContent, OutputStream outputStream) {
    // 简单处理：仅当上一行非表格且非空时，在表格前添加空行（支持前导空格，避免表格之间插空行）
    String processedContent = TABLE_SPACING_PATTERN.matcher(markdownContent).replaceAll("$1\n\n");

    // 如果内容更像是普通文本（非 Markdown），
    // 则将单行换行符转换为 Markdown 的硬换行（在换行前加两个空格），
    // 以确保在 Word 中按原有换行显示。
    if (isLikelyPlainText(processedContent)) {
      processedContent = processedContent.replace("\r\n", "\n");
      // 仅在非空行之间的单个换行处插入硬换行标记，保留空行作为段落分隔
      processedContent = processedContent.replaceAll("(?m)(?<=\\S)\n(?=\\S)", "  \n");
    }

    // 创建解析器
    Parser parser = Parser.builder(FLEXMARK_OPTIONS).build();
    Node document = parser.parse(processedContent);
    // 创建渲染器
    Builder builder = DocxRenderer.builder(FLEXMARK_OPTIONS);
    if (tenantId != null) {
      // SessionInterceptor 会清理线程本地变量，这里不用清理
      TenantIdUtil.setTenantId(tenantId);
      builder.contentResolverFactory(docChainImageResolverFactory);
    }
    DocxRenderer renderer = builder.build();
    // 获取默认模板
    WordprocessingMLPackage template = DocxRenderer.getDefaultTemplate();
    // 渲染文档
    renderer.render(document, template);

    try {
      // 保存到输出流
      template.save(outputStream, Docx4J.FLAG_SAVE_ZIP_FILE);
    }
    catch (Docx4JException e) {
      throw new BssException("Markdown 转 Word 文档失败: " + e.getMessage(), e);
    }
  }

  /**
   * 基于启发式判断文本是否更像普通文本而非 Markdown
   */
  private boolean isLikelyPlainText(String text) {
    // 常见 Markdown 标记的快速检测
    // @formatter:off
    String[] patterns = new String[] {
      "(?m)^\\s*#\\s",                 // 标题
      "(?m)^\\s*[\\-\\*\\+]\\s+",   // 无序列表
      "(?m)^\\s*\\d+\\.\\s+",        // 有序列表
      "(?m)^\\s*>\\s?",                 // 引用
      "`",                                 // 行内/代码块
      "\\|.*\\|",                       // 表格
      "!\\[.*?]\\(.*?\\)",            // 图片
      "\\[.*?]\\(.*?\\)",             // 链接
      "<\\w+[^>]*>"                      // 内联 HTML
    };
    // @formatter:on
    for (String p : patterns) {
      if (Pattern.compile(p).matcher(text).find()) {
        return false;
      }
    }
    return true;
  }

}
