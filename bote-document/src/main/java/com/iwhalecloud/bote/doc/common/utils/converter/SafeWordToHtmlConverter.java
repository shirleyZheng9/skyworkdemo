package com.iwhalecloud.bote.doc.common.utils.converter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * 自定义的 WordToHtmlConverter，安全处理 ListTables 为 null 的情况
 * <p>
 * 重写 processParagraphes 方法，在访问 ListTables 之前检查是否为 null，
 * 从而避免 NullPointerException。
 * </p>
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class SafeWordToHtmlConverter extends WordToHtmlConverter {
  private static final Logger logger = LoggerFactory.getLogger(SafeWordToHtmlConverter.class);

  // 样式常量
  private static final int POI_HALF_POINT_DIVISOR = 2; // POI 字体大小是半磅值，需要除以 2
  private static final int MAX_FONT_SIZE = 200; // 最大字体大小（pt）
  private static final int MIN_HEADING_FONT_SIZE = 12; // 标题最小字体大小（pt）
  private static final int HEADING_BASE_FONT_SIZE = 24; // 标题基准字体大小（pt）
  private static final int HEADING_FONT_SIZE_DECREMENT = 2; // 标题字体大小递减量（pt）
  private static final int TWIPS_PER_INCH = 1440; // Word 缩进单位 twips 每英寸
  private static final int PIXELS_PER_INCH = 96; // 像素每英寸

  // Word 标准颜色索引映射
  private static final Map<Integer, String> COLOR_INDEX_MAP = createColorIndexMap();

  private static Map<Integer, String> createColorIndexMap() {
    Map<Integer, String> map = new HashMap<>();
    map.put(0, "#000000"); // 自动/黑色
    map.put(1, "#0000FF"); // 蓝色
    map.put(2, "#00FFFF"); // 青色
    map.put(3, "#00FF00"); // 绿色
    map.put(4, "#FF00FF"); // 洋红色
    map.put(5, "#FF0000"); // 红色
    map.put(6, "#FFFF00"); // 黄色
    map.put(7, "#FFFFFF"); // 白色
    map.put(8, "#000080"); // 深蓝色
    map.put(9, "#008080"); // 深青色
    map.put(10, "#008000"); // 深绿色
    map.put(11, "#800080"); // 深洋红色
    map.put(12, "#800000"); // 深红色
    map.put(13, "#808000"); // 深黄色
    map.put(14, "#808080"); // 深灰色
    map.put(15, "#C0C0C0"); // 浅灰色
    return map;
  }

  public SafeWordToHtmlConverter(org.w3c.dom.Document domDocument) {
    super(domDocument);
  }

  /**
   * 重写 processParagraphes 方法，安全处理 ListTables 为 null 的情况
   * 方法签名必须与父类 AbstractWordConverter 中的方法一致
   */
  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  protected void processParagraphes(HWPFDocumentCore document, Element parentElement, Range range, int level) {
    try {
      // 先尝试调用父类方法，获取基本的 HTML 结构
      super.processParagraphes(document, parentElement, range, level);

      // 父类方法成功后，增强段落样式（对齐方式、字体大小等）
      enhanceParagraphStyles(parentElement, range);
    }
    catch (RuntimeException e) {
      // 捕获运行时异常（包括可能的NullPointerException），检查是否是ListTables相关的问题
      String errorMessage = e.getMessage();
      if (errorMessage != null && (errorMessage.contains("listTables") || errorMessage.contains("ListTables"))) {
        logger.warn("检测到 ListTables 为 null，使用备用处理逻辑: {}", errorMessage);
        // 使用安全的方式处理段落，保留列表格式和样式
        processParagraphesSafely(range, parentElement);
      }
      else {
        // 其他类型的运行时异常重新抛出
        throw e;
      }
    }
    catch (Exception e) {
      // 如果父类方法失败，使用备用处理逻辑
      logger.warn("父类方法处理段落失败，使用备用处理逻辑: {}", e.getMessage());
      processParagraphesSafely(range, parentElement);
    }
  }

  /**
   * 增强段落样式（对齐方式、字体大小等）
   * 在父类方法生成的 HTML 基础上，添加样式信息
   */
  private void enhanceParagraphStyles(Element parentElement, Range range) {
    int numParagraphs = range.numParagraphs();
    if (numParagraphs == 0) {
      return;
    }

    // 获取所有段落元素（包括嵌套在 div、li 等元素中的段落）
    List<Element> allParagraphs = new ArrayList<>();
    collectParagraphElements(parentElement, allParagraphs);

    if (logger.isDebugEnabled()) {
      logger.debug("找到 {} 个段落元素，源文档有 {} 个段落", allParagraphs.size(), numParagraphs);
    }

    // 遍历所有段落，应用样式
    int paragraphIndex = 0;
    for (int i = 0; i < numParagraphs && paragraphIndex < allParagraphs.size(); i++) {
      try {
        Paragraph paragraph = range.getParagraph(i);

        // 跳过空段落
        if (paragraph.numCharacterRuns() == 0) {
          continue;
        }

        // 获取对应的 HTML 段落元素
        if (paragraphIndex >= allParagraphs.size()) {
          break;
        }

        Element paragraphElement = allParagraphs.get(paragraphIndex);

        // 构建并应用样式
        String newStyle = buildParagraphStyle(paragraphElement, paragraph);
        if (newStyle != null && !newStyle.isEmpty()) {
          paragraphElement.setAttribute("style", newStyle);
        }

        paragraphIndex++;
      }
      catch (Exception e) {
        logger.warn("增强段落 {} 样式时出错: {}", i, e.getMessage(), e);
      }
    }
  }

  /**
   * 构建段落样式字符串（合并现有样式、对齐方式、段落样式）
   */
  private String buildParagraphStyle(Element paragraphElement, Paragraph paragraph) {
    StringBuilder styleBuilder = new StringBuilder();

    // 保留现有样式
    String currentStyle = paragraphElement.getAttribute("style");
    if (currentStyle != null && !currentStyle.isEmpty()) {
      String trimmed = currentStyle.trim();
      styleBuilder.append(trimmed);
      if (!trimmed.endsWith(";")) {
        styleBuilder.append("; ");
      }
      else {
        styleBuilder.append(" ");
      }
    }

    // 获取并添加对齐方式
    String alignment = getParagraphAlignment(paragraph);
    if (alignment != null && !alignment.isEmpty() && styleBuilder.indexOf("text-align") == -1) {
      styleBuilder.append("text-align: ").append(alignment).append("; ");
    }

    // 获取并添加段落样式（字体大小、粗体等）
    String paragraphStyle = getParagraphStyle(paragraph);
    if (paragraphStyle != null && !paragraphStyle.isEmpty()) {
      styleBuilder.append(paragraphStyle);
    }

    return styleBuilder.toString().trim();
  }

  /**
   * 递归收集所有段落元素
   */
  private void collectParagraphElements(Element element, List<Element> paragraphs) {
    org.w3c.dom.NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node node = children.item(i);
      if (node instanceof Element) {
        Element childElement = (Element) node;
        if ("p".equals(childElement.getTagName())) {
          paragraphs.add(childElement);
        }
        else {
          // 递归查找子元素中的段落
          collectParagraphElements(childElement, paragraphs);
        }
      }
    }
  }

  /**
   * 安全地处理段落，保留列表格式即使 ListTables 为 null
   */
  private void processParagraphesSafely(Range range, Element parentElement) {
    int numParagraphs = range.numParagraphs();
    ListContext listContext = initializeListContext();

    for (int i = 0; i < numParagraphs; i++) {
      Paragraph paragraph = range.getParagraph(i);
      processParagraphSafely(paragraph, i, parentElement, listContext);
    }

    closeOpenLists(listContext);
  }

  /**
   * 初始化列表上下文
   */
  private ListContext initializeListContext() {
    ListContext listContext = new ListContext();
    listContext.setOpenLists(new ArrayList<>());
    return listContext;
  }

  /**
   * 安全地处理单个段落
   */
  private void processParagraphSafely(Paragraph paragraph, int paragraphIndex, Element parentElement,
    ListContext listContext) {
    try {
      HWPFList hwpfList = null;
      Integer listLevel = null;
      Integer listID = null;

      try {
        hwpfList = paragraph.getList();
        if (hwpfList != null) {
          listLevel = getListLevel(paragraph);
          listID = getListID(paragraph);
        }
      }
      catch (RuntimeException e) {
        if (isListTablesNullError(e)) {
          logger.debug("段落 {} 的 ListTables 为 null，尝试通过反射获取列表信息", paragraphIndex);
          listLevel = getListLevelSafely(paragraph);
          listID = getListIDSafely(paragraph);
        }
        else {
          throw e;
        }
      }

      Element paragraphElement = processSingleParagraph(paragraph, hwpfList, listLevel, listID, listContext);
      appendParagraphIfNotNull(parentElement, paragraphElement);
    }
    catch (Exception e) {
      handleParagraphError(paragraph, paragraphIndex, parentElement, listContext, e);
    }
  }

  /**
   * 检查是否是ListTables为null导致的错误
   */
  private boolean isListTablesNullError(RuntimeException e) {
    String errorMessage = e.getMessage();
    return errorMessage != null && (errorMessage.contains("listTables") || errorMessage.contains("ListTables"));
  }

  /**
   * 如果段落元素不为null，添加到父元素
   */
  private void appendParagraphIfNotNull(Element parentElement, Element paragraphElement) {
    if (paragraphElement != null) {
      parentElement.appendChild(paragraphElement);
    }
  }

  /**
   * 处理段落处理错误
   */
  private void handleParagraphError(Paragraph paragraph, int paragraphIndex, Element parentElement,
    ListContext listContext, Exception e) {
    logger.warn("处理段落 {} 时出错: {}", paragraphIndex, e.getMessage());
    try {
      Element paragraphElement = processSingleParagraph(paragraph, null, null, null, listContext);
      appendParagraphIfNotNull(parentElement, paragraphElement);
    }
    catch (Exception e2) {
      logger.warn("备用处理段落 {} 也失败，跳过该段落: {}", paragraphIndex, e2.getMessage());
    }
  }

  /**
   * 使用反射获取整数类型的返回值（Integer 或 Short）
   *
   * @param target 目标对象
   * @param methodName 方法名
   * @return 整数值，如果获取失败返回 null
   */
  private static Integer getIntegerValue(Object target, String methodName) {
    try {
      Method method = target.getClass().getMethod(methodName);
      Object result = method.invoke(target);
      if (result instanceof Integer) {
        return (Integer) result;
      }
      else if (result instanceof Short) {
        return (int) (Short) result;
      }
    }
    catch (ReflectiveOperationException | SecurityException e) {
      // 忽略反射异常（NoSuchMethodException, InvocationTargetException, IllegalAccessException等）
      logger.debug("反射调用方法失败: {}, error={}", methodName, e.getMessage());
    }
    return null;
  }

  /**
   * 使用反射安全地获取段落列表级别（即使 ListTables 为 null）
   */
  private Integer getListLevelSafely(Paragraph paragraph) {
    Integer level = getIntegerValue(paragraph, "getIlvl");
    // getIlvl 返回的是缩进级别，-1 表示不在列表中
    if (level != null && level >= 0) {
      return level;
    }
    return null;
  }

  /**
   * 使用反射安全地获取段落列表 ID（即使 ListTables 为 null）
   */
  private Integer getListIDSafely(Paragraph paragraph) {
    Integer lsid = getIntegerValue(paragraph, "getLsid");
    // lsid > 0 表示在列表中
    if (lsid != null && lsid > 0) {
      return lsid;
    }
    return null;
  }

  /**
   * 正常获取列表级别
   */
  private Integer getListLevel(Paragraph paragraph) {
    return getIntegerValue(paragraph, "getIlvl");
  }

  /**
   * 正常获取列表 ID
   */
  private Integer getListID(Paragraph paragraph) {
    return getIntegerValue(paragraph, "getLsid");
  }


  /**
   * 关闭所有未关闭的列表
   */
  private void closeOpenLists(ListContext listContext) {
    while (!listContext.getOpenLists().isEmpty()) {
      listContext.getOpenLists().remove(listContext.getOpenLists().size() - 1);
      // 列表元素已经添加到父元素中，只需要清理栈
    }
  }

  /**
   * 处理单个段落，保留列表格式
   *
   * @param paragraph 段落对象
   * @param list 列表对象（可能为 null）
   * @param listLevel 列表级别（可能为 null）
   * @param listID 列表 ID（可能为 null）
   * @param listContext 列表上下文
   * @return 处理后的 HTML 元素
   */
  private Element processSingleParagraph(Paragraph paragraph, HWPFList list, Integer listLevel, Integer listID,
    ListContext listContext) {
    boolean isInList = isParagraphInList(listID, listLevel);
    boolean isOrdered = determineListOrder(list, listID, listLevel);

    if (isInList) {
      return processListItem(paragraph, listLevel, listID, listContext, isOrdered);
    }

    return processRegularParagraph(paragraph, listContext);
  }

  /**
   * 判断段落是否在列表中
   */
  private boolean isParagraphInList(Integer listID, Integer listLevel) {
    return (listID != null && listID > 0) || (listLevel != null && listLevel >= 0);
  }

  /**
   * 确定列表是否有序
   */
  private boolean determineListOrder(HWPFList list, Integer listID, Integer listLevel) {
    if (list != null) {
      try {
        return true; // 默认假设有序，可根据需要调整
      }
      catch (Exception e) {
        return false;
      }
    }
    else if (listID != null || listLevel != null) {
      return false; // 默认使用无序列表（圆点）
    }
    return false;
  }

  /**
   * 处理普通段落（非列表项）
   */
  private Element processRegularParagraph(Paragraph paragraph, ListContext listContext) {
    closeOpenLists(listContext);

    org.w3c.dom.Document document = getDocument();
    Element paragraphElement = document.createElement("p");

    applyParagraphAlignment(paragraph, paragraphElement);
    applyParagraphStyle(paragraph, paragraphElement);
    processParagraphCharacterRuns(paragraph, paragraphElement);

    if (isEmptyParagraph(paragraphElement)) {
      return null;
    }

    return paragraphElement;
  }

  /**
   * 应用段落对齐方式
   */
  private void applyParagraphAlignment(Paragraph paragraph, Element paragraphElement) {
    String alignment = getParagraphAlignment(paragraph);
    if (alignment != null && !alignment.isEmpty()) {
      paragraphElement.setAttribute("style", "text-align: " + alignment + ";");
    }
  }

  /**
   * 应用段落样式
   */
  private void applyParagraphStyle(Paragraph paragraph, Element paragraphElement) {
    String paragraphStyle = getParagraphStyle(paragraph);
    if (paragraphStyle == null || paragraphStyle.isEmpty()) {
      return;
    }

    String currentStyle = paragraphElement.getAttribute("style");
    if (currentStyle != null && !currentStyle.isEmpty()) {
      paragraphElement.setAttribute("style", currentStyle + " " + paragraphStyle);
    }
    else {
      paragraphElement.setAttribute("style", paragraphStyle);
    }
  }

  /**
   * 处理段落的字符运行
   */
  private void processParagraphCharacterRuns(Paragraph paragraph, Element paragraphElement) {
    Range characterRange = paragraph;
    int numCharacterRuns = characterRange.numCharacterRuns();

    for (int i = 0; i < numCharacterRuns; i++) {
      CharacterRun characterRun = characterRange.getCharacterRun(i);
      String text = characterRun.text();

      if (StringUtils.isNotBlank(text)) {
        Element textElement = processCharacterRun(characterRun, text);
        paragraphElement.appendChild(textElement);
      }
    }
  }

  /**
   * 检查段落是否为空
   */
  private boolean isEmptyParagraph(Element paragraphElement) {
    return paragraphElement.getChildNodes().getLength() == 0
      && StringUtils.isBlank(paragraphElement.getTextContent());
  }

  /**
   * 获取段落对齐方式
   */
  private String getParagraphAlignment(Paragraph paragraph) {
    try {
      // 尝试使用反射获取对齐方式
      Method getJustificationMethod = paragraph.getClass().getMethod("getJustification");
      Object result = getJustificationMethod.invoke(paragraph);

      int justification = 0;
      if (result instanceof Integer) {
        justification = (Integer) result;
      }
      else if (result instanceof Short) {
        justification = (Short) result;
      }

      // Word 对齐值：0=左对齐, 1=居中, 2=右对齐, 3=两端对齐, 4=分散对齐
      switch (justification) {
        case 0:
          return "left";
        case 1:
          return "center";
        case 2:
          return "right";
        case 3:
          return "justify";
        case 4:
          return "justify"; // 分散对齐也用 justify
        default:
          return "left"; // 默认左对齐
      }
    }
    catch (Exception e) {
      logger.debug("无法获取段落对齐方式: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 获取段落样式（如标题层级、字体大小等）
   */
  private String getParagraphStyle(Paragraph paragraph) {
    StringBuilder styleBuilder = new StringBuilder();

    // 1. 优先通过段落的首个字符运行获取字体大小（最可靠）
    extractFontSizeFromFirstRun(paragraph, styleBuilder);

    // 2. 尝试获取段落样式索引（辅助判断）
    extractStyleFromStyleIndex(paragraph, styleBuilder);

    // 3. 获取段落缩进
    extractIndentFromParagraph(paragraph, styleBuilder);

    String style = styleBuilder.toString().trim();
    return style.isEmpty() ? null : style;
  }

  /**
   * 从第一个字符运行提取字体大小
   */
  private void extractFontSizeFromFirstRun(Paragraph paragraph, StringBuilder styleBuilder) {
    try {
      Range characterRange = paragraph;
      if (characterRange.numCharacterRuns() == 0) {
        return;
      }

      CharacterRun firstRun = characterRange.getCharacterRun(0);

      // 获取字体大小
      Integer fontSize = getFontSizeFromRun(firstRun);
      if (fontSize != null && fontSize > 0 && fontSize < MAX_FONT_SIZE) {
        styleBuilder.append("font-size: ").append(fontSize).append("pt; ");
      }

      // 如果第一个字符是粗体且已设置字体大小，添加粗体样式
      if (firstRun.isBold() && styleBuilder.indexOf("font-size") != -1
        && styleBuilder.indexOf("font-weight") == -1) {
        styleBuilder.append("font-weight: bold; ");
      }
    }
    catch (Exception e) {
      logger.debug("无法通过字符运行获取段落样式: {}", e.getMessage());
    }
  }

  /**
   * 从段落样式索引提取样式
   */
  private void extractStyleFromStyleIndex(Paragraph paragraph, StringBuilder styleBuilder) {
    try {
      Integer styleIndex = getIntegerValue(paragraph, "getStyleIndex");
      if (styleIndex == null || styleIndex < 1 || styleIndex > 9) {
        return;
      }

      // 如果已经有字体大小但样式索引表明是标题，添加粗体
      if (styleBuilder.indexOf("font-size") != -1 && styleBuilder.indexOf("font-weight") == -1) {
        styleBuilder.append("font-weight: bold; ");
      }
      // 如果没有字体大小但样式索引表明是标题，设置默认字体大小
      else if (styleBuilder.indexOf("font-size") == -1) {
        int fontSize = HEADING_BASE_FONT_SIZE - (styleIndex - 1) * HEADING_FONT_SIZE_DECREMENT;
        if (fontSize < MIN_HEADING_FONT_SIZE) {
          fontSize = MIN_HEADING_FONT_SIZE;
        }
        styleBuilder.append("font-size: ").append(fontSize).append("pt; ");
        styleBuilder.append("font-weight: bold; ");
      }
    }
    catch (Exception e) {
      logger.debug("无法获取段落样式索引: {}", e.getMessage());
    }
  }

  /**
   * 从段落提取缩进样式
   */
  private void extractIndentFromParagraph(Paragraph paragraph, StringBuilder styleBuilder) {
    try {
      Integer indent = getIntegerValue(paragraph, "getIndentFromLeft");
      if (indent != null && indent > 0) {
        // 将缩进值转换为像素（Word 缩进单位是 twips，1 inch = 1440 twips）
        // 假设 1 inch = 96 pixels，所以 indent / 1440 * 96
        int indentPx = (int) (indent / (double) TWIPS_PER_INCH * PIXELS_PER_INCH);
        if (indentPx > 0) {
          styleBuilder.append("padding-left: ").append(indentPx).append("px; ");
        }
      }
    }
    catch (Exception e) {
      logger.debug("无法获取段落缩进: {}", e.getMessage());
    }
  }

  /**
   * 从字符运行获取字体大小
   */
  private Integer getFontSizeFromRun(CharacterRun characterRun) {
    try {
      Method getFontSizeMethod = characterRun.getClass().getMethod("getFontSize");
      Object result = getFontSizeMethod.invoke(characterRun);
      if (result instanceof Integer) {
        return (Integer) result / POI_HALF_POINT_DIVISOR;
      }
      else if (result instanceof Short) {
        return ((Short) result) / POI_HALF_POINT_DIVISOR;
      }
    }
    catch (Exception e) {
      logger.debug("无法获取字体大小: {}", e.getMessage());
    }
    return 0;
  }

  /**
   * 处理列表项，保留列表格式
   */
  private Element processListItem(Paragraph paragraph, Integer listLevel, Integer listID, ListContext listContext,
    boolean isOrdered) {
    org.w3c.dom.Document document = getDocument();
    int currentLevel = getCurrentListLevel(listLevel);

    closeExcessLists(listContext, currentLevel);
    Element currentListElement = findOrCreateListElement(document, listContext, currentLevel, isOrdered, listID);

    Element listItemElement = createListItemWithContent(paragraph, document);
    if (hasListItemContent(listItemElement)) {
      if (currentListElement != null) {
        currentListElement.appendChild(listItemElement);
      }
    }

    return null;
  }

  /**
   * 获取当前列表级别
   */
  private int getCurrentListLevel(Integer listLevel) {
    return (listLevel != null && listLevel >= 0) ? listLevel : 0;
  }

  /**
   * 关闭超出当前级别的列表
   */
  private void closeExcessLists(ListContext listContext, int currentLevel) {
    while (!listContext.getOpenLists().isEmpty()) {
      ListInfo lastInfo = listContext.getOpenLists().get(listContext.getOpenLists().size() - 1);
      if (lastInfo.getLevel() >= currentLevel) {
        listContext.getOpenLists().remove(listContext.getOpenLists().size() - 1);
      }
      else {
        break;
      }
    }
  }

  /**
   * 找到或创建列表元素
   */
  private Element findOrCreateListElement(org.w3c.dom.Document document, ListContext listContext, int currentLevel,
    boolean isOrdered, Integer listID) {
    if (shouldCreateNewList(listContext, currentLevel)) {
      return createNewListElement(document, listContext, currentLevel, isOrdered, listID);
    }

    return getExistingListElement(listContext);
  }

  /**
   * 判断是否需要创建新列表
   */
  private boolean shouldCreateNewList(ListContext listContext, int currentLevel) {
    return listContext.getOpenLists().isEmpty()
      || listContext.getOpenLists().get(listContext.getOpenLists().size() - 1).getLevel() < currentLevel;
  }

  /**
   * 创建新的列表元素
   */
  private Element createNewListElement(org.w3c.dom.Document document, ListContext listContext, int currentLevel,
    boolean isOrdered, Integer listID) {
    Element parentElement = getParentElementForNewList(document, listContext);
    Element currentListElement = document.createElement(isOrdered ? "ol" : "ul");

    if (currentLevel > 0) {
      currentListElement.setAttribute("style", "margin-left: " + (currentLevel * 20) + "px;");
    }

    if (parentElement != null) {
      parentElement.appendChild(currentListElement);
    }

    listContext.getOpenLists().add(new ListInfo(listID, currentLevel, isOrdered, currentListElement));
    return currentListElement;
  }

  /**
   * 获取新列表的父元素
   */
  private Element getParentElementForNewList(org.w3c.dom.Document document, ListContext listContext) {
    if (listContext.getOpenLists().isEmpty()) {
      Element parentElement = (Element) document.getDocumentElement().getElementsByTagName("body").item(0);
      return parentElement != null ? parentElement : document.getDocumentElement();
    }

    ListInfo parentInfo = listContext.getOpenLists().get(listContext.getOpenLists().size() - 1);
    Element parentListElement = parentInfo.getElement();
    Element parentListItemElement = document.createElement("li");
    parentListElement.appendChild(parentListItemElement);
    return parentListItemElement;
  }

  /**
   * 获取现有的列表元素
   */
  private Element getExistingListElement(ListContext listContext) {
    return listContext.getOpenLists().get(listContext.getOpenLists().size() - 1).getElement();
  }

  /**
   * 创建包含内容的列表项
   */
  private Element createListItemWithContent(Paragraph paragraph, org.w3c.dom.Document document) {
    Element listItemElement = document.createElement("li");
    Range characterRange = paragraph;
    int numCharacterRuns = characterRange.numCharacterRuns();

    for (int i = 0; i < numCharacterRuns; i++) {
      CharacterRun characterRun = characterRange.getCharacterRun(i);
      String text = characterRun.text();

      if (StringUtils.isNotBlank(text)) {
        Element textElement = processCharacterRun(characterRun, text);
        listItemElement.appendChild(textElement);
      }
    }

    return listItemElement;
  }

  /**
   * 检查列表项是否有内容
   */
  private boolean hasListItemContent(Element listItemElement) {
    return listItemElement.getChildNodes().getLength() > 0
      || StringUtils.isNotBlank(listItemElement.getTextContent());
  }

  /**
   * 处理字符运行（文本格式化），支持完整的样式信息
   */
  private Element processCharacterRun(CharacterRun characterRun, String text) {
    org.w3c.dom.Document document = getDocument();

    CharacterFormatInfo formatInfo = extractCharacterFormatInfo(characterRun);
    Element formattedElement = wrapTextWithFormatting(document, text, formatInfo);
    return wrapTextWithFontStyle(document, text, formattedElement, formatInfo);
  }

  /**
   * 提取字符格式信息
   */
  private CharacterFormatInfo extractCharacterFormatInfo(CharacterRun characterRun) {
    CharacterFormatInfo info = new CharacterFormatInfo();
    info.setIsBold(characterRun.isBold());
    info.setIsItalic(characterRun.isItalic());
    info.setIsUnderline(characterRun.getUnderlineCode() != 0);
    info.setIsStrikethrough(getStrikethrough(characterRun));
    info.setSubSuperScript(getSubSuperScript(characterRun));
    info.setFontName(getFontName(characterRun));
    info.setFontSize(getFontSizeFromRun(characterRun));
    info.setFontColor(getFontColor(characterRun));
    return info;
  }

  /**
   * 获取删除线信息
   */
  private boolean getStrikethrough(CharacterRun characterRun) {
    try {
      Method method = characterRun.getClass().getMethod("isStrikeThrough");
      return (Boolean) method.invoke(characterRun);
    }
    catch (NoSuchMethodException e) {
      return getStrikethroughViaGetter(characterRun);
    }
    catch (Exception e) {
      logger.debug("获取删除线信息时出错: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 通过getter获取删除线信息
   */
  private boolean getStrikethroughViaGetter(CharacterRun characterRun) {
    try {
      Method method = characterRun.getClass().getMethod("getStrikeThrough");
      Object result = method.invoke(characterRun);
      if (result instanceof Boolean) {
        return (Boolean) result;
      }
      else if (result instanceof Short) {
        return ((Short) result) > 0;
      }
      else if (result instanceof Integer) {
        return ((Integer) result) > 0;
      }
    }
    catch (NoSuchMethodException e) {
      logger.debug("无法获取删除线信息，方法不存在");
    }
    catch (Exception e) {
      logger.debug("获取删除线信息时出错: {}", e.getMessage());
    }
    return false;
  }

  /**
   * 获取上标/下标信息
   */
  private Short getSubSuperScript(CharacterRun characterRun) {
    try {
      Method method = characterRun.getClass().getMethod("getSubSuperScriptIndex");
      Object result = method.invoke(characterRun);
      if (result instanceof Short) {
        return (Short) result;
      }
      else if (result instanceof Integer) {
        return ((Integer) result).shortValue();
      }
    }
    catch (Exception e) {
      logger.debug("无法获取上标/下标信息: {}", e.getMessage());
    }
    return 0;
  }

  /**
   * 获取字体名称
   */
  private String getFontName(CharacterRun characterRun) {
    try {
      Method getFontNameMethod = characterRun.getClass().getMethod("getFontName");
      return (String) getFontNameMethod.invoke(characterRun);
    }
    catch (Exception e) {
      logger.debug("无法获取字体名称: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 获取字体颜色
   */
  private String getFontColor(CharacterRun characterRun) {
    try {
      Method getColorMethod = characterRun.getClass().getMethod("getColor");
      Object result = getColorMethod.invoke(characterRun);
      int colorIndex = -1;
      if (result instanceof Integer) {
        colorIndex = (Integer) result;
      }
      else if (result instanceof Short) {
        colorIndex = (Short) result;
      }

      if (colorIndex >= 0) {
        return convertColorIndexToHex(colorIndex);
      }
    }
    catch (Exception e) {
      logger.debug("无法获取字体颜色: {}", e.getMessage());
    }
    return null;
  }

  /**
   * 使用格式标签包装文本
   */
  private Element wrapTextWithFormatting(org.w3c.dom.Document document, String text, CharacterFormatInfo formatInfo) {
    org.w3c.dom.Text textNode = document.createTextNode(text);
    Element currentElement = null;

    if (Boolean.TRUE.equals(formatInfo.getIsBold())) {
      currentElement = document.createElement("b");
      currentElement.appendChild(textNode);
      textNode = null;
    }

    if (Boolean.TRUE.equals(formatInfo.getIsItalic())) {
      currentElement = wrapElement(document, currentElement, document.createElement("i"), text);
    }

    if (Boolean.TRUE.equals(formatInfo.getIsUnderline())) {
      currentElement = wrapElement(document, currentElement, document.createElement("u"), text);
    }

    if (Boolean.TRUE.equals(formatInfo.getIsStrikethrough())) {
      currentElement = wrapElement(document, currentElement, document.createElement("s"), text);
    }

    Short subSuperScript = formatInfo.getSubSuperScript();
    if (subSuperScript != null && subSuperScript != 0) {
      Element subSuperElement = document.createElement(subSuperScript > 0 ? "sup" : "sub");
      currentElement = wrapElement(document, currentElement, subSuperElement, text);
    }

    return currentElement;
  }

  /**
   * 包装元素
   */
  private Element wrapElement(org.w3c.dom.Document document, Element innerElement, Element wrapperElement,
    String text) {
    if (innerElement != null) {
      wrapperElement.appendChild(innerElement);
    }
    else {
      wrapperElement.appendChild(document.createTextNode(text));
    }
    return wrapperElement;
  }

  /**
   * 使用字体样式包装元素
   */
  private Element wrapTextWithFontStyle(org.w3c.dom.Document document, String text, Element formattedElement,
    CharacterFormatInfo formatInfo) {
    Element span = document.createElement("span");
    String style = buildFontStyle(formatInfo);

    if (style != null && !style.isEmpty()) {
      span.setAttribute("style", style);
    }

    if (formattedElement != null) {
      span.appendChild(formattedElement);
    }
    else {
      span.appendChild(document.createTextNode(text));
    }

    return span;
  }

  /**
   * 构建字体样式字符串
   */
  private String buildFontStyle(CharacterFormatInfo formatInfo) {
    StringBuilder styleBuilder = new StringBuilder();

    Integer fontSize = formatInfo.getFontSize();
    if (fontSize != null && fontSize > 0) {
      styleBuilder.append("font-size: ").append(fontSize).append("pt; ");
    }

    if (formatInfo.getFontColor() != null && !formatInfo.getFontColor().isEmpty()) {
      styleBuilder.append("color: ").append(formatInfo.getFontColor()).append("; ");
    }

    if (formatInfo.getFontName() != null && !formatInfo.getFontName().isEmpty()) {
      styleBuilder.append("font-family: '").append(formatInfo.getFontName()).append("'; ");
    }

    return styleBuilder.length() > 0 ? styleBuilder.toString().trim() : null;
  }


  /**
   * 将颜色索引转换为十六进制颜色值
   * 这是一个简化的转换，实际的颜色映射可能更复杂
   */
  private String convertColorIndexToHex(int colorIndex) {
    return COLOR_INDEX_MAP.getOrDefault(colorIndex, "#000000");
  }

}
