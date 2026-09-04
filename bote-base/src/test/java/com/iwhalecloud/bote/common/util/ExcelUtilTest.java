package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * {@link ExcelUtil} 单元测试
 *
 * <p>覆盖 getExcelData(InputStream, int, boolean) 读取单元格数据（含空行跳过）；
 * getExcelData(InputStream, int, boolean, String[]) 带列名映射（含模板校验、空行过滤、日期格式化）；
 * exportExcelDocument 与 genWorkbook 生成 Excel（含标题行、数据行、样式设置）。
 * 使用内存中的 XSSFWorkbook 构建 fixture，不依赖文件系统或 Spring。</p>
 */
class ExcelUtilTest {

  // ==================== getExcelData(InputStream, int, boolean) ====================

  @Test
  void getExcelData_simpleXlsx_returnsRows() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      header.createCell(1).setCellValue("Age");
      Row data = sheet.createRow(1);
      data.createCell(0).setCellValue("Alice");
      data.createCell(1).setCellValue(30);
      wb.write(baos);
    }

    List<List<String>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 0, false);

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).containsExactly("Name", "Age");
    assertThat(result.get(1)).containsExactly("Alice", "30");
  }

  @Test
  void getExcelData_skipRows_returnsFromInitialRow() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row r0 = sheet.createRow(0);
      r0.createCell(0).setCellValue("Header");
      Row r1 = sheet.createRow(1);
      r1.createCell(0).setCellValue("Data1");
      wb.write(baos);
    }

    List<List<String>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).get(0)).isEqualTo("Data1");
  }

  @Test
  void getExcelData_emptyCell_returnsEmptyString() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row row = sheet.createRow(0);
      row.createCell(0).setCellValue("A");
      // cell 1 未创建 -> 空
      row.createCell(2).setCellValue("C");
      wb.write(baos);
    }

    List<List<String>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 0, false);

    assertThat(result.get(0)).containsExactly("A", "", "C");
  }

  // ==================== getExcelData(InputStream, int, boolean, String[]) ====================

  @Test
  void getExcelDataWithColName_matchingTemplate_returnsRowsAsMaps() throws IOException {
    String[] colName = {"Name", "Age"};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      header.createCell(1).setCellValue("Age");
      Row data = sheet.createRow(1);
      data.createCell(0).setCellValue("Alice");
      data.createCell(1).setCellValue("30");
      wb.write(baos);
    }

    List<Map<String, Object>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false, colName);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).containsEntry("Name", "Alice");
    assertThat(result.get(0)).containsEntry("Age", "30");
  }

  @Test
  void getExcelDataWithColName_mismatchedTemplate_returnsErrMsg() throws IOException {
    String[] colName = {"ColA", "ColB"};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("WrongHeader");
      header.createCell(1).setCellValue("WrongHeader2");
      wb.write(baos);
    }

    List<Map<String, Object>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false, colName);

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).containsKey("errMsg");
    assertThat((String) result.get(0).get("errMsg")).contains("模板不匹配");
  }

  @Test
  void getExcelDataWithColName_emptyRows_returnsEmptyList() throws IOException {
    String[] colName = {"Name"};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      // 无数据行
      wb.write(baos);
    }

    List<Map<String, Object>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false, colName);

    assertThat(result).isEmpty();
  }

  @Test
  void getExcelDataWithColName_allEmptyRows_filtered() throws IOException {
    String[] colName = {"Name", "Age"};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      header.createCell(1).setCellValue("Age");
      Row emptyRow = sheet.createRow(1);
      emptyRow.createCell(0).setCellValue("");
      emptyRow.createCell(1).setCellValue("  ");
      wb.write(baos);
    }

    List<Map<String, Object>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false, colName);

    // 全空行被过滤
    assertThat(result).isEmpty();
  }

  @Test
  void getExcelDataWithColName_dateCell_formatted() throws IOException {
    String[] colName = {"Name", "Date"};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      header.createCell(1).setCellValue("Date");
      Row data = sheet.createRow(1);
      data.createCell(0).setCellValue("Alice");
      Cell dateCell = data.createCell(1);
      dateCell.setCellValue(new Date());
      CellStyle dateStyle = wb.createCellStyle();
      dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd"));
      dateCell.setCellStyle(dateStyle);
      wb.write(baos);
    }

    List<Map<String, Object>> result = ExcelUtil.getExcelData(
        new ByteArrayInputStream(baos.toByteArray()), 1, false, colName);

    assertThat(result).hasSize(1);
    String dateStr = (String) result.get(0).get("Date");
    assertThat(dateStr).contains("-"); // 日期格式包含 -
  }

  // ==================== exportExcelDocument / genWorkbook ====================

  @Test
  void exportExcelDocument_validData_producesXlsx() throws IOException {
    Map<String, Object> param = Map.of("title", "Test Report");
    Map<String, String> titleMap = new LinkedHashMap<>();
    titleMap.put("name", "Name");
    titleMap.put("age", "Age");
    List<Map<String, Object>> data = List.of(
        Map.of("name", "Alice", "age", "30"),
        Map.of("name", "Bob", "age", "25"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ExcelUtil.exportExcelDocument(param, titleMap, data, out);

    assertThat(out.size()).isGreaterThan(0);

    // 验证生成的文件可被读取
    try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
      Sheet sheet = wb.getSheetAt(0);
      assertThat(sheet.getSheetName()).isEqualTo("Test Report");
      // 第一行：key
      Row keyRow = sheet.getRow(0);
      assertThat(keyRow.getCell(0).getStringCellValue()).isEqualTo("name");
      assertThat(keyRow.getCell(1).getStringCellValue()).isEqualTo("age");
      // 第二行：value
      Row valueRow = sheet.getRow(1);
      assertThat(valueRow.getCell(0).getStringCellValue()).isEqualTo("Name");
      assertThat(valueRow.getCell(1).getStringCellValue()).isEqualTo("Age");
      // 第三行：数据
      Row dataRow = sheet.getRow(2);
      assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("Alice");
      assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("30");
    }
  }

  @Test
  void exportExcelDocument_nullTitle_usesDefaultName() throws IOException {
    Map<String, Object> param = new LinkedHashMap<>(); // 无 title
    Map<String, String> titleMap = new LinkedHashMap<>();
    titleMap.put("key1", "Header1");
    List<Map<String, Object>> data = List.of(Map.of("key1", "val1"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ExcelUtil.exportExcelDocument(param, titleMap, data, out);

    try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
      assertThat(wb.getSheetAt(0).getSheetName()).isEqualTo("未命名");
    }
  }

  @Test
  void genWorkbook_emptyData_createsHeaderOnly() throws IOException {
    Map<String, Object> param = Map.of("title", "Empty");
    Map<String, String> titleMap = new LinkedHashMap<>();
    titleMap.put("col1", "Column1");
    titleMap.put("col2", "Column2");

    try (Workbook wb = new XSSFWorkbook()) {
      ExcelUtil.genWorkbook(wb, param, titleMap, List.of());

      Sheet sheet = wb.getSheetAt(0);
      assertThat(sheet.getSheetName()).isEqualTo("Empty");
      // 只有标题行（key 行和 value 行），无数据行
      assertThat(sheet.getLastRowNum()).isEqualTo(1);
    }
  }

  @Test
  void exportExcelDocument_multipleDataRows_allWritten() throws IOException {
    Map<String, Object> param = Map.of("title", "Multi");
    Map<String, String> titleMap = new LinkedHashMap<>();
    titleMap.put("id", "ID");

    List<Map<String, Object>> data = List.of(
        Map.of("id", "1"),
        Map.of("id", "2"),
        Map.of("id", "3"));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ExcelUtil.exportExcelDocument(param, titleMap, data, out);

    try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
      Sheet sheet = wb.getSheetAt(0);
      // 2 header rows + 3 data rows = 5 rows (0-4)
      assertThat(sheet.getLastRowNum()).isEqualTo(4);
      assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("1");
      assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("2");
      assertThat(sheet.getRow(4).getCell(0).getStringCellValue()).isEqualTo("3");
    }
  }
}
