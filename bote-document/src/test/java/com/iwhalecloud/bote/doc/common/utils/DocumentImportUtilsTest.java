package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link DocumentImportUtils} 单元测试。
 *
 * <p>该类大部分公共方法依赖 docx4j 对真实 .docx 的重转换（重量级、属集成测试范畴），
 * 故按可测性聚焦于纯逻辑：公共守卫 {@code isNotWordDocument}、转换守卫异常，
 * 以及经反射访问的私有纯方法（标题层级计算、HTML 样式清洗、docx NaN 清洗、异常链判定）。
 * 不启动 Spring 容器、不联网。</p>
 */
class DocumentImportUtilsTest {

  // ============ isNotWordDocument（公共） ============

  @Test
  void isNotWordDocument_blankOrNonWord_returnsTrue() {
    assertThat(DocumentImportUtils.isNotWordDocument(null)).isTrue();
    assertThat(DocumentImportUtils.isNotWordDocument("")).isTrue();
    assertThat(DocumentImportUtils.isNotWordDocument("  ")).isTrue();
    assertThat(DocumentImportUtils.isNotWordDocument("report.pdf")).isTrue();
    assertThat(DocumentImportUtils.isNotWordDocument("notes.txt")).isTrue();
  }

  @Test
  void isNotWordDocument_wordExtension_returnsFalse() {
    assertThat(DocumentImportUtils.isNotWordDocument("a.doc")).isFalse();
    assertThat(DocumentImportUtils.isNotWordDocument("a.docx")).isFalse();
    // 大小写不敏感
    assertThat(DocumentImportUtils.isNotWordDocument("A.DOCX")).isFalse();
  }

  // ============ convertWordToHtml 守卫 ============

  @Test
  void convertWordToHtml_nonWord_throwsBssException() {
    MultipartFile file = mock(MultipartFile.class);
    assertThatThrownBy(() ->
      DocumentImportUtils.convertWordToHtml(file, "not-word.pdf", "doc-1", null, 1L))
      .isInstanceOf(BssException.class)
      .hasMessageContaining("仅支持Word文档格式");
  }

  // ============ calculateHeadingLevel（私有） ============

