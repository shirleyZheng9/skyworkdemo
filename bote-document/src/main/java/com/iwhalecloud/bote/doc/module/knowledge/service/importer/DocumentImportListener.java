package com.iwhalecloud.bote.doc.module.knowledge.service.importer;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.DocumentParameterManageMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文档导入解析入库
 *
 * @author qian.sisheng
 * @since 2025-2-6
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentImportListener extends AnalysisEventListener<Map<Integer, String>> {

  private static final Logger logger = LoggerFactory.getLogger(DocumentImportListener.class);

  /** 一个批次最大入库数据量 */
  private static final int LIMIT = 1000;
  /** 表格行数据 */
  private final List<Map<Integer, String>> list = new ArrayList<>();
  /** 表头 */
  private Map<Integer, String> header = new HashMap<>();
  /** 租户 ID */
  private final Long tenantId;
  /** 文档ID */
  private final Long documentId;
  /** 文件流 */
  private final InputStream inputStream;
  /** 导入类型 */
  private final String importType;
  /** 文件类型 */
  private final String fileType;
  /** 排序 */
  private int sort = 1;
  /** 日期正则 */
  private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
  /** 日期时间正则 */
  private static final Pattern DATETIME_PATTERN = Pattern.compile(
    "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])\\s([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$");
  /** 浮点数正则 */
  private static final Pattern NUMBER_PATTERN = Pattern.compile("^[-+]?(\\d+\\.?\\d*|\\.\\d+)$");
  /** 整数正则 */
  private static final Pattern INTEGER_PATTERN = Pattern.compile("^[-+]?\\d+$");

  /** 数据类型优先级，值越大优先级越高 */
  private static final Map<String, Integer> DATA_TYPE_PRIORITY = new HashMap<>();

  static {
    DATA_TYPE_PRIORITY.put(AttrDataType.STRING.getCode(), 0);
    DATA_TYPE_PRIORITY.put(AttrDataType.NUMBER.getCode(), 1);
    DATA_TYPE_PRIORITY.put(AttrDataType.INTEGER.getCode(), 2);
    DATA_TYPE_PRIORITY.put(AttrDataType.DATE.getCode(), 3);
    DATA_TYPE_PRIORITY.put(AttrDataType.DATETIME.getCode(), 4);
  }

  private final DocumentParameterManageMapper parameterManageMapper;
  private final IDocumentContentManageService contentManageService;

  public DocumentImportListener(Long tenantId, Long documentId, InputStream inputStream, String importType, String fileType) {
    this.tenantId = tenantId;
    this.documentId = documentId;
    this.parameterManageMapper = SpringUtil.getBean(DocumentParameterManageMapper.class);
    this.contentManageService = SpringUtil.getBean(IDocumentContentManageService.class);
    this.inputStream = inputStream;
    this.importType = importType;
    this.fileType = fileType;
  }

  @Override
  public void invoke(Map<Integer, String> data, AnalysisContext context) {
    list.add(data);
    if (list.size() >= LIMIT) {
      // 批量入库
      saveDocument();
      list.clear();
    }
  }

  @Override
  public void doAfterAllAnalysed(AnalysisContext context) {
    if (CollectionUtils.isNotEmpty(list)) {
      saveDocument();
    }
  }

  @Override
  public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
    header = headMap;
    List<DocumentParameterDTO> parameterList = parameterManageMapper.selectDocumentParameterList(documentId, tenantId);
    if (CollectionUtils.isEmpty(parameterList)) {
      return;
    }
    if (KnowledgeConsts.IMPORT_TYPE_INCREMENT.equals(importType) && header.size() != parameterList.size()) {
      throw new BssException("表头与参数不匹配");
    }
    for (int i = 0; i < header.size(); i++) {
      String name = header.get(i);
      DocumentParameterDTO parameter = IterableUtils.find(parameterList, p -> name.equals(p.getParameterName()));
      if (KnowledgeConsts.IMPORT_TYPE_INCREMENT.equals(importType) && parameter == null) {
        throw new BssException("表头与参数不匹配");
      }
    }
  }

  private void saveDocument() {
    List<DocumentContentDTO> contentList = new ArrayList<>();
    List<DocumentParameterDTO> parameterList = parameterManageMapper.selectDocumentParameterList(documentId, tenantId);
    if (CollectionUtils.isEmpty(parameterList)) {
      parameterList = saveCorpusParameter();
    }
    convertCorpusContent(parameterList, contentList);
    DocumentDTO document = new DocumentDTO();
    document.setDocumentId(documentId);
    document.setTenantId(tenantId);
    document.setContents(contentList);
    document.setParameters(parameterList);
    contentManageService.saveDocumentContent(document);
  }

  private void convertCorpusContent(List<DocumentParameterDTO> parameterList, List<DocumentContentDTO> contentList) {
    try {
      for (Map<Integer, String> data : list) {
        DocumentContentDTO content = createCorpusContent();
        content.setSort(sort++);
        for (int i = 0; i < header.size(); i++) {
          String name = header.get(i);
          DocumentParameterDTO parameter = IterableUtils.find(parameterList, p -> Objects.equals(name, p.getParameterName()));
          if (parameter == null) {
            continue;
          }
          String value = data.get(i);
          value = StringUtils.isNotEmpty(value) ? value : parameter.getDefaultValue();
          BeanUtils.setProperty(content, parameter.getMappingCode(), value);
        }
        contentList.add(content);
      }
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to get filed, message={}", e.getMessage(), e);
      }
      throw new BssException("导入失败, 获取文档内容异常：" + e.getMessage(), e);
    }
  }

  private List<DocumentParameterDTO> saveCorpusParameter() {
    List<DocumentParameterDTO> parameters = new LinkedList<>();
    Map<Integer, Pair<Integer, String>> maxColumnLength = getMaxColumnLength();
    for (int i = 0; i < header.size(); i++) {
      String name = header.get(i);
      createCorpusParameter(name, parameters, maxColumnLength.get(i), i);
    }
    DocumentDTO document = new DocumentDTO();
    document.setParameters(parameters);
    document.setDocumentId(documentId);
    contentManageService.saveDocumentParameter(document);
    return parameters;
  }

  private void createCorpusParameter(String name, List<DocumentParameterDTO> parameters, Pair<Integer, String> pair, int i) {
    DocumentParameterDTO parameter = new DocumentParameterDTO();
    parameter.setParameterName(name);
    parameter.setTenantId(tenantId);
    parameter.setDocumentId(documentId);
    parameter.setDataType(pair.getRight());
    parameter.setMaxLength(pair.getLeft());
    parameter.setSort(i + 1);
    parameters.add(parameter);
  }

  private DocumentContentDTO createCorpusContent() {
    DocumentContentDTO content = new DocumentContentDTO();
    content.setDocumentId(documentId);
    content.setActionType("A");
    content.setTenantId(tenantId);
    content.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    content.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    return content;
  }

  public Map<Integer, Pair<Integer, String>> getMaxColumnLength() {
    Map<Integer, Pair<Integer, String>> maxLength = new HashMap<>();
    try {
      if ("csv".equals(fileType)) {
        readCsv(maxLength);
      }
      else {
        readXls(maxLength);
      }
    }
    catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to get max column length, message={}", e.getMessage(), e);
      }
      throw new BssException("导入失败, message：" + e.getMessage(), e);
    }
    return maxLength;
  }

  /**
   * 更新最大长度
   */
  private void updateMaxLength(Map<Integer, Pair<Integer, String>> maxLength, int i, String value, String dataType) {
    int length = value.length();
    if (!maxLength.containsKey(i)) {
      maxLength.put(i, Pair.of(length, dataType));
      return;
    }
    Pair<Integer, String> current = maxLength.get(i);
    // 类型取优先级更高的，长度取最大值
    String mergedType = DATA_TYPE_PRIORITY.getOrDefault(dataType, 0) > DATA_TYPE_PRIORITY.getOrDefault(current.getRight(), 0)
      ? dataType : current.getRight();
    int mergedLength = Math.max(length, current.getLeft());
    maxLength.put(i, Pair.of(mergedLength, mergedType));
  }

  private void readXls(Map<Integer, Pair<Integer, String>> maxLength) throws IOException {
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
          Cell cell = row.getCell(i);
          if (cell != null) {
            Pair<String, String> cellValue = getCellValue(cell);
            updateMaxLength(maxLength, i, cellValue.getLeft(), cellValue.getRight());
          }
        }
      }
    }
  }

  private void readCsv(Map<Integer, Pair<Integer, String>> maxLength) throws IOException {
    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
         CSVParser csvParser = CSVParser.builder().setReader(reader).setFormat(CSVFormat.DEFAULT).get()) {
      List<String> headers = csvParser.getHeaderNames();
      for (CSVRecord record : csvParser) {
        for (int i = 0; i < headers.size(); i++) {
          String value = record.get(i);
          updateMaxLength(maxLength, i, value, detectStringDataType(value));
        }
      }
    }
  }

  private Pair<String, String> getCellValue(Cell cell) {
    switch (cell.getCellType()) {
      case STRING:
        return handleStringCell(cell);
      case NUMERIC:
        return handleNumericCell(cell);
      case BOOLEAN:
        return Pair.of(String.valueOf(cell.getBooleanCellValue()), AttrDataType.STRING.getCode());
      case FORMULA:
        return Pair.of(cell.getCellFormula(), AttrDataType.STRING.getCode());
      case BLANK:
        return Pair.of("", AttrDataType.STRING.getCode());
      default:
        throw new BssException("未知的单元格类型: " + cell.getCellType());
    }
  }

  /**
   * 检测字符串数据类型
   */
  private String detectStringDataType(String value) {
    if (DATETIME_PATTERN.matcher(value).matches()) {
      return AttrDataType.DATETIME.getCode();
    }
    else if (DATE_PATTERN.matcher(value).matches()) {
      return AttrDataType.DATE.getCode();
    }
    else if (INTEGER_PATTERN.matcher(value).matches()) {
      return AttrDataType.INTEGER.getCode();
    }
    else if (NUMBER_PATTERN.matcher(value).matches()) {
      return AttrDataType.NUMBER.getCode();
    }
    return AttrDataType.STRING.getCode();
  }

  private Pair<String, String> handleStringCell(Cell cell) {
    String cellValue = cell.getStringCellValue();
    return Pair.of(cellValue, detectStringDataType(cellValue));
  }

  private Pair<String, String> handleNumericCell(Cell cell) {
    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
      return handleDateFormattedCell(cell);
    }
    else {
      double numericValue = cell.getNumericCellValue();
      if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
        String intValue = String.valueOf((long) numericValue);
        return Pair.of(intValue, AttrDataType.INTEGER.getCode());
      }
      String numericStr = String.valueOf(numericValue);
      return Pair.of(numericStr, AttrDataType.NUMBER.getCode());
    }
  }

  /**
   * 处理日期格式化单元格
   */
  private Pair<String, String> handleDateFormattedCell(Cell cell) {
    Date date = cell.getDateCellValue();
    String formattedDate = DateUtil.formatDate(date);
    // 判断是否含有时间部分（时分秒不全为0则为datetime）
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    boolean hasTime = cal.get(Calendar.HOUR_OF_DAY) != 0
      || cal.get(Calendar.MINUTE) != 0
      || cal.get(Calendar.SECOND) != 0;
    return Pair.of(formattedDate, hasTime ? AttrDataType.DATETIME.getCode() : AttrDataType.DATE.getCode());
  }

}

