package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ExcelToDataPluginParams;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 图生视频
 *
 * @author fan.cong
 * @since 2025-08-06
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class ExcelToDataPlugin extends AbstractPlugin<ExcelToDataPluginParams> {
  private static final List<String> excelTypes = Arrays.asList("xlsx", "xls");

  private final IFileStoreService fileStoreService;

  public ExcelToDataPlugin(IFileStoreService fileStoreService) {
    super(ExcelToDataPluginParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("fileId", "文件id", AttrDataType.INTEGER),
      ParameterSpec.newProperty("dataType", "数据类型", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING),
      ParameterSpec.newProperty("data", "数据", AttrDataType.OBJECT)));
  }

  @Override
  public void validateParams(ExcelToDataPluginParams params) {
    Assert.notNull(params.getFileId(), "文件id不能为空");
    Assert.notNull(params.getDataType(), "返回数据类型不能为空");
  }

  @Override
  public Object doRun(ExcelToDataPluginParams pluginParams) {
    Map<String, Object> result = new HashMap<>();
    FileInfoVO fileInfoVO = fileStoreService.getFileInfoById(pluginParams.getFileId());
    if (fileInfoVO == null || !excelTypes.contains(fileInfoVO.getFileType()) || fileInfoVO.getFileSize() <= 0) {
      result.put("message", "获取到的文件为空或文件类型不正确");
      return result;
    }
    try (BufferedInputStream bis = new BufferedInputStream(fileStoreService.downloadFileStream(fileInfoVO))) {
      Object data;
      if ("list".equals(pluginParams.getDataType())) {
        data = getListData(bis);
      }
      else {
        data = getMapData(bis);
      }
      result.put("message", "获取数据成功");
      result.put("data", data);
    }
    catch (Exception e) {
      logger.error("获取数据异常，文件ID: {}, 数据类型: {}", pluginParams.getFileId(), pluginParams.getDataType(), e);
      result.put("message", "获取数据异常");
    }
    return result;
  }

  public Map<String, List<List<Object>>> getListData(BufferedInputStream inputStream) throws Exception {
    Map<String, List<List<Object>>> result = new LinkedHashMap<>();
    Workbook workbook = WorkbookFactory.create(inputStream);
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);
      String sheetName = sheet.getSheetName();
      List<List<Object>> sheetData = new ArrayList<>();
      for (Row row : sheet) {
        List<Object> rowData = new ArrayList<>();
        for (Cell cell : row) {
          rowData.add(getCellValue(cell));
        }
        sheetData.add(rowData);
      }
      result.put(sheetName, sheetData);
    }
    workbook.close();
    return result;
  }

  public Map<String, List<Map<Integer, Map<String, Object>>>> getMapData(BufferedInputStream inputStream)
    throws Exception {
    Map<String, List<Map<Integer, Map<String, Object>>>> result = new LinkedHashMap<>();
    Workbook workbook = WorkbookFactory.create(inputStream);
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);
      String sheetName = sheet.getSheetName();
      List<Map<Integer, Map<String, Object>>> sheetData = new ArrayList<>();
      Row headerRow = sheet.getRow(0);
      if (headerRow == null) {
        continue;
      }
      List<String> headers = new ArrayList<>();
      for (Cell cell : headerRow) {
        headers.add(getCellValue(cell).toString());
      }
      for (int r = 1; r <= sheet.getLastRowNum(); r++) {
        Row row = sheet.getRow(r);
        if (row == null) {
          continue;
        }
        Map<String, Object> rowMap = new LinkedHashMap<>();
        for (int c = 0; c < headers.size(); c++) {
          Cell cell = row.getCell(c);
          rowMap.put(headers.get(c), cell == null ? null : getCellValue(cell));
        }
        Map<Integer, Map<String, Object>> rowWrapper = new HashMap<>();
        rowWrapper.put(r, rowMap);
        sheetData.add(rowWrapper);
      }
      result.put(sheetName, sheetData);
    }
    workbook.close();
    return result;
  }

  private Object getCellValue(Cell cell) {
    if (cell == null) {
      return null;
    }
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue();
        }
        else {
          return cell.getNumericCellValue();
        }
      case BOOLEAN:
        return cell.getBooleanCellValue();
      case FORMULA:
        try {
          return cell.getNumericCellValue();
        }
        catch (Exception e) {
          return cell.getStringCellValue();
        }
      case BLANK:
        return "";
      default:
        return cell.toString();
    }
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_EXCEL_TO_DATA;
  }
}
