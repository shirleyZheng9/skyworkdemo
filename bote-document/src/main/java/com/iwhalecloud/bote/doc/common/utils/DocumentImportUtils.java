package com.iwhalecloud.bote.doc.common.utils;

import com.iwhalecloud.bote.doc.common.utils.converter.WordPackageLoadResultDTO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.iwhalecloud.bote.doc.common.utils.converter.DocumentConversionImageHandler;
import com.iwhalecloud.bote.doc.common.utils.converter.SafeWordToHtmlConverter;
import com.iwhalecloud.bote.doc.common.utils.converter.UploadingPicturesManager;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

/**
 * 文档导入工具类 提供文档格式转换功能，用于将本地文档转换为在线文档格式
 *
 * @author lizuyin
 * @since 2025-10-22
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class DocumentImportUtils {
  private DocumentImportUtils() {
  }

  private static final Logger logger = LoggerFactory.getLogger(DocumentImportUtils.class);

  /**
   * 内存缓冲的最大文件大小（50MB），超过此大小将使用临时文件 这是为了避免大文件导致 OOM（Out of Memory）错误
   */
  private static final long MAX_MEMORY_BUFFER_SIZE = 50 * 1024 * 1024L;

  /** 匹配以数字编号开头的段落前缀（如 1、2.1、2 1、2.1 1.1 等），用于计算标题层级。 */
  private static final Pattern NUMBERING_HEADING_PATTERN =
    Pattern.compile("^(\\d+(?:[.\\s]\\d+)*)\\s+.*");

  /** docx4j 解压单 part 允许的最大字节数（256MB），支持含大体积嵌入对象的 docx，避免 PartTooLargeException。 */
  private static final long DOCX4J_MAX_BYTES_UNZIP = 256L * 1024 * 1024;

  // 静态初始化块：在类加载时设置系统属性以解决 Woodstox 7.x 与 docx4j 的兼容性问题，并设置解压大小限制
  static {
    try {
      // 检查系统属性是否已设置
      String accessExternalDtd = System.getProperty("javax.xml.accessExternalDTD");
      String accessExternalSchema = System.getProperty("javax.xml.accessExternalSchema");

      // 如果没有设置，则设置为允许访问，以解决 Woodstox 7.x 与 docx4j 11.5.6 的兼容性问题
      if (accessExternalDtd == null) {
        System.setProperty("javax.xml.accessExternalDTD", "all");
        logger.debug("已设置系统属性 javax.xml.accessExternalDTD=all 以解决 Woodstox 7.x 兼容性问题");
      }
      if (accessExternalSchema == null) {
        System.setProperty("javax.xml.accessExternalSchema", "all");
        logger.debug("已设置系统属性 javax.xml.accessExternalSchema=all 以解决 Woodstox 7.x 兼容性问题");
      }
      // docx4j 解压/反序列化大小限制：若未通过 docx4j.properties 配置，则通过系统属性兜底，支持大体积嵌入对象（如 OLE）
      if (System.getProperty("docx4j.openpackaging.parts.MAX_BYTES.unzip.error") == null) {
        System.setProperty("docx4j.openpackaging.parts.MAX_BYTES.unzip.error", String.valueOf(DOCX4J_MAX_BYTES_UNZIP));
      }
      if (System.getProperty("docx4j.openpackaging.parts.MAX_BYTES.unmarshal.error") == null) {
        System.setProperty("docx4j.openpackaging.parts.MAX_BYTES.unmarshal.error", String.valueOf(DOCX4J_MAX_BYTES_UNZIP));
      }
      // docx4j 总解压大小限制（Zip bomb 检测），须与单 part 限制一致或更大
      if (System.getProperty("docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error") == null) {
        System.setProperty("docx4j.openpackaging.package.MAX_UNCOMPRESSED_SIZE.unzip.error", String.valueOf(DOCX4J_MAX_BYTES_UNZIP));
      }
    }
    catch (Exception e) {
      logger.warn("设置 XML 访问外部资源系统属性失败: {}", e.getMessage());
    }
  }

  /**
   * 将Word文档（.doc/.docx）转换为HTML格式 使用docx4j库进行转换，确保格式保留 如果提供了文档附件服务和文档ID，会在转换前提取图片并上传到文件服务器，用服务器地址替换Word中的图片
   *
   * @param file Word文档文件
   * @param fileName 文件名
   * @param documentId 文档ID（必需，用于上传图片附件）
   * @param documentAttachmentService 文档附件服务（如果提供则会提取并上传图片）
   * @param userId 用户ID（用于上传图片附件）
   * @return HTML格式的文档内容
   * @throws BssException 当文件无法读取或转换失败时抛出
   */
  public static String convertWordToHtml(MultipartFile file, String fileName, String documentId,
    IDocumentAttachmentService documentAttachmentService, Long userId) {
    // 验证文件类型
    if (isNotWordDocument(fileName)) {
      throw new BssException("上传为在线文档仅支持Word文档格式（.doc/.docx）");
    }
    boolean isLegacyDoc = isLegacyWordDocument(fileName);
    // 获取文件大小，用于优化内存使用
    Long fileSize = file.getSize() > 0 ? file.getSize() : null;
    try (InputStream inputStream = file.getInputStream()) {
      if (isLegacyDoc) {
        return processLegacyWordToHtml(inputStream, fileName, documentId, documentAttachmentService, userId);
      }
      return processWordToHtml(inputStream, fileName, documentId, documentAttachmentService, userId, fileSize);
    }
    catch (Docx4JException e) {
      logger.error("Word文档转换失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("Word文档转换失败: " + e.getMessage(), e);
    }
    catch (IOException e) {
      logger.error("读取Word文档失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("读取Word文档失败: " + e.getMessage(), e);
    }
    catch (Exception e) {
      logger.error("Word文档转换过程发生未知错误: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("Word文档转换失败: " + e.getMessage(), e);
    }
  }

  /**
   * 处理Word文档到HTML的转换逻辑
   *
   * @param inputStream Word文档输入流
   * @param fileName 文件名
   * @param documentId 文档ID（必需，用于上传图片附件）
   * @param documentAttachmentService 文档附件服务（如果提供则会提取并上传图片）
   * @param userId 用户ID（用于上传图片附件）
   * @param fileSize 文件大小（字节），可选，用于优化内存使用
   * @return HTML格式的文档内容
   * @throws Docx4JException 当Word文档转换失败时抛出
   * @throws IOException 当IO操作失败时抛出
   */
  public static String processWordToHtml(InputStream inputStream, String fileName, String documentId,
    IDocumentAttachmentService documentAttachmentService, Long userId, Long fileSize)
    throws Docx4JException, IOException {
    if (isLegacyWordDocument(fileName)) {
      return processLegacyWordToHtml(inputStream, fileName, documentId, documentAttachmentService, userId);
    }
    // 使用包装方法加载 Word 文档（含 NaN 清洗）；导出完成后再清理临时文件，确保 docx4j 导出时从已清洗文件读取
    WordPackageLoadResultDTO loadResult = loadWordPackageWithWoodstoxWorkaround(inputStream, fileSize);
    WordprocessingMLPackage wordMLPackage = loadResult.getPkg();
    try {
      return processWordToHtmlWithPackage(wordMLPackage, fileName, documentId, documentAttachmentService, userId);
    }
    finally {
      if (loadResult.getCleanup() != null) {
        loadResult.getCleanup().run();
      }
    }
  }

  /**
   * 在已加载的 Word 包上执行 toHTML 及后续 HTML 处理（供 processWordToHtml 在 try 内调用）
   */
  private static String processWordToHtmlWithPackage(WordprocessingMLPackage wordMLPackage, String fileName,
    String documentId, IDocumentAttachmentService documentAttachmentService, Long userId)
    throws Docx4JException, IOException {
    HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
    htmlSettings.setOpcPackage(wordMLPackage);

    htmlSettings.setImageDirPath("");
    htmlSettings.setImageTargetUri("");
    if (documentAttachmentService != null && StringUtils.isNotBlank(documentId) && userId != null) {
      htmlSettings.setImageHandler(new DocumentConversionImageHandler(documentAttachmentService, documentId, userId));
    }

    // 先注入常用段落/表格样式，避免 ParagraphStylesInTableFix 中 expressStyle 为 null 导致 NPE
    ensureMinimalTableStyles(wordMLPackage);

    String htmlContent;
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      try {
        Docx4J.toHTML(htmlSettings, outputStream, Docx4J.FLAG_NONE);
      }
      catch (Docx4JException e) {
        if (isTableStyleNullCause(e)) {
          logger.warn("Word转HTML因缺失表格样式失败，尝试再次注入样式后重试: {}", e.getMessage());
          ensureMinimalTableStyles(wordMLPackage);
          outputStream.reset();
          Docx4J.toHTML(htmlSettings, outputStream, Docx4J.FLAG_NONE);
        }
        else {
          throw e;
        }
      }
      htmlContent = outputStream.toString(StandardCharsets.UTF_8);
      if (StringUtils.isBlank(htmlContent)) {
        throw new BssException("文档转换结果为空");
      }

      logger.info("Word文档转换为HTML成功: fileName={}, htmlLength={}", fileName, htmlContent.length());
      writeDebugHtmlFile(htmlContent);
    }
    // 清理 HTML 中的样式属性，避免生成 textStyle
    htmlContent = removeStyleAttributes(htmlContent);
    // 将“像标题”的段落（以数字编号开头、较短）转为 h2/h3/h4，便于在线文档识别大纲
    htmlContent = promoteNumberedParagraphsToHeadings(htmlContent);
    return htmlContent;
  }

  /**
   * 将“以数字编号开头”的短段落提升为标题标签（h2～h6），用于原 Word 未使用标题样式、仅手动编号的场景。
   * 匹配如「1 提示词模型适配」「2.1 1.1 概述」「2.1.1 1.1.1 需求背景」等，按编号层级映射为 h2/h3/h4。
   */
  private static String promoteNumberedParagraphsToHeadings(String htmlContent) {
    if (StringUtils.isBlank(htmlContent)) {
      return htmlContent;
    }
    // 匹配 <p...>...内容...</p>，内容可含子标签（如 <b>），且段落内纯文以“数字+点/空格+数字”开头且较短
    Pattern pBlock = Pattern.compile("<p([^>]*)>([\\s\\S]*?)</p>", Pattern.CASE_INSENSITIVE);
    java.util.regex.Matcher m = pBlock.matcher(htmlContent);
    StringBuffer sb = new StringBuffer(htmlContent.length());
    while (m.find()) {
      String open = m.group(1);
      String inner = m.group(2);
      String plain = inner.replaceAll("<[^>]+>", "").trim();
      int headingLevel = calculateHeadingLevel(plain);
      if (headingLevel > 0) {
        String tag = "h" + headingLevel;
        m.appendReplacement(sb, "<" + tag + java.util.regex.Matcher.quoteReplacement(open) + ">"
          + java.util.regex.Matcher.quoteReplacement(inner) + "</" + tag + ">");
      }
      else {
        m.appendReplacement(sb, "<p$1>$2</p>");
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * 根据段落纯文本内容计算标题层级（h2～h6）。
   * 以数字编号开头（支持 1 、2.1 、2.1.1 、2 1 、2.1 1.1 等），且整段较短视为标题。
   *
   * @param plain 段落纯文本
   * @return 标题等级（2～6），不是标题返回 0
   */
  private static int calculateHeadingLevel(String plain) {
    if (plain.length() < 2 || plain.length() > 120) {
      return 0;
    }
    java.util.regex.Matcher matcher = NUMBERING_HEADING_PATTERN.matcher(plain);
    if (!matcher.find()) {
      return 0;
    }
    // matcher.group(1) 是前缀数字编号部分，如 "1"、"2.1"、"2.1.1"、"2 1" 等
    String numbering = matcher.group(1);
    int dotCount = numbering.length() - numbering.replace(".", "").length();
    int spaceCount = numbering.length() - numbering.replace(" ", "").length();
    // 有小数点时按点数定层级；无点但包含空格则视为一级（如「2 1」），否则不是标题
    int depth = dotCount > 0 ? Math.min(dotCount, 3) : spaceCount > 0 ? 1 : 0;
    if (depth <= 0) {
      return 0;
    }
    return Math.min(2 + depth, 6);
  }

  /**
   * 移除 HTML 中的样式属性，避免在转换为 JSON 时生成 textStyle
   * 去掉 style 属性和 color 相关的内联样式
   *
   * @param htmlContent HTML 内容
   * @return 清理后的 HTML 内容
   */
  private static String removeStyleAttributes(String htmlContent) {
    if (StringUtils.isBlank(htmlContent)) {
      return htmlContent;
    }
    // 去掉 style 属性（包括 style="..." 和 style='...'）
    htmlContent = htmlContent.replaceAll("(?i)\\s*style\\s*=\\s*[\"'][^\"']*[\"']", "");
    // 去掉 color 相关的内联样式（如果还有残留）
    htmlContent = htmlContent.replaceAll("(?i)color\\s*:\\s*[^;]+;?", "");
    return htmlContent;
  }

  /**
   * 判断是否为 docx4j 表格样式缺失导致的异常（ParagraphStylesInTableFix 中 expressStyle 为 null 引发 NPE）
   */
  private static boolean isTableStyleNullCause(Docx4JException e) {
    Throwable cause = e.getCause();
    while (cause != null) {
      if (cause instanceof NullPointerException) {
        String msg = cause.getMessage();
        return msg != null && msg.contains("expressStyle");
      }
      cause = cause.getCause();
    }
    return false;
  }

  /**
   * 为文档注入缺失的段落样式（含表格常用样式），避免 docx4j 转 HTML 时
   * ParagraphStylesInTableFix 因 getStyleById 返回 null 而 NPE（Cannot invoke "Style.getPPr()" because "expressStyle" is null）
   */
  private static void ensureMinimalTableStyles(WordprocessingMLPackage pkg) {
    try {
      MainDocumentPart mainPart = pkg.getMainDocumentPart();
      StyleDefinitionsPart stylePart = mainPart.getStyleDefinitionsPart();
      if (stylePart == null) {
        return;
      }
      Styles styles = stylePart.getJaxbElement();
      if (styles == null || styles.getStyle() == null) {
        return;
      }
      java.util.List<Style> styleList = styles.getStyle();
      java.util.Set<String> existingIds = collectExistingStyleIds(styleList);
      ensureDefaultParagraphStyle(stylePart, styleList, existingIds);
      ensureCommonTableAndParagraphStyles(styleList, existingIds);
    }
    catch (Exception ex) {
      logger.warn("注入表格/段落样式失败，将继续使用原文档转换: {}", ex.getMessage());
    }
  }

  /**
   * 收集已有样式 ID，便于后续补充缺失样式。
   */
  private static java.util.Set<String> collectExistingStyleIds(java.util.List<Style> styleList) {
    java.util.Set<String> existingIds = new java.util.HashSet<>();
    for (Style s : styleList) {
      if (s.getStyleId() != null) {
        existingIds.add(s.getStyleId());
      }
    }
    return existingIds;
  }

  /**
   * 确保存在默认段落样式（type=paragraph 且 default=true），避免 ParagraphStylesInTableFix 取默认样式时返回 null。
   */
  private static void ensureDefaultParagraphStyle(StyleDefinitionsPart stylePart,
    java.util.List<Style> styleList, java.util.Set<String> existingIds) {
    Style defaultPStyle = stylePart.getDefaultParagraphStyle();
    if (defaultPStyle == null) {
      // 优先使用/创建 Normal 作为默认段落样式
      Style normal = stylePart.getStyleById("Normal");
      if (normal == null) {
        normal = Context.getWmlObjectFactory().createStyle();
        normal.setStyleId("Normal");
        Style.Name name = Context.getWmlObjectFactory().createStyleName();
        name.setVal("Normal");
        normal.setName(name);
        styleList.add(normal);
        existingIds.add("Normal");
      }
      if (normal.getType() == null) {
        normal.setType("paragraph");
      }
      if (normal.getPPr() == null) {
        normal.setPPr(Context.getWmlObjectFactory().createPPr());
      }
      // 将 Normal 标记为默认段落样式（w:default="1"），避免 getDefaultParagraphStyle 返回 null
      normal.setDefault(true);
    }
    else {
      // 已存在默认段落样式，但可能缺少 PPr，补上以防 expressStyle.getPPr() NPE
      if (defaultPStyle.getPPr() == null) {
        defaultPStyle.setPPr(Context.getWmlObjectFactory().createPPr());
      }
      if (defaultPStyle.getType() == null) {
        defaultPStyle.setType("paragraph");
      }
    }
  }

  /**
   * 补充表格/段落常用样式并设置 PPr，避免 ParagraphStylesInTableFix 中 expressStyle 为 null。
   */
  private static void ensureCommonTableAndParagraphStyles(java.util.List<Style> styleList,
    java.util.Set<String> existingIds) {
    String[] styleIds = {
      "TableHeading", "TableContents", "Normal", "TableNormal",
      "Caption", "Heading1", "Heading2", "Heading3", "TOCHeading",
      "ListParagraph", "FootnoteText", "Header", "Footer"
    };
    for (String styleId : styleIds) {
      if (existingIds.contains(styleId)) {
        continue;
      }
      Style style = Context.getWmlObjectFactory().createStyle();
      style.setStyleId(styleId);
      Style.Name name = Context.getWmlObjectFactory().createStyleName();
      name.setVal(styleId);
      style.setName(name);
      PPr pPr = Context.getWmlObjectFactory().createPPr();
      style.setPPr(pPr);
      styleList.add(style);
      existingIds.add(styleId);
    }
  }

  /**
   * 加载 Word 文档包，处理 Woodstox 7.x 与 docx4j 的兼容性问题
   * <p>
   * docx4j 11.5.6 尝试在 Woodstox 7.x 的 XMLInputFactory 上设置 ACCESS_EXTERNAL_DTD 属性， 但 Woodstox 7.x 不支持此属性，会抛出
   * IllegalArgumentException。 该方法通过捕获异常并检查异常链，在检测到兼容性问题时提供详细的错误信息和解决方案。
   * </p>
   * <p>
   * 注意：为了确保与浏览器上传方式的一致性，该方法会将输入流缓冲， 以便 docx4j 可以多次读取流内容（docx4j 可能需要多次读取 ZIP 条目）。 为了避免
   * OOM，小文件（&lt;50MB）使用内存缓冲，大文件使用临时文件。
   * </p>
   *
   * @param inputStream Word 文档输入流（可能是网络流或文件流，不支持多次读取）
   * @param fileSize 文件大小（字节），如果为 null 或 &lt;0，将在读取时动态检测
   * @return WordprocessingMLPackage 对象
   * @throws Docx4JException 如果加载失败
   * @throws IOException 如果 IO 操作失败
   */
  private static WordPackageLoadResultDTO loadWordPackageWithWoodstoxWorkaround(InputStream inputStream, Long fileSize)
    throws Docx4JException, IOException {
    // 始终使用缓冲方案，且从「已清洗 NaN 的临时文件」加载，确保 docx4j 在 toHTML 时从同一文件读取 part，避免延迟反序列化仍读到 NaN。
    if (fileSize != null && fileSize > 0 && fileSize <= MAX_MEMORY_BUFFER_SIZE) {
      return loadWordPackageFromMemory(inputStream);
    }
    if (fileSize != null && fileSize > MAX_MEMORY_BUFFER_SIZE) {
      logger.debug("文件大小 {} 超过内存缓冲限制 {}，使用临时文件", fileSize, MAX_MEMORY_BUFFER_SIZE);
      return loadWordPackageFromTempFile(inputStream);
    }
    return loadWordPackageWithDynamicBuffer(inputStream);
  }

  /**
   * 从内存缓冲加载：先清洗 NaN，写入临时文件，再从文件加载；临时文件在 toHTML 完成后由调用方清理。
   *
   * @param inputStream 输入流
   * @return 加载结果（含包与清理回调）
   */
  private static WordPackageLoadResultDTO loadWordPackageFromMemory(InputStream inputStream)
    throws Docx4JException, IOException {
    byte[] documentBytes;
    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      byte[] data = new byte[8192];
      int bytesRead;
      long totalBytes = 0;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        totalBytes += bytesRead;
        if (totalBytes > MAX_MEMORY_BUFFER_SIZE) {
          throw new IOException(
            String.format("文件大小（%d 字节）超过内存缓冲限制（%d 字节），请使用临时文件方案", totalBytes, MAX_MEMORY_BUFFER_SIZE));
        }
        buffer.write(data, 0, bytesRead);
      }
      documentBytes = buffer.toByteArray();
    }

    byte[] bytesToLoad = documentBytes;
    try {
      bytesToLoad = sanitizeDocxNan(documentBytes);
    }
    catch (IOException e) {
      logger.warn("NaN 清洗失败，使用原始字节: {}", e.getMessage());
    }

    Path tempFile = Files.createTempFile("docx4j_", ".docx");
    try {
      Files.write(tempFile, bytesToLoad, StandardOpenOption.TRUNCATE_EXISTING);
      WordprocessingMLPackage pkg = Docx4J.load(tempFile.toFile());
      return new WordPackageLoadResultDTO(pkg, () -> {
        try {
          Files.deleteIfExists(tempFile);
        }
        catch (IOException e) {
          logger.warn("删除临时文件失败: {}", tempFile, e);
        }
      });
    }
    catch (Exception e) {
      try {
        Files.deleteIfExists(tempFile);
      }
      catch (IOException ex) {
        logger.debug("加载失败时删除临时文件失败: {}", tempFile, ex);
      }
      if (isNanRelatedException(e)) {
        logger.warn("Word 文档内存在非法数值 NaN，清洗后仍失败: {}", e.getMessage());
      }
      throw handleDocx4JException(e);
    }
  }

  /**
   * 从临时文件加载（大文件）：写入临时文件后先清洗 NaN 再覆盖，从文件加载；临时文件在 toHTML 完成后由调用方清理。
   *
   * @param inputStream 输入流
   * @return 加载结果（含包与清理回调）
   */
  private static WordPackageLoadResultDTO loadWordPackageFromTempFile(InputStream inputStream) throws Docx4JException {
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("docx4j_", ".docx");
      Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      byte[] bytes = Files.readAllBytes(tempFile);
      try {
        byte[] sanitized = sanitizeDocxNan(bytes);
        Files.write(tempFile, sanitized, StandardOpenOption.TRUNCATE_EXISTING);
      }
      catch (IOException e) {
        logger.warn("NaN 清洗失败，使用原始文件: {}", e.getMessage());
      }
      WordprocessingMLPackage pkg = Docx4J.load(tempFile.toFile());
      final Path pathToDelete = tempFile;
      return new WordPackageLoadResultDTO(pkg, () -> {
        try {
          Files.deleteIfExists(pathToDelete);
        }
        catch (IOException e) {
          logger.warn("删除临时文件失败: {}", pathToDelete, e);
        }
      });
    }
    catch (IOException e) {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        }
        catch (IOException ex) {
          logger.debug("IO 异常时删除临时文件失败: {}", tempFile, ex);
        }
      }
      throw handleDocx4JException(e);
    }
    catch (Exception e) {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        }
        catch (IOException ex) {
          logger.debug("异常时删除临时文件失败: {}", tempFile, ex);
        }
      }
      throw handleDocx4JException(e);
    }
  }

  /**
   * 动态缓冲加载（文件大小未知时使用）
   *
   * @param inputStream 输入流
   * @return 加载结果（含包与清理回调）
   */
  private static WordPackageLoadResultDTO loadWordPackageWithDynamicBuffer(InputStream inputStream)
    throws Docx4JException, IOException {
    try {
      // 先尝试内存缓冲
      return loadWordPackageFromMemory(inputStream);
    }
    catch (IOException e) {
      // 如果是超过内存限制的异常，使用临时文件方案
      if (e.getMessage() != null && e.getMessage().contains("超过内存缓冲限制")) {
        logger.debug("动态检测到文件超过内存限制，切换到临时文件方案: {}", e.getMessage());
        // 注意：此时输入流已部分读取，无法再次使用
        // 实际上这种情况应该由调用方在知道文件大小时提前选择方案
        throw new IOException("无法动态切换缓冲方案，请在调用时提供文件大小信息", e);
      }
      throw e;
    }
  }

  /**
   * 判断异常链是否由 document.xml 中非法数值 "NaN" 导致（部分 Word 导出或损坏文档会包含该值，JAXB 反序列化会报 NumberFormatException）。
   */
  private static boolean isNanRelatedException(Throwable e) {
    Throwable t = e;
    while (t != null) {
      if (t instanceof NumberFormatException && t.getMessage() != null && t.getMessage().contains("NaN")) {
        return true;
      }
      t = t.getCause();
    }
    return false;
  }

  /**
   * 清洗 docx 字节流中 XML 部件内的非法或非常规数值，避免 JAXB 反序列化报错。
   * - "NaN"（不区分大小写）→ "0"
   * - 属性中的百分数如 "100%" → "100"（OOXML 中部分属性按整数解析，带 % 会报 NumberFormatException）
   * 仅处理 zip 内 .xml 条目，其它条目原样复制。
   *
   * @param docxBytes 原始 docx 字节数组
   * @return 清洗后的 docx 字节数组
   * @throws IOException 读写 zip 时发生异常
   */
  private static byte[] sanitizeDocxNan(byte[] docxBytes) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(docxBytes));
         ByteArrayOutputStream out = new ByteArrayOutputStream(docxBytes.length * 2);
         ZipOutputStream zos = new ZipOutputStream(out)) {
      byte[] buf = new byte[8192];
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String name = entry.getName();
        boolean isXml = name.endsWith(".xml");
        zos.putNextEntry(new ZipEntry(name));
        if (isXml) {
          ByteArrayOutputStream entryOut = new ByteArrayOutputStream();
          int n;
          while ((n = zis.read(buf, 0, buf.length)) != -1) {
            entryOut.write(buf, 0, n);
          }
          String content = entryOut.toString(StandardCharsets.UTF_8);
          content = sanitizeXmlNumericValues(content);
          zos.write(content.getBytes(StandardCharsets.UTF_8));
        } else {
          int n;
          while ((n = zis.read(buf, 0, buf.length)) != -1) {
            zos.write(buf, 0, n);
          }
        }
        zos.closeEntry();
        zis.closeEntry();
      }
      zos.finish();
      return out.toByteArray();
    }
  }

  /**
   * 清洗 XML 字符串中会导致 JAXB 整数解析失败的值：NaN → 0，数字+% → 数字。
   */
  private static String sanitizeXmlNumericValues(String xmlContent) {
    if (xmlContent == null || xmlContent.isEmpty()) {
      return xmlContent;
    }
    // NaN（不区分大小写）→ 0，避免 NumberFormatException: For input string: "NaN"
    String s = xmlContent.replaceAll("(?i)NaN", "0");
    // 属性中 "100%" 等 → "100"，避免 NumberFormatException: For input string: "100%"
    s = s.replaceAll("(\\d+)%", "$1");
    return s;
  }

  /**
   * 统一处理 docx4j 加载异常，检查 Woodstox 兼容性问题
   *
   * @param e 原始异常
   * @return 处理后的异常
   */
  private static Docx4JException handleDocx4JException(Exception e) {
    // 检查异常链中是否包含 ACCESS_EXTERNAL_DTD 相关的错误
    Throwable rootCause = e;
    while (rootCause != null) {
      String errorMessage = rootCause.getMessage();
      if (errorMessage != null && errorMessage.contains("Unrecognized property")
        && errorMessage.contains("accessExternalDTD")) {
        logger.error("检测到 Woodstox 7.x 与 docx4j 11.5.6 的兼容性问题: {}", errorMessage);
        // 提供详细的解决建议
        return new Docx4JException("Word文档加载失败：docx4j 11.5.6 与 Woodstox 7.x 存在兼容性问题。\n" + "错误原因: " + errorMessage + "\n"
          + "解决方案:\n" + "1. 在应用启动时添加 JVM 参数: -Djavax.xml.accessExternalDTD=all\n"
          + "2. 或者在 pom.xml 中排除 Woodstox 7.x，强制使用 6.x 版本:\n" + "   <dependency>\n"
          + "     <groupId>com.fasterxml.woodstox</groupId>\n" + "     <artifactId>woodstox-core</artifactId>\n"
          + "     <version>6.5.1</version>\n" + "   </dependency>\n" + "3. 或者等待 docx4j 更新以支持 Woodstox 7.x", e);
      }
      rootCause = rootCause.getCause();
    }
    // 如果不是兼容性问题，重新抛出原始异常
    if (e instanceof Docx4JException) {
      return (Docx4JException) e;
    }
    return new Docx4JException("Word文档加载失败: " + e.getMessage(), e);
  }

  /**
   * 判断文件是否不为Word文档
   *
   * @param fileName 文件名
   * @return 如果是Word文档返回false，否则返回true
   */
  public static boolean isNotWordDocument(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return true;
    }

    String lowerCaseFileName = fileName.toLowerCase();
    return !lowerCaseFileName.endsWith(".doc") && !lowerCaseFileName.endsWith(".docx");
  }

  /**
   * 将 HTML 内容写入调试文件。
   */
  private static void writeDebugHtmlFile(String htmlContent) {
    try {
      Path filePath = Paths.get(System.getProperty("java.io.tmpdir"), "httt.html");
      Files.writeString(filePath, htmlContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      logger.info("HTML 调试文件写入完成: {}", filePath);
    }
    catch (IOException e) {
      logger.warn("写入调试 HTML 文件失败: {}", e.getMessage());
    }
  }

  private static boolean isLegacyWordDocument(String fileName) {
    return StringUtils.defaultString(fileName).toLowerCase().endsWith(".doc");
  }

  private static String processLegacyWordToHtml(InputStream inputStream, String fileName, String documentId,
    IDocumentAttachmentService documentAttachmentService, Long userId) {
    byte[] documentBytes = bufferInputStream(inputStream, fileName);

    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentBytes);
      HWPFDocument document = new HWPFDocument(byteArrayInputStream)) {
      return convertLegacyWordDocumentToHtml(document, fileName, documentAttachmentService, documentId, userId);
    }
    catch (OfficeXmlFileException e) {
      logger.info("检测到文件 {} 为 OOXML 格式（.docx），使用 docx4j 处理", fileName);
      return handleOoxmlFormatFile(documentBytes, fileName, documentId, documentAttachmentService, userId);
    }
    catch (ParserConfigurationException | TransformerException e) {
      logger.error("旧版Word文档转换失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("Word文档转换失败: " + e.getMessage(), e);
    }
    catch (IOException e) {
      logger.error("读取旧版Word文档失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("读取Word文档失败: " + e.getMessage(), e);
    }
    catch (Exception e) {
      logger.error("旧版Word文档处理失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("Word文档转换失败: " + e.getMessage(), e);
    }
  }

  /**
   * 缓冲输入流为字节数组
   *
   * @param inputStream 输入流
   * @param fileName 文件名（用于错误日志）
   * @return 字节数组
   * @throws BssException 如果读取失败
   */
  private static byte[] bufferInputStream(InputStream inputStream, String fileName) {
    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      byte[] data = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, bytesRead);
      }
      return buffer.toByteArray();
    }
    catch (IOException e) {
      logger.error("读取输入流失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("读取Word文档失败: " + e.getMessage(), e);
    }
  }

  /**
   * 转换旧版 Word 文档为 HTML
   *
   * @param document HWPFDocument 对象
   * @param fileName 文件名
   * @param documentAttachmentService 文档附件服务
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return HTML 内容
   * @throws ParserConfigurationException 解析配置异常
   * @throws TransformerException 转换异常
   */
  private static String convertLegacyWordDocumentToHtml(HWPFDocument document, String fileName,
    IDocumentAttachmentService documentAttachmentService, String documentId, Long userId)
    throws ParserConfigurationException, TransformerException {
    SafeWordToHtmlConverter wordToHtmlConverter = new SafeWordToHtmlConverter(
      DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
    wordToHtmlConverter.setPicturesManager(new UploadingPicturesManager(documentAttachmentService, documentId, userId));

    wordToHtmlConverter.processDocument(document);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Transformer serializer = TransformerFactory.newInstance().newTransformer();
    serializer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
    serializer.setOutputProperty(OutputKeys.METHOD, "html");
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    serializer.transform(new DOMSource(wordToHtmlConverter.getDocument()), new StreamResult(out));
    String htmlContent = out.toString(StandardCharsets.UTF_8);
    if (StringUtils.isBlank(htmlContent)) {
      throw new BssException("文档转换结果为空");
    }
    logger.info("Word(.doc)文档转换为HTML成功: fileName={}, htmlLength={}", fileName, htmlContent.length());
    writeDebugHtmlFile(htmlContent);
    // 清理 HTML 中的样式属性，避免生成 textStyle
    htmlContent = removeStyleAttributes(htmlContent);
    return htmlContent;
  }

  /**
   * 处理检测到的 OOXML 格式文件（.docx）
   *
   * @param documentBytes 文档字节数组
   * @param fileName 文件名
   * @param documentId 文档ID
   * @param documentAttachmentService 文档附件服务
   * @param userId 用户ID
   * @return HTML 内容
   * @throws BssException 如果转换失败
   */
  private static String handleOoxmlFormatFile(byte[] documentBytes, String fileName, String documentId,
    IDocumentAttachmentService documentAttachmentService, Long userId) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(documentBytes)) {
      WordPackageLoadResultDTO loadResult = loadWordPackageWithWoodstoxWorkaround(byteArrayInputStream,
        (long) documentBytes.length);
      try {
        return processWordToHtmlWithPackage(loadResult.getPkg(), fileName, documentId, documentAttachmentService, userId);
      }
      finally {
        if (loadResult.getCleanup() != null) {
          loadResult.getCleanup().run();
        }
      }
    }
    catch (Docx4JException | IOException e) {
      logger.error("使用 docx4j 处理 OOXML 文件失败: fileName={}, error={}", fileName, e.getMessage(), e);
      throw new BssException("Word文档转换失败: " + e.getMessage(), e);
    }
  }
}
