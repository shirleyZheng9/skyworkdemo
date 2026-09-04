package com.iwhalecloud.bote.doc.common.utils.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link SafeWordToHtmlConverter} 单元测试（按可测性补纯解析方法）。
 *
 * <p>该类继承 POI {@code WordToHtmlConverter}，核心段落处理流程（processParagraphes/
 * enhanceParagraphStyles/processParagraphSafely）深度耦合 POI {@code Range}/{@code Paragraph}
 * 等具体类，属集成测试范畴，按计划跳过。此处经反射覆盖可纯测的私有方法：
 * 颜色索引映射、整数反射取值、字体样式构建、列表判定/级别/创建、段落/列表项空判定、
 * 字符格式包装、字符运行属性读取（mock {@link CharacterRun}）。不启动 Spring 容器、不联网、不连 DB。</p>
 */
class SafeWordToHtmlConverterTest {

  private static Document doc;
  private static SafeWordToHtmlConverter converter;

  @BeforeAll
  static void setUpFixture() throws Exception {
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    converter = new SafeWordToHtmlConverter(doc);
  }

  // ---- convertColorIndexToHex ----

  @Test
  void convertColorIndexToHex_mapsKnownIndices() throws Exception {
    assertThat(invokeColorHex(0)).isEqualTo("#000000");
    assertThat(invokeColorHex(1)).isEqualTo("#0000FF");
    assertThat(invokeColorHex(5)).isEqualTo("#FF0000");
    assertThat(invokeColorHex(7)).isEqualTo("#FFFFFF");
    assertThat(invokeColorHex(15)).isEqualTo("#C0C0C0");
  }

  @Test
  void convertColorIndexToHex_returnsBlack_forUnknownIndex() throws Exception {
    assertThat(invokeColorHex(99)).isEqualTo("#000000");
    assertThat(invokeColorHex(-1)).isEqualTo("#000000");
  }

  // ---- getIntegerValue (static) ----

  @Test
  void getIntegerValue_handlesIntegerAndShortAndMissingAndThrowing() throws Exception {
    IntTarget target = new IntTarget();
    assertThat(invokeGetIntegerValue(target, "getI")).isEqualTo(42); // int -> Integer
    assertThat(invokeGetIntegerValue(target, "getS")).isEqualTo(7); // short -> Short
    assertThat(invokeGetIntegerValue(target, "getStr")).isNull(); // 非 Integer/Short
    assertThat(invokeGetIntegerValue(target, "noSuch")).isNull(); // NoSuchMethodException
    assertThat(invokeGetIntegerValue(target, "getThrowing")).isNull(); // 反射调用抛异常
  }

  // ---- buildFontStyle ----