  private int calculateHeadingLevel(String plain) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod("calculateHeadingLevel", String.class);
    m.setAccessible(true);
    return (int) m.invoke(null, plain);
  }

  @Test
  void calculateHeadingLevel_singleLevelNotHeading_returnsZero() throws Exception {
    // 编号仅为单个数字（无点/无内部空格）不视为标题
    assertThat(calculateHeadingLevel("1 概述")).isZero();
    assertThat(calculateHeadingLevel("1")).isZero();
  }

  @Test
  void calculateHeadingLevel_tooShortOrLong_returnsZero() throws Exception {
    assertThat(calculateHeadingLevel("")).isZero();
    assertThat(calculateHeadingLevel("x")).isZero();
    String tooLong = "2.1 " + "a".repeat(130);
    assertThat(calculateHeadingLevel(tooLong)).isZero();
  }

  @Test
  void calculateHeadingLevel_nonNumericPrefix_returnsZero() throws Exception {
    assertThat(calculateHeadingLevel("概述内容")).isZero();
    assertThat(calculateHeadingLevel("abc def")).isZero();
  }

  @Test
  void calculateHeadingLevel_dottedNumbering_mapsToDepth() throws Exception {
    assertThat(calculateHeadingLevel("2.1 概述")).isEqualTo(3);
    assertThat(calculateHeadingLevel("2.1.1 概述")).isEqualTo(4);
    assertThat(calculateHeadingLevel("2.1.1.1 概述")).isEqualTo(5);
    // 超过 3 级点仍封顶到 h5
    assertThat(calculateHeadingLevel("2.1.1.1.1 概述")).isEqualTo(5);
  }

  @Test
  void calculateHeadingLevel_spaceSeparatedNumbering_mapsToH3() throws Exception {
    // 「2 1」式：无点但编号内含空格 -> depth 1 -> h3
    assertThat(calculateHeadingLevel("2 1 概述")).isEqualTo(3);
  }

  // ============ promoteNumberedParagraphsToHeadings（私有） ============

  private String promoteNumberedParagraphsToHeadings(String html) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod(
      "promoteNumberedParagraphsToHeadings", String.class);
    m.setAccessible(true);
    return (String) m.invoke(null, html);
  }

  @Test
  void promoteNumberedParagraphsToHeadings_blank_returnsAsIs() throws Exception {
    assertThat(promoteNumberedParagraphsToHeadings("")).isEmpty();
    assertThat(promoteNumberedParagraphsToHeadings("   ")).isEqualTo("   ");
  }

  @Test
  void promoteNumberedParagraphsToHeadings_promotesNumberedParagraph() throws Exception {
    String html = "<p>2.1 概述</p>";
    assertThat(promoteNumberedParagraphsToHeadings(html)).isEqualTo("<h3>2.1 概述</h3>");
  }

  @Test
  void promoteNumberedParagraphsToHeadings_keepsAttributesAndInnerTags() throws Exception {
    String html = "<p class=\"x\"><b>2.1</b> 概述</p>";
    assertThat(promoteNumberedParagraphsToHeadings(html))
      .isEqualTo("<h3 class=\"x\"><b>2.1</b> 概述</h3>");
  }

  @Test
  void promoteNumberedParagraphsToHeadings_plainParagraphUnchanged() throws Exception {
    String html = "<p>这是普通正文段落</p>";
    assertThat(promoteNumberedParagraphsToHeadings(html)).isEqualTo(html);
  }

  // ============ removeStyleAttributes（私有） ============

  private String removeStyleAttributes(String html) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod("removeStyleAttributes", String.class);
    m.setAccessible(true);
    return (String) m.invoke(null, html);
  }

  @Test
  void removeStyleAttributes_blank_returnsAsIs() throws Exception {
    assertThat(removeStyleAttributes("")).isEmpty();
    assertThat(removeStyleAttributes(null)).isNull();
  }

  @Test
  void removeStyleAttributes_stripsDoubleAndSingleQuotedStyle() throws Exception {
    assertThat(removeStyleAttributes("<p style=\"color:red\">x</p>")).isEqualTo("<p>x</p>");
    assertThat(removeStyleAttributes("<p style='color:red'>x</p>")).isEqualTo("<p>x</p>");
  }

  @Test
  void removeStyleAttributes_stripsResidualColorDecl() throws Exception {
    // 残留 color 声明
    assertThat(removeStyleAttributes("color:red;font-size:12px;"))
      .doesNotContain("color:red");
  }

  @Test
  void removeStyleAttributes_noStyle_unchanged() throws Exception {
    String html = "<p>普通文本</p>";
    assertThat(removeStyleAttributes(html)).isEqualTo(html);
  }

  // ============ sanitizeXmlNumericValues（私有） ============

  private String sanitizeXmlNumericValues(String xml) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod(
      "sanitizeXmlNumericValues", String.class);
    m.setAccessible(true);
    return (String) m.invoke(null, xml);
  }

  @Test
  void sanitizeXmlNumericValues_nullOrEmpty_returnsAsIs() throws Exception {
    assertThat(sanitizeXmlNumericValues(null)).isNull();
    assertThat(sanitizeXmlNumericValues("")).isEmpty();
  }

  @Test
  void sanitizeXmlNumericValues_replacesNanCaseInsensitive() throws Exception {
    assertThat(sanitizeXmlNumericValues("<v val=\"NaN\"/>")).isEqualTo("<v val=\"0\"/>");
    assertThat(sanitizeXmlNumericValues("<v val=\"nan\"/>")).isEqualTo("<v val=\"0\"/>");
  }

  @Test
  void sanitizeXmlNumericValues_stripsPercentSuffix() throws Exception {
    assertThat(sanitizeXmlNumericValues("<w:pct val=\"100%\"/>"))
      .isEqualTo("<w:pct val=\"100\"/>");
  }

  @Test
  void sanitizeXmlNumericValues_normalXml_unchanged() throws Exception {
    String xml = "<root><item>text</item></root>";
    assertThat(sanitizeXmlNumericValues(xml)).isEqualTo(xml);
  }

  // ============ sanitizeDocxNan（私有，zip 包装） ============

  private byte[] sanitizeDocxNan(byte[] docxBytes) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod("sanitizeDocxNan", byte[].class);
    m.setAccessible(true);
    return (byte[]) m.invoke(null, docxBytes);
  }

  @Test
  void sanitizeDocxNan_onlyXmlEntriesCleaned() throws Exception {
    // 构造 zip：一个 .xml 条目含 NaN/100%，一个 .bin 条目原样
    ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {
      zos.putNextEntry(new ZipEntry("word/document.xml"));
      zos.write("<v val=\"NaN\" pct=\"100%\"/>".getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("media/image1.bin"));
      zos.write(new byte[]{1, 2, 3});
      zos.closeEntry();
    }
    byte[] sanitized = sanitizeDocxNan(zipOut.toByteArray());

    String xmlEntry = null;
    byte[] binEntry = null;
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(sanitized))) {
      ZipEntry e;
      while ((e = zis.getNextEntry()) != null) {
        byte[] data = zis.readAllBytes();
        if (e.getName().endsWith(".xml")) {
          xmlEntry = new String(data, StandardCharsets.UTF_8);
        } else {
          binEntry = data;
        }
      }
    }
    assertThat(xmlEntry).isNotNull();
    assertThat(xmlEntry).contains("\"0\"").contains("\"100\"");
    assertThat(xmlEntry).doesNotContain("NaN").doesNotContain("100%");
    assertThat(binEntry).containsExactly(1, 2, 3);
  }

  // ============ isTableStyleNullCause（私有） ============

  private boolean isTableStyleNullCause(Docx4JException e) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod(
      "isTableStyleNullCause", Docx4JException.class);
    m.setAccessible(true);
    return (boolean) m.invoke(null, e);
  }

  @Test
  void isTableStyleNullCause_npeWithExpressStyleMessage_returnsTrue() throws Exception {
    Docx4JException e = new Docx4JException("conv",
      new NullPointerException("Cannot invoke \"Style.getPPr()\" because \"expressStyle\" is null"));
    assertThat(isTableStyleNullCause(e)).isTrue();
  }

  @Test
  void isTableStyleNullCause_otherNpeOrNoNpe_returnsFalse() throws Exception {
    assertThat(isTableStyleNullCause(
      new Docx4JException("x", new NullPointerException("other")))).isFalse();
    assertThat(isTableStyleNullCause(
      new Docx4JException("x", new RuntimeException("no npe")))).isFalse();
    assertThat(isTableStyleNullCause(new Docx4JException("x"))).isFalse();
  }

  // ============ isNanRelatedException（私有） ============

  private boolean isNanRelatedException(Throwable e) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod("isNanRelatedException", Throwable.class);
    m.setAccessible(true);
    return (boolean) m.invoke(null, e);
  }

  @Test
  void isNanRelatedException_nanNumberFormatException_returnsTrue() throws Exception {
    assertThat(isNanRelatedException(new NumberFormatException("For input string: \"NaN\"")))
      .isTrue();
    // 嵌套在 cause 链中
    assertThat(isNanRelatedException(
      new RuntimeException("outer", new NumberFormatException("NaN"))))
      .isTrue();
  }

  @Test
  void isNanRelatedException_otherException_returnsFalse() throws Exception {
    assertThat(isNanRelatedException(new NumberFormatException("42"))).isFalse();
    assertThat(isNanRelatedException(new RuntimeException("plain"))).isFalse();
  }

  // ============ handleDocx4JException（私有） ============

  private Docx4JException handleDocx4JException(Exception e) throws Exception {
    Method m = DocumentImportUtils.class.getDeclaredMethod("handleDocx4JException", Exception.class);
    m.setAccessible(true);
    return (Docx4JException) m.invoke(null, e);
  }

  @Test
  void handleDocx4JException_woodstoxCompat_returnsDescriptiveMessage() throws Exception {
    Exception e = new RuntimeException("Unrecognized property 'accessExternalDTD'");
    Docx4JException result = handleDocx4JException(e);
    assertThat(result.getMessage()).contains("兼容性问题");
    assertThat(result.getMessage()).contains("accessExternalDTD");
    assertThat(result.getCause()).isSameAs(e);
  }

  @Test
  void handleDocx4JException_docx4jExceptionReturnedAsIs() throws Exception {
    Docx4JException original = new Docx4JException("already docx4j");
    assertThat(handleDocx4JException(original)).isSameAs(original);
  }

  @Test
  void handleDocx4JException_otherException_wrappedWithGenericMessage() throws Exception {
    Exception e = new IOException("io fail");
    Docx4JException result = handleDocx4JException(e);
    assertThat(result.getMessage()).contains("Word文档加载失败");
    assertThat(result.getCause()).isSameAs(e);
  }
}
