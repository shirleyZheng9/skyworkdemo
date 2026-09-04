package com.iwhalecloud.bote.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel操作工具类
 */
@SuppressWarnings({"PMD.LooseCoupling", "PMD.GuardLogStatement"})
public final class ExcelUtil {

  private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

  private ExcelUtil() {
  }
  /**
   * 读取Excel 文件中内容
   *
   * @param initialRow 从第几行开始解析（0-based）
   * @param isXls 是否xls格式
   */
  public static List<List<String>> getExcelData(InputStream inputStream, int initialRow, boolean isXls) throws IOException {
    List<List<String>> squareList = new ArrayList<>();
    try (Workbook workbook = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      DataFormatter dataFormatter = new DataFormatter();

      for (int rowIndex = initialRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
          continue;
        }
        List<String> rowData = new ArrayList<>();

        for (int i = 0; i < row.getLastCellNum(); i++) {
          Cell cell = row.getCell(i, MissingCellPolicy.RETURN_NULL_AND_BLANK);
          if (cell == null) {
            rowData.add("");
            continue;
          }

          String cellValue = dataFormatter.formatCellValue(cell);
          rowData.add(cellValue);
        }

        squareList.add(rowData);
      }
    }
    return squareList;
  }

  public static List<Map<String, Object>> getExcelData(InputStream inputStream, int initialRow, boolean isXls,
                                                       String[] colName) {
    List<Map<String, Object>> squareList = new ArrayList<>();
    DataFormatter dataFormatter = new DataFormatter();

    try (Workbook workbook = isXls ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      if (sheet == null) {
        Map<String, Object> errmap = new HashMap<>();
        errmap.put("errMsg", "工作表为空，请检查文件内容!");
        squareList.add(errmap);
        return squareList;
      }

      if (!validateExcelTemplate(workbook, colName)) {
        Map<String, Object> errmap = new HashMap<>();
        errmap.put("errMsg", "模板不匹配，请使用正确模板!");
        squareList.add(errmap);
        return squareList;
      }

      loopRow(sheet, initialRow, colName, dataFormatter, squareList);

    }
    catch (IOException e) {
      log.error("读取Excel出错: {}", e.getMessage());
      Map<String, Object> errmap = new HashMap<>();
      errmap.put("errMsg", "读取Excel出错：" + e.getMessage());
      squareList.add(errmap);
    }

    return squareList;
  }

  /**
   * 遍历工作表中的每一行数据，并转换为Map对象存入列表中
   *
   * @param sheet 工作表对象
   * @param initialRow 起始行号（0-based）
   * @param colName 列名数组
   * @param dataFormatter 数据格式化工具
   * @param squareList 输出结果列表
   */
  private static void loopRow(Sheet sheet, int initialRow, String[] colName,
                              DataFormatter dataFormatter, List<Map<String, Object>> squareList) {
    // 获取物理行数（包括空行）
    int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
    if (physicalNumberOfRows <= initialRow) {
      return;
    }

    for (int rowIndex = initialRow; rowIndex < physicalNumberOfRows; rowIndex++) {
      Row row = sheet.getRow(rowIndex);
      if (row == null) {
        continue;
      }

      // 检查行是否为空（所有单元格都为空或只包含空格）
      boolean isEmptyRow;
      Map<String, Object> mapCol = new HashMap<>();
      mapCol.put(colName[0], rowIndex); // 第一列为行索引
      isEmptyRow = loopCell(row, colName, dataFormatter, mapCol);
      // 只添加非空行
      if (!isEmptyRow) {
        squareList.add(mapCol);
      }
    }
  }

  private static boolean loopCell(Row row, String[] colName, DataFormatter dataFormatter, Map<String, Object> mapCol) {
    boolean isEmptyRow = true;
    for (int i = 0; i < colName.length; i++) {
      Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
      String cellValue = "";

      if (cell != null) {
        // 使用 DataFormatter 处理各种类型（数字、字符串、日期、公式等）
        cellValue = dataFormatter.formatCellValue(cell);

        // 如果是日期类型，用指定格式输出
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
          cellValue = com.iwhalecloud.bss.litchi.util.DateUtil.format(cell.getDateCellValue());
        }
      }

      // 如果单元格不为空且不只包含空格，则行不为空
      if (cellValue != null && !cellValue.trim().isEmpty()) {
        isEmptyRow = false;
      }

      mapCol.put(colName[i], cellValue);
    }
    return isEmptyRow;
  }




  /**
   * 验证Excel模板格式是否正确
   *
   * @param workbook Excel工作簿
   * @param colName 列名数组
   * @return true - 格式正确，false - 格式错误
   */
  private static boolean validateExcelTemplate(Workbook workbook, String[] colName) {
    Sheet sheet = workbook.getSheetAt(0);

    // 检查是否存在标题行
    Row firstRow = sheet.getRow(0);
    if (firstRow == null) {
      return false;
    }

    // 检查列数是否一致
    if (colName.length != firstRow.getLastCellNum()) {
      return false;
    }

    // 校验标题是否与 colName 完全一致
    for (int i = 0; i < colName.length; i++) {
      Cell cell = firstRow.getCell(i);
      String actualTitle = "";
      if (cell != null) {
        actualTitle = cell.getStringCellValue().trim();
      }
      if (!colName[i].equals(actualTitle)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 导出Excel文档
   */
  public static void exportExcelDocument(Map<String, Object> param, Map<String, String> titleMap,
                                         List<Map<String, Object>> result, OutputStream out) {
    try (Workbook workbook = new XSSFWorkbook()) {
      genWorkbook(workbook, param, titleMap, result);
      workbook.write(out);
      out.flush();
    }
    catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  public static void genWorkbook(Workbook workbook, Map<String, Object> param, Map<String, String> titleMap, List<Map<String, Object>> result) {
    // 生成一个表格
    Sheet sheet = workbook.createSheet(param.get("title") == null ? "未命名" : String.valueOf(param.get("title")));

    // 网格线
    sheet.setDisplayGridlines(true);
    // 设置默认表宽
    sheet.setDefaultColumnWidth(30);

    // 第一行：titleMap 的 Key
    Row keyRow = sheet.createRow(0);
    // 第二行：titleMap 的 Value（显示标题）
    Row valueRow = sheet.createRow(1);

    // 样式：标题样式
    CellStyle titleStyle = workbook.createCellStyle();
    titleStyle.setAlignment(HorizontalAlignment.CENTER);
    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    Font titleFont = workbook.createFont();
    titleFont.setFontName("Arial");
    titleFont.setFontHeightInPoints((short) 12);
    titleFont.setBold(false);
    titleStyle.setFont(titleFont);

    // 填充第一行（Key）和第二行（Value）
    int count = 0;
    for (Entry<String, String> entry : titleMap.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      // 第一行填入 key
      Cell keyCell = keyRow.createCell(count);
      keyCell.setCellStyle(titleStyle);
      keyCell.setCellValue(key);

      keyRow.setHeight((short) 0);

      // 第二行填入 value
      Cell valueCell = valueRow.createCell(count);
      valueCell.setCellStyle(titleStyle);
      valueCell.setCellValue(value);

      count++;
    }

    // 样式：内容样式
    CellStyle contentStyle = workbook.createCellStyle();
    contentStyle.setAlignment(HorizontalAlignment.CENTER);
    contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

    Font contentFont = workbook.createFont();
    contentFont.setFontName("Arial");
    contentFont.setFontHeightInPoints((short) 10);
    contentStyle.setFont(contentFont);

    // 填充数据行（从第2行开始）
    for (int i = 0, iSize = result.size(); i < iSize; i++) {
      Map<String, Object> resultMap = result.get(i);
      Row row = sheet.createRow(i + 2); // 跳过前两行标题
      count = 0;

      for (String key : titleMap.keySet()) {
        Cell cell = row.createCell(count);
        cell.setCellStyle(contentStyle);
        Object value = resultMap.get(key);
        cell.setCellValue(value != null ? String.valueOf(value) : "");
        count++;
      }
    }
  }


}