  @Test
  void buildFontStyle_returnsNull_whenNoStyle() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    assertThat(invokeBuildFontStyle(info)).isNull();
  }

  @Test
  void buildFontStyle_buildsCombinedStyle() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setFontSize(12);
    info.setFontColor("#FF0000");
    info.setFontName("Arial");
    assertThat(invokeBuildFontStyle(info)).isEqualTo("font-size: 12pt; color: #FF0000; font-family: 'Arial';");
  }

  @Test
  void buildFontStyle_skipsNonPositiveFontSize() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setFontSize(0);
    info.setFontColor("#0000FF");
    assertThat(invokeBuildFontStyle(info)).isEqualTo("color: #0000FF;");
  }

  // ---- isParagraphInList ----

  @Test
  void isParagraphInList_branches() throws Exception {
    assertThat(invokeIsParagraphInList(5, null)).isTrue(); // listID > 0
    assertThat(invokeIsParagraphInList(null, 2)).isTrue(); // listLevel >= 0
    assertThat(invokeIsParagraphInList(0, 0)).isTrue(); // listLevel 0 >= 0
    assertThat(invokeIsParagraphInList(null, null)).isFalse();
    assertThat(invokeIsParagraphInList(0, -1)).isFalse(); // 0 not >0, -1 not >=0
    assertThat(invokeIsParagraphInList(-1, null)).isFalse();
  }

  // ---- determineListOrder ----

  @Test
  void determineListOrder_returnsTrueWhenListNonNull_otherwiseFalse() throws Exception {
    HWPFList list = mock(HWPFList.class);
    assertThat(invokeDetermineListOrder(list, 1, 0)).isTrue();
    assertThat(invokeDetermineListOrder(null, 5, null)).isFalse(); // listID 非空
    assertThat(invokeDetermineListOrder(null, null, 2)).isFalse(); // listLevel 非空
    assertThat(invokeDetermineListOrder(null, null, null)).isFalse();
  }

  // ---- getCurrentListLevel ----

  @Test
  void getCurrentListLevel_branches() throws Exception {
    assertThat(invokeGetCurrentListLevel(null)).isZero();
    assertThat(invokeGetCurrentListLevel(-1)).isZero();
    assertThat(invokeGetCurrentListLevel(3)).isEqualTo(3);
  }

  // ---- shouldCreateNewList ----

  @Test
  void shouldCreateNewList_branches() throws Exception {
    // 空栈 -> 新建
    assertThat(invokeShouldCreateNewList(newContext(), 1)).isTrue();
    // 末尾级别 1 < 当前 3 -> 新建
    assertThat(invokeShouldCreateNewList(contextWithLevel(1), 3)).isTrue();
    // 末尾级别 3 >= 当前 1 -> 不新建
    assertThat(invokeShouldCreateNewList(contextWithLevel(3), 1)).isFalse();
    // 末尾级别 3 >= 当前 3 -> 不新建
    assertThat(invokeShouldCreateNewList(contextWithLevel(3), 3)).isFalse();
  }

  // ---- isListTablesNullError ----

  @Test
  void isListTablesNullError_branches() throws Exception {
    assertThat(invokeIsListTablesNullError(new RuntimeException("listTables is null"))).isTrue();
    assertThat(invokeIsListTablesNullError(new RuntimeException("ListTables foo"))).isTrue();
    assertThat(invokeIsListTablesNullError(new RuntimeException("other error"))).isFalse();
    assertThat(invokeIsListTablesNullError(new RuntimeException())).isFalse(); // message null
  }

  // ---- isEmptyParagraph ----

  @Test
  void isEmptyParagraph_branches() throws Exception {
    Element empty = doc.createElement("p");
    assertThat(invokeIsEmptyParagraph(empty)).isTrue();

    Element withText = doc.createElement("p");
    withText.setTextContent("hello");
    assertThat(invokeIsEmptyParagraph(withText)).isFalse();

    Element withChild = doc.createElement("p");
    withChild.appendChild(doc.createElement("b"));
    assertThat(invokeIsEmptyParagraph(withChild)).isFalse();
  }

  // ---- hasListItemContent ----

  @Test
  void hasListItemContent_branches() throws Exception {
    Element withChild = doc.createElement("li");
    withChild.appendChild(doc.createElement("b"));
    assertThat(invokeHasListItemContent(withChild)).isTrue();

    Element withText = doc.createElement("li");
    withText.setTextContent("item");
    assertThat(invokeHasListItemContent(withText)).isTrue();

    Element empty = doc.createElement("li");
    assertThat(invokeHasListItemContent(empty)).isFalse();
  }

  // ---- wrapTextWithFormatting ----

  @Test
  void wrapTextWithFormatting_returnsNull_whenNoFormat() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    assertThat(invokeWrapTextWithFormatting("txt", info)).isNull();
  }

  @Test
  void wrapTextWithFormatting_wrapsBold() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setIsBold(true);
    Element el = invokeWrapTextWithFormatting("txt", info);
    assertThat(el.getTagName()).isEqualTo("b");
    assertThat(el.getTextContent()).isEqualTo("txt");
  }

  @Test
  void wrapTextWithFormatting_wrapsBoldAndItalic() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setIsBold(true);
    info.setIsItalic(true);
    Element el = invokeWrapTextWithFormatting("txt", info);
    assertThat(el.getTagName()).isEqualTo("i");
    assertThat(el.getFirstChild().getNodeName()).isEqualTo("b");
  }

  @Test
  void wrapTextWithFormatting_wrapsUnderlineStrikethroughAndSubSuper() throws Exception {
    CharacterFormatInfo underline = new CharacterFormatInfo();
    underline.setIsUnderline(true);
    assertThat(invokeWrapTextWithFormatting("t", underline).getTagName()).isEqualTo("u");

    CharacterFormatInfo strike = new CharacterFormatInfo();
    strike.setIsStrikethrough(true);
    assertThat(invokeWrapTextWithFormatting("t", strike).getTagName()).isEqualTo("s");

    CharacterFormatInfo sup = new CharacterFormatInfo();
    sup.setSubSuperScript((short) 1);
    assertThat(invokeWrapTextWithFormatting("t", sup).getTagName()).isEqualTo("sup");

    CharacterFormatInfo sub = new CharacterFormatInfo();
    sub.setSubSuperScript((short) -1);
    assertThat(invokeWrapTextWithFormatting("t", sub).getTagName()).isEqualTo("sub");
  }

  // ---- wrapTextWithFontStyle ----

  @Test
  void wrapTextWithFontStyle_wrapsTextWithSpan_andAppliesStyle() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setFontSize(12);
    Element span = invokeWrapTextWithFontStyle("txt", null, info);
    assertThat(span.getTagName()).isEqualTo("span");
    assertThat(span.getAttribute("style")).isEqualTo("font-size: 12pt;");
    assertThat(span.getTextContent()).isEqualTo("txt");
  }

  @Test
  void wrapTextWithFontStyle_wrapsFormattedElement() throws Exception {
    CharacterFormatInfo info = new CharacterFormatInfo();
    Element bold = doc.createElement("b");
    bold.setTextContent("txt");
    Element span = invokeWrapTextWithFontStyle("txt", bold, info);
    assertThat(span.getTagName()).isEqualTo("span");
    assertThat(span.hasAttribute("style")).isFalse();
    assertThat(span.getFirstChild().getNodeName()).isEqualTo("b");
  }

  // ---- wrapElement ----

  @Test
  void wrapElement_appendsInnerOrText() throws Exception {
    Element wrapper = doc.createElement("span");
    Element inner = doc.createElement("b");
    invokeWrapElement(inner, wrapper, "txt");
    assertThat(wrapper.getChildNodes().getLength()).isEqualTo(1);
    assertThat(wrapper.getFirstChild()).isSameAs(inner);

    Element wrapper2 = doc.createElement("span");
    invokeWrapElement(null, wrapper2, "txt");
    assertThat(wrapper2.getChildNodes().getLength()).isEqualTo(1);
    assertThat(wrapper2.getFirstChild().getTextContent()).isEqualTo("txt");
  }

  // ---- getFontSizeFromRun ----

  @Test
  void getFontSizeFromRun_halvesInteger_andReturnsZeroOnException() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.getFontSize()).thenReturn(24);
    assertThat(invokeGetFontSizeFromRun(run)).isEqualTo(12);

    CharacterRun throwing = mock(CharacterRun.class);
    when(throwing.getFontSize()).thenThrow(new RuntimeException("boom"));
    assertThat(invokeGetFontSizeFromRun(throwing)).isZero();
  }

  // ---- getStrikethrough ----

  @Test
  void getStrikethrough_readsBoolean_andReturnsFalseOnException() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.isStrikeThrough()).thenReturn(true);
    assertThat(invokeGetStrikethrough(run)).isTrue();

    CharacterRun throwing = mock(CharacterRun.class);
    when(throwing.isStrikeThrough()).thenThrow(new RuntimeException("boom"));
    assertThat(invokeGetStrikethrough(throwing)).isFalse();
  }

  // ---- getStrikethroughViaGetter (NoSuchMethod 路径) ----

  @Test
  void getStrikethroughViaGetter_returnsFalse_whenGetterMissing() throws Exception {
    // CharacterRun 无 getStrikeThrough 方法 -> NoSuchMethodException -> false
    CharacterRun run = mock(CharacterRun.class);
    assertThat(invokeGetStrikethroughViaGetter(run)).isFalse();
  }

  // ---- getSubSuperScript ----

  @Test
  void getSubSuperScript_returnsShort_andZeroOnException() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.getSubSuperScriptIndex()).thenReturn((short) 2);
    assertThat(invokeGetSubSuperScript(run)).isEqualTo((short) 2);

    CharacterRun throwing = mock(CharacterRun.class);
    when(throwing.getSubSuperScriptIndex()).thenThrow(new RuntimeException("boom"));
    assertThat(invokeGetSubSuperScript(throwing)).isEqualTo((short) 0);
  }

  // ---- getFontName ----

  @Test
  void getFontName_returnsName_andNullOnException() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.getFontName()).thenReturn("Arial");
    assertThat(invokeGetFontName(run)).isEqualTo("Arial");

    CharacterRun throwing = mock(CharacterRun.class);
    when(throwing.getFontName()).thenThrow(new RuntimeException("boom"));
    assertThat(invokeGetFontName(throwing)).isNull();
  }

  // ---- getFontColor ----

  @Test
  void getFontColor_returnsHexForKnownIndex_andNullForNegativeAndException() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.getColor()).thenReturn(1);
    assertThat(invokeGetFontColor(run)).isEqualTo("#0000FF");

    CharacterRun negative = mock(CharacterRun.class);
    when(negative.getColor()).thenReturn(-1);
    assertThat(invokeGetFontColor(negative)).isNull();

    CharacterRun throwing = mock(CharacterRun.class);
    when(throwing.getColor()).thenThrow(new RuntimeException("boom"));
    assertThat(invokeGetFontColor(throwing)).isNull();
  }

  // ---- extractCharacterFormatInfo ----

  @Test
  void extractCharacterFormatInfo_populatesAllFieldsFromRun() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.isBold()).thenReturn(true);
    when(run.isItalic()).thenReturn(true);
    when(run.getUnderlineCode()).thenReturn(1);
    when(run.isStrikeThrough()).thenReturn(true);
    when(run.getSubSuperScriptIndex()).thenReturn((short) 1);
    when(run.getFontName()).thenReturn("Arial");
    when(run.getFontSize()).thenReturn(24);
    when(run.getColor()).thenReturn(1);

    CharacterFormatInfo info = invokeExtractCharacterFormatInfo(run);
    assertThat(info.getIsBold()).isTrue();
    assertThat(info.getIsItalic()).isTrue();
    assertThat(info.getIsUnderline()).isTrue();
    assertThat(info.getIsStrikethrough()).isTrue();
    assertThat(info.getSubSuperScript()).isEqualTo((short) 1);
    assertThat(info.getFontName()).isEqualTo("Arial");
    assertThat(info.getFontSize()).isEqualTo(12); // 24 / 2
    assertThat(info.getFontColor()).isEqualTo("#0000FF");
  }

  @Test
  void extractCharacterFormatInfo_defaultsWhenRunReturnsDefaults() throws Exception {
    CharacterRun run = mock(CharacterRun.class); // 默认 false/0/null
    when(run.getUnderlineCode()).thenReturn(0);
    when(run.getColor()).thenReturn(0);

    CharacterFormatInfo info = invokeExtractCharacterFormatInfo(run);
    assertThat(info.getIsBold()).isFalse();
    assertThat(info.getIsUnderline()).isFalse(); // getUnderlineCode 0
    assertThat(info.getFontSize()).isZero();
    assertThat(info.getFontColor()).isEqualTo("#000000");
  }

  // ---- processCharacterRun ----

  @Test
  void processCharacterRun_returnsSpanWrappingFormattedContent() throws Exception {
    CharacterRun run = mock(CharacterRun.class);
    when(run.isBold()).thenReturn(true);
    when(run.getFontSize()).thenReturn(24);
    when(run.getColor()).thenReturn(1);
    when(run.getFontName()).thenReturn("Arial");
    when(run.getUnderlineCode()).thenReturn(0);
    when(run.getSubSuperScriptIndex()).thenReturn((short) 0);

    Element el = invokeProcessCharacterRun(run, "hello");
    assertThat(el.getTagName()).isEqualTo("span");
    assertThat(el.getAttribute("style")).contains("font-size: 12pt", "color: #0000FF", "font-family: 'Arial'");
    // 粗体 -> 内层 <b>
    assertThat(el.getFirstChild().getNodeName()).isEqualTo("b");
  }

  // ---- 反射调用辅助 ----

  private String invokeColorHex(int idx) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("convertColorIndexToHex", int.class);
    m.setAccessible(true);
    return (String) m.invoke(converter, idx);
  }

  private Integer invokeGetIntegerValue(Object target, String methodName) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getIntegerValue", Object.class, String.class);
    m.setAccessible(true);
    return (Integer) m.invoke(null, target, methodName);
  }

  private String invokeBuildFontStyle(CharacterFormatInfo info) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("buildFontStyle", CharacterFormatInfo.class);
    m.setAccessible(true);
    return (String) m.invoke(converter, info);
  }

  private boolean invokeIsParagraphInList(Integer listID, Integer listLevel) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("isParagraphInList", Integer.class, Integer.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, listID, listLevel);
  }

  private boolean invokeDetermineListOrder(HWPFList list, Integer listID, Integer listLevel) throws Exception {
    Method m = SafeWordToHtmlConverter.class
      .getDeclaredMethod("determineListOrder", HWPFList.class, Integer.class, Integer.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, list, listID, listLevel);
  }

  private int invokeGetCurrentListLevel(Integer listLevel) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getCurrentListLevel", Integer.class);
    m.setAccessible(true);
    return (int) m.invoke(converter, listLevel);
  }

  private boolean invokeShouldCreateNewList(ListContext ctx, int currentLevel) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("shouldCreateNewList", ListContext.class, int.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, ctx, currentLevel);
  }

  private boolean invokeIsListTablesNullError(RuntimeException e) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("isListTablesNullError", RuntimeException.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, e);
  }

  private boolean invokeIsEmptyParagraph(Element el) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("isEmptyParagraph", Element.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, el);
  }

  private boolean invokeHasListItemContent(Element el) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("hasListItemContent", Element.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, el);
  }

  private Element invokeWrapTextWithFormatting(String text, CharacterFormatInfo info) throws Exception {
    Method m = SafeWordToHtmlConverter.class
      .getDeclaredMethod("wrapTextWithFormatting", Document.class, String.class, CharacterFormatInfo.class);
    m.setAccessible(true);
    return (Element) m.invoke(converter, doc, text, info);
  }

  private Element invokeWrapTextWithFontStyle(String text, Element formatted, CharacterFormatInfo info) throws Exception {
    Method m = SafeWordToHtmlConverter.class
      .getDeclaredMethod("wrapTextWithFontStyle", Document.class, String.class, Element.class, CharacterFormatInfo.class);
    m.setAccessible(true);
    return (Element) m.invoke(converter, doc, text, formatted, info);
  }

  private void invokeWrapElement(Element inner, Element wrapper, String text) throws Exception {
    Method m = SafeWordToHtmlConverter.class
      .getDeclaredMethod("wrapElement", Document.class, Element.class, Element.class, String.class);
    m.setAccessible(true);
    m.invoke(converter, doc, inner, wrapper, text);
  }

  private Integer invokeGetFontSizeFromRun(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getFontSizeFromRun", CharacterRun.class);
    m.setAccessible(true);
    return (Integer) m.invoke(converter, run);
  }

  private boolean invokeGetStrikethrough(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getStrikethrough", CharacterRun.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, run);
  }

  private boolean invokeGetStrikethroughViaGetter(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getStrikethroughViaGetter", CharacterRun.class);
    m.setAccessible(true);
    return (boolean) m.invoke(converter, run);
  }

  private Short invokeGetSubSuperScript(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getSubSuperScript", CharacterRun.class);
    m.setAccessible(true);
    return (Short) m.invoke(converter, run);
  }

  private String invokeGetFontName(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getFontName", CharacterRun.class);
    m.setAccessible(true);
    return (String) m.invoke(converter, run);
  }

  private String invokeGetFontColor(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("getFontColor", CharacterRun.class);
    m.setAccessible(true);
    return (String) m.invoke(converter, run);
  }

  private CharacterFormatInfo invokeExtractCharacterFormatInfo(CharacterRun run) throws Exception {
    Method m = SafeWordToHtmlConverter.class.getDeclaredMethod("extractCharacterFormatInfo", CharacterRun.class);
    m.setAccessible(true);
    return (CharacterFormatInfo) m.invoke(converter, run);
  }

  private Element invokeProcessCharacterRun(CharacterRun run, String text) throws Exception {
    Method m = SafeWordToHtmlConverter.class
      .getDeclaredMethod("processCharacterRun", CharacterRun.class, String.class);
    m.setAccessible(true);
    return (Element) m.invoke(converter, run, text);
  }

  // ---- fixtures ----

  private static ListContext newContext() {
    ListContext ctx = new ListContext();
    ctx.setOpenLists(new ArrayList<>());
    return ctx;
  }

  private static ListContext contextWithLevel(int level) {
    ListContext ctx = newContext();
    ctx.getOpenLists().add(new ListInfo(1, level, true, null));
    return ctx;
  }

  /** getIntegerValue 测试目标对象。 */
  public static class IntTarget {
    public int getI() {
      return 42;
    }

    public short getS() {
      return (short) 7;
    }

    public String getStr() {
      return "x";
    }

    public int getThrowing() {
      throw new RuntimeException("boom");
    }
  }
}
