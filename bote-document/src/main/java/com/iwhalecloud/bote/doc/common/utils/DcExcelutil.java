package com.iwhalecloud.bote.doc.common.utils;

import com.iwhalecloud.bote.doc.common.utils.converter.ImageLoadResult;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.utils.converter.DcExcelHelper;
import com.iwhalecloud.bote.doc.module.document.dto.AttachmentUrlInfoDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.doc.module.document.service.helper.DocumentAttachmentHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

/**
 * excel工具类
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class DcExcelutil {

  private static final Logger logger = LoggerFactory.getLogger(DcExcelutil.class);
  // LuckySheet 资源名称常量
  private static final String RESOURCE_SHEET_DRAWING_PLUGIN = "SHEET_DRAWING_PLUGIN";

  // LuckySheet 资源名称常量（顺序必须与在线文档保持一致）
  private static final String[] LUCKYSHEET_RESOURCE_NAMES = {
    "SHEET_RANGE_PROTECTION_PLUGIN", "SHEET_AuthzIoMockService_PLUGIN", "SHEET_WORKSHEET_PROTECTION_PLUGIN",
    "SHEET_WORKSHEET_PROTECTION_POINT_PLUGIN", RESOURCE_SHEET_DRAWING_PLUGIN, "SHEET_DEFINED_NAME_PLUGIN",
    "SHEET_RANGE_THEME_MODEL_PLUGIN"
  };

  private static final int SHEET_ID_LENGTH = 22;
  private DcExcelutil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 将LuckySheet JSON数据转换为Excel文件
   *
   * @param jsonData LuckySheet JSON数据
   * @param outputPath 输出文件路径
   * @param documentAttachmentHelper 文档附件帮助类，用于加载附件图片（必填，如果为null则无法下载附件图片）
   * @throws IOException 当文件读取或写入失败时抛出
   */
  public static void convertLuckysheetToExcel(String jsonData, String outputPath,
    DocumentAttachmentHelper documentAttachmentHelper) throws IOException {
    logger.debug("convertLuckysheetToExcel: 开始转换，outputPath={}, documentAttachmentHelper={}", outputPath,
      documentAttachmentHelper != null ? "not null" : "null");

    if (DocBaseConsts.DEFULT_NULL_CONTENT.equals(jsonData)) {
      jsonData = DocBaseConsts.DEFULT_WORK_BOOK_CONTENT;
    }
    JsonNode json = JsonUtil.getObjectMapper().readTree(jsonData);
    JsonNode sheets = json.get("sheets");

    if (sheets == null || sheets.isNull()) {
      throw new IllegalArgumentException("JSON数据中缺少 'sheets' 节点");
    }

    int sheetCount = sheets.size();
    logger.debug("convertLuckysheetToExcel: 找到 {} 个 sheet", sheetCount);

    try (XSSFWorkbook workbook = new XSSFWorkbook();
      FileOutputStream fos = new FileOutputStream(outputPath);
      BufferedOutputStream out = new BufferedOutputStream(fos)) {
      CreationHelper helper = workbook.getCreationHelper();
      Map<String, Sheet> sheetIdMap = new HashMap<>();

      processAllSheets(sheets, workbook, helper, sheetIdMap, documentAttachmentHelper);
      processFloatingImages(json, workbook, sheetIdMap, documentAttachmentHelper);

      workbook.write(out);
    }
  }

  /**
   * 处理所有sheet
   */
  private static void processAllSheets(JsonNode sheets, XSSFWorkbook workbook, CreationHelper helper,
    Map<String, Sheet> sheetIdMap, DocumentAttachmentHelper documentAttachmentHelper) {
    Iterator<String> sheetIterator = sheets.fieldNames();
    while (sheetIterator.hasNext()) {
      String sheetId = sheetIterator.next();
      JsonNode sheet = sheets.get(sheetId);
      String sheetName = sheet.get("name").asText();
      Sheet excelSheet = workbook.createSheet(sheetName);
      Drawing<?> drawing = excelSheet.createDrawingPatriarch();
      sheetIdMap.put(sheetId, excelSheet);

      JsonNode cellData = sheet.get("cellData");
      if (cellData != null && !cellData.isNull()) {
        int rowCount = cellData.size();
        logger.debug("convertLuckysheetToExcel: Sheet {} 有 {} 行数据", sheetName, rowCount);
        processSheetCellData(cellData, excelSheet, workbook, helper, drawing, documentAttachmentHelper);
      }
      else {
        logger.debug("convertLuckysheetToExcel: Sheet {} 没有 cellData", sheetName);
      }
    }
  }

  /**
   * 处理单个sheet的单元格数据
   */
  private static void processSheetCellData(JsonNode cellData, Sheet excelSheet, Workbook workbook,
    CreationHelper helper, Drawing<?> drawing, DocumentAttachmentHelper documentAttachmentHelper) {
    Iterator<String> rowIterator = cellData.fieldNames();
    while (rowIterator.hasNext()) {
      String rowKey = rowIterator.next();
      int rowNum = Integer.parseInt(rowKey);
      Row row = excelSheet.createRow(rowNum);

      JsonNode rowData = cellData.get(rowKey);
      if (rowData == null) {
        continue;
      }

      Iterator<String> colIterator = rowData.fieldNames();
      while (colIterator.hasNext()) {
        String colKey = colIterator.next();
        int colNum = Integer.parseInt(colKey);
        Cell cell = row.createCell(colNum);

        JsonNode cellObj = rowData.get(colKey);
        processCell(cellObj, cell, workbook, helper, drawing, rowNum, colNum, documentAttachmentHelper);
      }
    }
  }

  /**
   * 处理单个单元格
   */
  private static void processCell(JsonNode cellObj, Cell cell, Workbook workbook, CreationHelper helper,
    Drawing<?> drawing, int rowNum, int colNum, DocumentAttachmentHelper documentAttachmentHelper) {
    boolean hasImage = DcExcelHelper.hasCellImage(cellObj);
    if (hasImage) {
      logger.debug("convertLuckysheetToExcel: 单元格 [{}][{}] 有图片", rowNum, colNum);
    }

    if (!hasImage) {
      DcExcelHelper.setCellValue(cell, cellObj);
    }

    processCellImages(cellObj, workbook, helper, drawing, rowNum, colNum, documentAttachmentHelper);
  }

  /**
   * 处理单元格中的图片
   */
  private static void processCellImages(JsonNode cellObj, Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    int rowNum, int colNum, DocumentAttachmentHelper documentAttachmentHelper) {
    if (cellObj == null || cellObj.isNull()) {
      logger.debug("processCellImages: cellObj 为 null, row={}, col={}", rowNum, colNum);
      return;
    }

    JsonNode pObj = cellObj.get("p");
    if (pObj == null || pObj.isNull()) {
      logger.debug("processCellImages: cellObj 中没有 p 节点, row={}, col={}", rowNum, colNum);
      return;
    }

    JsonNode drawings = pObj.get("drawings");
    if (drawings == null || drawings.isNull()) {
      logger.debug("processCellImages: p 节点中没有 drawings, row={}, col={}", rowNum, colNum);
      return;
    }

    int drawingCount = drawings.size();
    logger.debug("processCellImages: 找到 {} 个单元格图片, row={}, col={}", drawingCount, rowNum, colNum);

    // 遍历所有图片
    Iterator<String> drawingIterator = drawings.fieldNames();
    while (drawingIterator.hasNext()) {
      String drawingId = drawingIterator.next();
      JsonNode drawingObj = drawings.get(drawingId);
      logger.debug("processCellImages: 处理单元格图片, drawingId={}, row={}, col={}", drawingId, rowNum, colNum);
      addImageToCell(drawingObj, workbook, helper, drawing, rowNum, colNum, documentAttachmentHelper);
    }
  }

  /**
   * 添加图片到单元格
   */
  private static void addImageToCell(JsonNode drawingObj, Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    int rowNum, int colNum, DocumentAttachmentHelper documentAttachmentHelper) {
    String imageSource = DcExcelHelper.extractImageSourceFromDrawing(drawingObj, rowNum, colNum);
    if (imageSource == null) {
      return;
    }

    logger.debug("addImageToCell: 开始处理图片, row={}, col={}, imageSource={}", rowNum, colNum, imageSource);

    ImageLoadResult loadResult = loadImageData(imageSource, documentAttachmentHelper);
    if (!DcExcelHelper.isValidLoadResult(loadResult)) {
      return;
    }

    byte[] imageBytes = loadResult.getImageBytes();
    Integer pictureType = loadResult.getPictureType();
    if (pictureType == null) {
      logger.warn("addImageToCell: 图片类型为null, row={}, col={}", rowNum, colNum);
      return;
    }
    logger.debug("addImageToCell: 图片数据准备完成, row={}, col={}, imageBytes.length={} bytes, pictureType={}", rowNum, colNum,
      imageBytes.length, pictureType);

    processImageWithTransform(drawingObj, workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType);
  }


  /**
   * 处理图片（根据transform情况）
   */
  private static void processImageWithTransform(JsonNode drawingObj, Workbook workbook, CreationHelper helper,
    Drawing<?> drawing, int rowNum, int colNum, byte[] imageBytes, int pictureType) {
    int[] actualSize = getImageActualSize(imageBytes);
    int actualWidth = actualSize[0];
    int actualHeight = actualSize[1];

    JsonNode transform = drawingObj.get("transform");
    if (transform == null || transform.isNull()) {
      processImageWithoutTransform(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, actualWidth,
        actualHeight);
      return;
    }

    processImageWithValidTransform(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, transform,
      actualWidth, actualHeight);
  }

  /**
   * 处理无transform的图片
   */
  private static void processImageWithoutTransform(Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    int rowNum, int colNum, byte[] imageBytes, int pictureType, int actualWidth, int actualHeight) {
    if (actualWidth > 0 && actualHeight > 0) {
      addImageToCellWithoutTransform(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, actualWidth,
        actualHeight);
    }
  }

  /**
   * 处理有transform的图片
   */
  private static void processImageWithValidTransform(Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    int rowNum, int colNum, byte[] imageBytes, int pictureType, JsonNode transform, int actualWidth, int actualHeight) {
    double[] dimensions = getDimensionsFromTransform(transform, actualWidth, actualHeight);
    double width = dimensions[0];
    double height = dimensions[1];

    int[] sizeInEMU = calculateAndLimitCellImageSize(width, height);
    insertPictureToCell(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, sizeInEMU[0], sizeInEMU[1],
      width);
  }


  /**
   * 加载图片数据（从Base64或URL）
   */
  private static ImageLoadResult loadImageData(String imageSource, DocumentAttachmentHelper documentAttachmentHelper) {
    // 处理 Base64 格式的图片
    if (imageSource.startsWith("data:image")) {
      return DcExcelHelper.loadBase64Image(imageSource);
    }
    // 处理 URL 格式的图片（附件图片）
    else if (imageSource.startsWith("/") || imageSource.startsWith("http://") || imageSource.startsWith("https://")) {
      return loadUrlImage(imageSource, documentAttachmentHelper);
    }
    else {
      logger.debug("不支持的图片格式: {}", imageSource);
      return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
    }
  }

  /**
   * 加载URL格式图片（附件图片）
   */
  private static ImageLoadResult loadUrlImage(String imageSource, DocumentAttachmentHelper documentAttachmentHelper) {
    logger.debug("addImageToCell: 检测到URL格式图片, imageSource={}", imageSource);
    try {
      if (documentAttachmentHelper == null) {
        logger.warn("无法加载单元格图片，DocumentAttachmentHelper为null: imageSource={}", imageSource);
        return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
      }

      boolean isAttachment = DcExcelHelper.isAttachmentUrl(imageSource);
      logger.debug("addImageToCell: isAttachmentUrl 结果={}, imageSource={}", isAttachment, imageSource);

      if (!isAttachment) {
        logger.warn("跳过非附件URL格式的图片: imageSource={}", imageSource);
        return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
      }

      AttachmentUrlInfoDTO urlInfo = DcExcelHelper.parseAttachmentUrl(imageSource);
      logger.debug("addImageToCell: parseAttachmentUrl 结果={}, imageSource={}", urlInfo != null ? "成功" : "失败",
        imageSource);

      if (urlInfo == null) {
        logger.error("解析附件URL失败: imageSource={}", imageSource);
        return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
      }

      return loadAttachmentImage(urlInfo, documentAttachmentHelper);
    }
    catch (Exception e) {
      logger.error("加载附件图片失败: url={}, error={}", imageSource, e.getMessage(), e);
      return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
    }
  }

  /**
   * 加载附件图片
   */
  private static ImageLoadResult loadAttachmentImage(AttachmentUrlInfoDTO urlInfo,
    DocumentAttachmentHelper documentAttachmentHelper) {
    logger.debug("开始加载单元格图片: documentId={}, fileName={}", urlInfo.getDocumentId(), urlInfo.getFileName());
    try (InputStream inputStream = documentAttachmentHelper.loadAttachment(urlInfo.getDocumentId(),
      urlInfo.getFileName(), Boolean.TRUE)) {
      if (inputStream == null) {
        logger.error("加载附件图片失败，返回null: documentId={}, fileName={}", urlInfo.getDocumentId(), urlInfo.getFileName());
        return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
      }

      byte[] imageBytes = DcExcelHelper.readInputStream(inputStream);
      logger.debug("成功加载单元格图片: documentId={}, fileName={}, size={} bytes", urlInfo.getDocumentId(),
        urlInfo.getFileName(), imageBytes.length);

      int pictureType = getPictureTypeFromFileName(urlInfo.getFileName());
      return new ImageLoadResult(true, imageBytes, pictureType);
    }
    catch (IOException e) {
      logger.error("读取附件图片输入流失败: documentId={}, fileName={}, error={}", urlInfo.getDocumentId(), urlInfo.getFileName(),
        e.getMessage(), e);
      return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
    }
  }

  /**
   * 根据文件名获取图片类型
   */
  private static int getPictureTypeFromFileName(String fileName) {
    String lowerFileName = fileName.toLowerCase();
    if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
      return Workbook.PICTURE_TYPE_JPEG;
    }
    else if (lowerFileName.endsWith(".png")) {
      return Workbook.PICTURE_TYPE_PNG;
    }
    else if (lowerFileName.endsWith(".gif")) {
      // 注意：Apache POI 不支持 GIF 格式，GIF 图片使用 PNG 类型处理
      return Workbook.PICTURE_TYPE_PNG;
    }
    return Workbook.PICTURE_TYPE_PNG;
  }

  /**
   * 获取图片实际尺寸
   */
  private static int[] getImageActualSize(byte[] imageBytes) {
    try {
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
      if (image != null) {
        return new int[] {
          image.getWidth(), image.getHeight()
        };
      }
    }
    catch (Exception e) {
      logger.debug("无法读取图片实际尺寸，将使用 transform 中的尺寸: {}", e.getMessage());
    }
    return new int[] {
      0, 0
    };
  }

  /**
   * 从transform节点获取尺寸
   */
  private static double[] getDimensionsFromTransform(JsonNode transform, int actualWidth, int actualHeight) {
    double width = transform.has("width") ? transform.get("width").asDouble() : 100;
    double height = transform.has("height") ? transform.get("height").asDouble() : 20;

    // 如果 transform 中的尺寸太小（小于20像素），使用图片实际尺寸
    if (width < 20 && actualWidth > 0) {
      width = actualWidth;
      logger.debug("transform 中的宽度 {} 太小，使用图片实际宽度 {}", transform.has("width") ? transform.get("width").asDouble() : 0,
        actualWidth);
    }
    if (height < 20 && actualHeight > 0) {
      height = actualHeight;
      logger.debug("transform 中的高度 {} 太小，使用图片实际高度 {}", transform.has("height") ? transform.get("height").asDouble() : 0,
        actualHeight);
    }

    return new double[] {
      width, height
    };
  }

  /**
   * 计算并限制单元格图片尺寸（转换为EMU并限制在单元格大小内）
   */
  private static int[] calculateAndLimitCellImageSize(double width, double height) {
    // 1 像素 = 9525 EMU (96 DPI)
    int widthEMU = (int) (width * 9525);
    int heightEMU = (int) (height * 9525);

    // 单元格宽度和高度（EMU）：默认单元格宽度 88 像素，高度 24 像素
    int cellWidthEMU = 88 * 9525;
    int cellHeightEMU = 24 * 9525;

    // 限制图片尺寸不超过单元格大小（如果超过，按比例缩放）
    if (widthEMU > cellWidthEMU) {
      double ratio = (double) cellWidthEMU / widthEMU;
      widthEMU = cellWidthEMU;
      heightEMU = (int) (heightEMU * ratio);
      logger.debug("图片宽度超过单元格，按比例缩放: 原始宽度={}, 缩放后宽度={}", width, widthEMU / 9525);
    }
    if (heightEMU > cellHeightEMU) {
      double ratio = (double) cellHeightEMU / heightEMU;
      heightEMU = cellHeightEMU;
      widthEMU = (int) (widthEMU * ratio);
      logger.debug("图片高度超过单元格，按比例缩放: 原始高度={}, 缩放后高度={}", height, heightEMU / 9525);
    }

    return new int[] {
      widthEMU, heightEMU
    };
  }

  /**
   * 将图片添加到单元格（没有transform的情况下）
   */
  private static void addImageToCellWithoutTransform(Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    int rowNum, int colNum, byte[] imageBytes, int pictureType, int actualWidth, int actualHeight) {
    try {
      int widthEMU = actualWidth * 9525;
      int heightEMU = actualHeight * 9525;

      int cellWidthEMU = 88 * 9525;
      int cellHeightEMU = 24 * 9525;

      // 限制图片尺寸不超过单元格大小（如果超过，按比例缩放）
      if (widthEMU > cellWidthEMU) {
        double ratio = (double) cellWidthEMU / widthEMU;
        widthEMU = cellWidthEMU;
        heightEMU = (int) (heightEMU * ratio);
      }
      if (heightEMU > cellHeightEMU) {
        double ratio = (double) cellHeightEMU / heightEMU;
        heightEMU = cellHeightEMU;
        widthEMU = (int) (widthEMU * ratio);
      }

      insertPictureWithAnchor(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, widthEMU, heightEMU);
      logger.debug("使用图片实际尺寸添加到单元格: width={}, height={}, col={}, row={}, 绑定在单个单元格内", actualWidth, actualHeight, colNum,
        rowNum);
    }
    catch (Exception e) {
      logger.warn("添加图片到Excel失败: {}", e.getMessage());
    }
  }

  /**
   * 插入图片到单元格（使用anchor）
   */
  private static void insertPictureToCell(Workbook workbook, CreationHelper helper, Drawing<?> drawing, int rowNum,
    int colNum, byte[] imageBytes, int pictureType, int widthEMU, int heightEMU, double widthPx) {
    try {
      insertPictureWithAnchor(workbook, helper, drawing, rowNum, colNum, imageBytes, pictureType, widthEMU, heightEMU);
      double heightPx = widthPx > 0 ? (heightEMU * widthPx / widthEMU) : 0;
      logger.debug("成功添加单元格图片: col={}, row={}, width={}px ({}EMU), height={}px ({}EMU), 绑定在单个单元格内", colNum, rowNum,
        widthPx, widthEMU, heightPx, heightEMU);
    }
    catch (Exception e) {
      logger.error("添加图片到Excel失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 使用anchor插入图片
   */
  private static void insertPictureWithAnchor(Workbook workbook, CreationHelper helper, Drawing<?> drawing, int rowNum,
    int colNum, byte[] imageBytes, int pictureType, int widthEMU, int heightEMU) {
    int pictureIdx = workbook.addPicture(imageBytes, pictureType);
    ClientAnchor anchor = helper.createClientAnchor();

    // 单元格图片：col1 == col2 && row1 == row2（严格绑定在单个单元格内）
    anchor.setCol1(colNum);
    anchor.setRow1(rowNum);
    anchor.setCol2(colNum); // 不跨越列
    anchor.setRow2(rowNum); // 不跨越行

    // 设置图片在单元格内的位置（dx1/dy1 是起始位置，dx2/dy2 是结束位置）
    anchor.setDx1(0); // 从单元格左边界开始
    anchor.setDy1(0); // 从单元格上边界开始
    anchor.setDx2(widthEMU); // 宽度
    anchor.setDy2(heightEMU); // 高度

    drawing.createPicture(anchor, pictureIdx);
  }

  /**
   * 处理浮动图片（resources.SHEET_DRAWING_PLUGIN）
   */
  private static void processFloatingImages(JsonNode json, Workbook workbook, Map<String, Sheet> sheetIdMap,
    DocumentAttachmentHelper documentAttachmentHelper) {
    logger.debug("processFloatingImages: 开始处理浮动图片");

    if (documentAttachmentHelper == null) {
      logger.warn("跳过浮动图片处理（未提供DocumentAttachmentHelper）");
      return;
    }

    JsonNode resources = json.get("resources");
    if (resources == null || !resources.isArray()) {
      logger.debug("processFloatingImages: JSON 中没有 resources 节点或不是数组");
      return;
    }

    int resourceCount = resources.size();
    logger.debug("processFloatingImages: 找到 {} 个资源", resourceCount);

    JsonNode drawingData = findDrawingPluginResource(resources);
    if (drawingData == null) {
      logger.debug("processFloatingImages: 未找到 SHEET_DRAWING_PLUGIN 资源");
      return;
    }

    processDrawingDataForSheets(drawingData, workbook, sheetIdMap, documentAttachmentHelper);
  }

  /**
   * 查找SHEET_DRAWING_PLUGIN资源
   */
  private static JsonNode findDrawingPluginResource(JsonNode resources) {
    for (JsonNode resource : resources) {
      String resourceName = resource.has("name") ? resource.get("name").asText(null) : null;
      logger.debug("processFloatingImages: 检查资源 name={}", resourceName);
      if (!RESOURCE_SHEET_DRAWING_PLUGIN.equals(resourceName)) {
        continue;
      }

      logger.debug("processFloatingImages: 找到 SHEET_DRAWING_PLUGIN 资源");

      JsonNode dataNode = resource.get("data");
      if (dataNode == null || dataNode.isNull() || dataNode.asText().isEmpty()) {
        continue;
      }

      try {
        String dataStr = dataNode.asText();
        return JsonUtil.getObjectMapper().readTree(dataStr);
      }
      catch (Exception e) {
        logger.error("解析浮动图片数据失败: {}", e.getMessage(), e);
        return null;
      }
    }
    return null;
  }

  /**
   * 处理所有sheet的图片数据
   */
  private static void processDrawingDataForSheets(JsonNode drawingData, Workbook workbook,
    Map<String, Sheet> sheetIdMap, DocumentAttachmentHelper documentAttachmentHelper) {
    try {
      Iterator<String> sheetIterator = drawingData.fieldNames();
      while (sheetIterator.hasNext()) {
        String sheetId = sheetIterator.next();
        Sheet excelSheet = sheetIdMap.get(sheetId);
        if (excelSheet == null) {
          logger.debug("找不到对应的Sheet: sheetId={}", sheetId);
          continue;
        }

        JsonNode sheetDrawings = drawingData.get(sheetId);
        if (sheetDrawings == null || sheetDrawings.isNull()) {
          continue;
        }

        JsonNode dataObj = sheetDrawings.get("data");
        if (dataObj == null || dataObj.isNull()) {
          logger.debug("processFloatingImages: sheetDrawings 中没有 data 节点, sheetId={}", sheetId);
          continue;
        }

        int floatingImageCount = dataObj.size();
        logger.debug("processFloatingImages: sheetId={} 找到 {} 个浮动图片", sheetId, floatingImageCount);

        Drawing<?> drawing = excelSheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();

        processFloatingImagesForSheet(dataObj, workbook, helper, drawing, excelSheet, documentAttachmentHelper);
      }
    }
    catch (Exception e) {
      logger.error("处理浮动图片失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理单个sheet的浮动图片
   */
  private static void processFloatingImagesForSheet(JsonNode dataObj, Workbook workbook, CreationHelper helper,
    Drawing<?> drawing, Sheet excelSheet, DocumentAttachmentHelper documentAttachmentHelper) {
    Iterator<String> drawingIterator = dataObj.fieldNames();
    while (drawingIterator.hasNext()) {
      String drawingId = drawingIterator.next();
      JsonNode drawingObj = dataObj.get(drawingId);
      logger.debug("processFloatingImages: 处理浮动图片, sheetId={}, drawingId={}", excelSheet.getSheetName(), drawingId);
      addFloatingImage(drawingObj, workbook, helper, drawing, documentAttachmentHelper);
    }
  }

  /**
   * 添加浮动图片到Sheet
   */
  private static void addFloatingImage(JsonNode drawingObj, Workbook workbook, CreationHelper helper,
    Drawing<?> drawing, DocumentAttachmentHelper documentAttachmentHelper) {
    String imageSource = extractImageSource(drawingObj);
    if (imageSource == null) {
      return;
    }

    if (documentAttachmentHelper == null) {
      logger.warn("无法加载浮动图片，DocumentAttachmentHelper为null: imageSource={}", imageSource);
      return;
    }

    ImageLoadResult loadResult = loadUrlImage(imageSource, documentAttachmentHelper);
    if (!Boolean.TRUE.equals(loadResult.getSuccess()) || loadResult.getImageBytes() == null
      || loadResult.getImageBytes().length == 0) {
      logger.warn("addFloatingImage: 图片数据为空，无法添加浮动图片, imageSource={}", imageSource);
      return;
    }

    Integer pictureType = loadResult.getPictureType();
    if (pictureType == null) {
      logger.warn("addFloatingImage: 图片类型为null, imageSource={}", imageSource);
      return;
    }

    logger.debug("addFloatingImage: 图片数据准备完成, imageBytes.length={} bytes, pictureType={}",
      loadResult.getImageBytes().length, pictureType);

    int[] position = extractFloatingImagePosition(drawingObj);
    if (position == null) {
      return;
    }

    insertFloatingPicture(workbook, helper, drawing, loadResult.getImageBytes(), pictureType,
      position[0], position[1], position[2], position[3]);
  }

  /**
   * 提取图片源URL
   */
  private static String extractImageSource(JsonNode drawingObj) {
    JsonNode sourceNode = drawingObj.get("source");
    if (sourceNode == null || sourceNode.isNull()) {
      logger.debug("浮动图片缺少source字段");
      return null;
    }

    String imageSource = sourceNode.asText();
    if (imageSource == null || imageSource.isEmpty()) {
      logger.debug("浮动图片source为空");
      return null;
    }
    return imageSource;
  }

  /**
   * 提取浮动图片位置信息
   */
  private static int[] extractFloatingImagePosition(JsonNode drawingObj) {
    JsonNode sheetTransform = drawingObj.get("sheetTransform");
    if (sheetTransform == null || sheetTransform.isNull()) {
      return null;
    }

    int[] startPos = extractStartPosition(sheetTransform);
    if (startPos == null) {
      return null;
    }

    int[] endPos = extractEndPosition(sheetTransform, drawingObj, startPos[0], startPos[1]);
    return new int[] {
      startPos[0], startPos[1], endPos[0], endPos[1]
    };
  }

  /**
   * 提取起始位置
   */
  private static int[] extractStartPosition(JsonNode sheetTransform) {
    JsonNode fromNode = sheetTransform.get("from");
    if (fromNode == null || fromNode.isNull()) {
      return null;
    }

    int startCol = fromNode.has("column") ? fromNode.get("column").asInt() : 0;
    int startRow = fromNode.has("row") ? fromNode.get("row").asInt() : 0;
    return new int[] {
      startCol, startRow
    };
  }

  /**
   * 提取结束位置
   */
  private static int[] extractEndPosition(JsonNode sheetTransform, JsonNode drawingObj, int startCol, int startRow) {
    JsonNode toNode = sheetTransform.get("to");
    if (toNode != null && !toNode.isNull()) {
      return getEndPositionFromToNode(toNode, startCol, startRow);
    }

    return calculateEndPositionFromTransform(drawingObj, startCol, startRow);
  }

  /**
   * 从toNode获取结束位置
   */
  private static int[] getEndPositionFromToNode(JsonNode toNode, int startCol, int startRow) {
    int endCol = toNode.has("column") ? toNode.get("column").asInt() : startCol;
    int endRow = toNode.has("row") ? toNode.get("row").asInt() : startRow;
    return new int[] {
      endCol, endRow
    };
  }

  /**
   * 从transform计算结束位置
   */
  private static int[] calculateEndPositionFromTransform(JsonNode drawingObj, int startCol, int startRow) {
    JsonNode transform = drawingObj.get("transform");
    if (transform != null && !transform.isNull()) {
      double width = transform.has("width") ? transform.get("width").asDouble() : 200;
      double height = transform.has("height") ? transform.get("height").asDouble() : 100;
      int endCol = startCol + (int) Math.ceil(width / 88.0);
      int endRow = startRow + (int) Math.ceil(height / 24.0);
      return new int[] {
        endCol, endRow
      };
    }
    return new int[] {
      startCol, startRow
    };
  }

  /**
   * 插入浮动图片
   */
  private static void insertFloatingPicture(Workbook workbook, CreationHelper helper, Drawing<?> drawing,
    byte[] imageBytes, int pictureType, int startCol, int startRow, int endCol, int endRow) {
    try {
      int pictureIdx = workbook.addPicture(imageBytes, pictureType);
      ClientAnchor anchor = helper.createClientAnchor();
      anchor.setCol1(startCol);
      anchor.setRow1(startRow);
      anchor.setCol2(Math.max(startCol + 1, endCol));
      anchor.setRow2(Math.max(startRow + 1, endRow));
      drawing.createPicture(anchor, pictureIdx);
    }
    catch (Exception e) {
      logger.warn("添加浮动图片到Excel失败: {}", e.getMessage());
    }
  }

  /**
   * 将Excel文件转换为LuckySheet JSON格式
   *
   * @param excelPath Excel文件路径
   * @return LuckySheet JSON格式字符串
   * @throws IOException 当文件读取失败时抛出
   */
  public static String convertExcelToLuckysheet(String excelPath, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) throws IOException {
    try (InputStream rawStream = new FileInputStream(excelPath)) {
      return convertExcelToLuckysheetInternal(rawStream, documentId, userId, documentAttachmentService);
    }
  }

  /**
   * 将Excel文件转换为LuckySheet JSON格式（支持图片上传）
   *
   * @param rawStream Excel文件输入流
   * @param documentId 文档ID，用于上传图片
   * @param userId 用户ID，用于上传图片
   * @param documentAttachmentService 文档附件服务，用于上传图片（可为null，如果为null则不处理图片）
   * @return LuckySheet JSON格式字符串
   * @throws IOException 当文件读取失败时抛出
   */
  public static String convertExcelToLuckysheet(InputStream rawStream, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) throws IOException {
    return convertExcelToLuckysheetInternal(rawStream, documentId, userId, documentAttachmentService);
  }

  /**
   * 将Excel文件转换为LuckySheet JSON格式的内部实现
   *
   * @param rawStream Excel文件输入流
   * @param documentId 文档ID，用于上传图片
   * @param userId 用户ID，用于上传图片
   * @param documentAttachmentService 文档附件服务，用于上传图片
   * @return LuckySheet JSON格式字符串
   * @throws IOException 当文件读取失败时抛出
   */
  private static String convertExcelToLuckysheetInternal(InputStream rawStream, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) throws IOException {
    ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    // 读取Excel文件
    try (InputStream checkedStream = FileMagic.prepareToCheckMagic(rawStream)) {
      FileMagic fileMagic = FileMagic.valueOf(checkedStream);
      if (fileMagic != FileMagic.OOXML && fileMagic != FileMagic.OLE2) {
        throw new IOException("仅支持上传标准Excel文件（.xls 或 .xlsx），当前文件类型无法识别");
      }

      try (Workbook workbook = WorkbookFactory.create(checkedStream)) {
        DataFormatter dataFormatter = new DataFormatter();

        // 构建根节点
        ObjectNode rootNode = DcExcelHelper.buildRootNode(objectMapper);

        // 构建sheets对象
        ObjectNode sheetsNode = objectMapper.createObjectNode();
        ArrayNode sheetOrderArray = objectMapper.createArrayNode();

        // 遍历所有sheet
        int sheetCount = workbook.getNumberOfSheets();
        Map<String, Sheet> sheetIdMap = new HashMap<>();
        for (int i = 0; i < sheetCount; i++) {
          Sheet sheet = workbook.getSheetAt(i);
          String sheetId = UUID.randomUUID().toString().replace("-", "").substring(0, SHEET_ID_LENGTH);
          sheetOrderArray.add(sheetId);
          sheetIdMap.put(sheetId, sheet);

          // 构建单个sheet对象
          ObjectNode sheetNode = buildSheetNode(objectMapper, sheet, sheetId, dataFormatter, workbook, documentId,
            userId, documentAttachmentService);
          sheetsNode.set(sheetId, sheetNode);
        }

        rootNode.set("sheetOrder", sheetOrderArray);
        rootNode.set("sheets", sheetsNode);

        // 获取工作簿 ID
        String workbookId = rootNode.get("id").asText();

        // 构建resources数组（包含浮动图片）
        ArrayNode resourcesArray = buildResourcesArray(objectMapper, workbook, sheetIdMap, documentId, userId,
          documentAttachmentService, workbookId);
        rootNode.set("resources", resourcesArray);

        return objectMapper.writeValueAsString(rootNode);
      }
    }
    catch (IllegalArgumentException e) {
      throw new IOException("无法识别的Excel文件格式，请确认上传的是有效的Excel文件", e);
    }
  }

  /**
   * 构建LuckySheet资源数组（包含浮动图片）
   */
  private static ArrayNode buildResourcesArray(ObjectMapper objectMapper, Workbook workbook,
    Map<String, Sheet> sheetIdMap, String documentId, Long userId, IDocumentAttachmentService documentAttachmentService,
    String workbookId) {
    ArrayNode resourcesArray = objectMapper.createArrayNode();
    for (String resourceName : LUCKYSHEET_RESOURCE_NAMES) {
      ObjectNode resourceNode = objectMapper.createObjectNode();
      resourceNode.put("name", resourceName);
      // SHEET_RANGE_PROTECTION_PLUGIN 和 SHEET_DEFINED_NAME_PLUGIN 使用空字符串
      if ("SHEET_RANGE_PROTECTION_PLUGIN".equals(resourceName) || "SHEET_DEFINED_NAME_PLUGIN".equals(resourceName)) {
        resourceNode.put("data", "");
      }
      else if (RESOURCE_SHEET_DRAWING_PLUGIN.equals(resourceName)) {
        // 处理浮动图片
        if (documentAttachmentService != null && workbookId != null) {
          ObjectNode drawingData;
          if (workbook instanceof XSSFWorkbook) {
            // 处理 .xlsx 文件的浮动图片
            drawingData = extractFloatingImagesFromXSSF(sheetIdMap, documentId, userId, documentAttachmentService,
              objectMapper, workbookId);
          }
          else if (workbook instanceof HSSFWorkbook) {
            // 处理 .xls 文件的浮动图片
            drawingData = extractFloatingImagesFromHSSF(sheetIdMap, documentId, userId, documentAttachmentService,
              objectMapper, workbookId);
          }
          else {
            drawingData = objectMapper.createObjectNode();
          }
          resourceNode.put("data", JsonUtil.toJsonString(drawingData));
        }
        else {
          resourceNode.put("data", "{}");
        }
      }
      else {
        resourceNode.put("data", "{}");
      }
      resourcesArray.add(resourceNode);
    }
    return resourcesArray;
  }

  /**
   * 构建单个sheet的JSON节点
   */
  private static ObjectNode buildSheetNode(ObjectMapper objectMapper, Sheet sheet, String sheetId,
    DataFormatter dataFormatter, Workbook workbook, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) {
    ObjectNode sheetNode = objectMapper.createObjectNode();
    sheetNode.put("id", sheetId);
    sheetNode.put("name", sheet.getSheetName());
    sheetNode.put("tabColor", "");
    sheetNode.put("hidden", 0);
    sheetNode.put("rowCount", 1000);
    sheetNode.put("columnCount", 20);
    sheetNode.put("zoomRatio", 1);

    // freeze对象
    ObjectNode freezeNode = objectMapper.createObjectNode();
    freezeNode.put("xSplit", 0);
    freezeNode.put("ySplit", 0);
    freezeNode.put("startRow", -1);
    freezeNode.put("startColumn", -1);
    sheetNode.set("freeze", freezeNode);

    sheetNode.put("scrollTop", 0);
    sheetNode.put("scrollLeft", 0);
    sheetNode.put("defaultColumnWidth", 88);
    sheetNode.put("defaultRowHeight", 24);
    sheetNode.putArray("mergeData");

    // 构建cellData
    ObjectNode cellDataNode = objectMapper.createObjectNode();

    // 提取单元格数据
    DcExcelHelper.extractCellData(sheet, cellDataNode, dataFormatter);

    // 提取并上传单元格图片
    if (documentAttachmentService != null) {
      if (workbook instanceof XSSFWorkbook) {
        // 处理 .xlsx 文件的单元格图片
        int cellImageCount = DcExcelHelper.countCellImagesFromXSSF((XSSFSheet) sheet);
        logger.debug("Sheet {} 开始处理图片（XSSF），共 {} 个", sheet.getSheetName(), cellImageCount);
        extractAndUploadCellImagesFromXSSF((XSSFSheet) sheet, cellDataNode, documentId, userId,
          documentAttachmentService, objectMapper);
      }
      else if (workbook instanceof HSSFWorkbook) {
        // 处理 .xls 文件的单元格图片
        int cellImageCount = DcExcelHelper.countCellImagesFromHSSF((HSSFSheet) sheet);
        logger.debug("Sheet {} 开始处理图片（HSSF），共 {} 个", sheet.getSheetName(), cellImageCount);
        extractAndUploadCellImagesFromHSSF((HSSFSheet) sheet, cellDataNode, documentId, userId,
          documentAttachmentService, objectMapper);
      }
    }

    // 清理 cellData 中所有包含 DISPIMG 但没有 p 节点的单元格（这些可能是浮动图片或处理失败的单元格图片）
    DcExcelHelper.cleanDispimgCells(cellDataNode);

    sheetNode.set("cellData", cellDataNode);
    sheetNode.putObject("rowData");
    sheetNode.putObject("columnData");
    sheetNode.put("showGridlines", 1);

    // rowHeader和columnHeader
    ObjectNode rowHeaderNode = objectMapper.createObjectNode();
    rowHeaderNode.put("width", 46);
    rowHeaderNode.put("hidden", 0);
    sheetNode.set("rowHeader", rowHeaderNode);

    ObjectNode columnHeaderNode = objectMapper.createObjectNode();
    columnHeaderNode.put("height", 20);
    columnHeaderNode.put("hidden", 0);
    sheetNode.set("columnHeader", columnHeaderNode);

    sheetNode.put("rightToLeft", 0);

    return sheetNode;
  }

  /**
   * 提取并上传单元格图片 - XSSF
   */
  private static void extractAndUploadCellImagesFromXSSF(XSSFSheet sheet, ObjectNode cellDataNode, String documentId,
    Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    XSSFDrawing drawing = sheet.getDrawingPatriarch();
    if (drawing == null) {
      return;
    }

    java.util.List<XSSFShape> shapes = drawing.getShapes();
    for (XSSFShape shape : shapes) {
      if (shape instanceof XSSFPicture) {
        XSSFPicture picture = (XSSFPicture) shape;
        try {
          processCellImageFromXSSF(picture, cellDataNode, documentId, userId, documentAttachmentService, objectMapper);
        }
        catch (Exception e) {
          logger.warn("提取单元格图片失败: {}", e.getMessage(), e);
        }
      }
    }

    int processedCount = countProcessedCellImages(cellDataNode);
    logger.debug("Sheet {} 处理了 {} 个单元格图片", sheet.getSheetName(), processedCount);
  }

  /**
   * 处理单个XSSF单元格图片
   */
  private static void processCellImageFromXSSF(XSSFPicture picture, ObjectNode cellDataNode, String documentId,
    Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    XSSFPictureData pictureData = picture.getPictureData();
    byte[] imageBytes = pictureData.getData();
    String extension = pictureData.suggestFileExtension();

    ClientAnchor anchor = picture.getClientAnchor();
    if (anchor == null) {
      logger.debug("图片 anchor 为 null，将在浮动图片处理中处理（可能是截图）");
      return;
    }

    if (DcExcelHelper.isFloatingPicture(anchor)) {
      logger.debug("跳过浮动图片，将在 extractFloatingImages 中处理: col1={}, col2={}, row1={}, row2={}", anchor.getCol1(),
        anchor.getCol2(), anchor.getRow1(), anchor.getRow2());
      return;
    }

    logger.debug("单元格图片处理: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}, dx2={}, dy2={}, isFloating=false",
      anchor.getCol1(), anchor.getCol2(), anchor.getRow1(), anchor.getRow2(), anchor.getDx1(), anchor.getDy1(),
      anchor.getDx2(), anchor.getDy2());

    int rowNum = anchor.getRow1();
    int colNum = anchor.getCol1();

    String imageUrl = DcExcelHelper.uploadImage(imageBytes, extension, documentId, userId, documentAttachmentService);
    if (imageUrl == null) {
      logger.warn("图片上传失败: row={}, col={}", rowNum, colNum);
      return;
    }

    int[] imageSize = DcExcelHelper.calculateImageSize(imageBytes, anchor);
    int width = imageSize[0];
    int height = imageSize[1];

    String drawingId = UUID.randomUUID().toString().replace("-", "").substring(0, 22);
    ObjectNode cellNode = DcExcelHelper.getOrCreateCellNode(cellDataNode, rowNum, colNum, objectMapper);
    ObjectNode pNode = createCellImagePNode(objectMapper, imageUrl, width, height, drawingId);
    cellNode.set("p", pNode);

    handleCellValueForImage(cellNode, rowNum, colNum);
    logger.debug("成功添加单元格图片到 cellData[{}][{}]", rowNum, colNum);
  }

  /**
   * 创建单元格图片的p节点
   */
  private static ObjectNode createCellImagePNode(ObjectMapper objectMapper, String imageUrl, int width, int height,
    String drawingId) {
    ObjectNode pNode = objectMapper.createObjectNode();
    pNode.put("id", "d");

    pNode.set("documentStyle", DcExcelHelper.createDocumentStyle(objectMapper));
    pNode.set("body", DcExcelHelper.createCellImageBody(objectMapper, drawingId));

    ObjectNode drawingsNode = objectMapper.createObjectNode();
    ObjectNode drawingObj = DcExcelHelper.createDrawingObject(objectMapper, imageUrl, width, height, drawingId);
    drawingsNode.set(drawingId, drawingObj);
    pNode.set("drawings", drawingsNode);
    pNode.putArray("drawingsOrder").add(drawingId);

    return pNode;
  }

  /**
   * 处理单元格的值（保留或删除DISIMG）
   */
  private static void handleCellValueForImage(ObjectNode cellNode, int rowNum, int colNum) {
    if (!cellNode.has("v")) {
      return;
    }

    String cellValue = cellNode.get("v").asText();
    if (cellValue == null || cellValue.trim().isEmpty()) {
      cellNode.remove("v");
    }
    else if (cellValue.trim().startsWith("_xlfn.DISPIMG") || cellValue.trim().startsWith("DISPIMG")) {
      logger.debug("保留单元格图片的 DISPIMG 文本值: row={}, col={}, value={}", rowNum, colNum, cellValue);
    }
  }

  /**
   * 统计已处理的单元格图片数量
   */
  private static int countProcessedCellImages(ObjectNode cellDataNode) {
    int processedCount = 0;
    for (Iterator<String> rowIt = cellDataNode.fieldNames(); rowIt.hasNext();) {
      ObjectNode rowNode = (ObjectNode) cellDataNode.get(rowIt.next());
      for (Iterator<String> colIt = rowNode.fieldNames(); colIt.hasNext();) {
        ObjectNode cellNode = (ObjectNode) rowNode.get(colIt.next());
        if (cellNode.has("p")) {
          processedCount++;
        }
      }
    }
    return processedCount;
  }

  /**
   * 提取浮动图片 - XSSF
   */
  private static ObjectNode extractFloatingImagesFromXSSF(Map<String, Sheet> sheetIdMap, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper, String workbookId) {
    ObjectNode drawingData = objectMapper.createObjectNode();

    for (Map.Entry<String, Sheet> entry : sheetIdMap.entrySet()) {
      String sheetId = entry.getKey();
      Sheet sheet = entry.getValue();

      if (!(sheet instanceof XSSFSheet)) {
        continue;
      }

      XSSFDrawing drawing = ((XSSFSheet) sheet).getDrawingPatriarch();
      if (drawing == null) {
        continue;
      }

      ObjectNode sheetDrawings = processFloatingImagesForXSSFSheet(drawing, sheetId, workbookId, documentId, userId,
        documentAttachmentService, objectMapper);
      if (sheetDrawings != null && sheetDrawings.has("data")) {
        drawingData.set(sheetId, sheetDrawings);
        logger.debug("Sheet {} 提取到 {} 个浮动图片", sheetId, ((ObjectNode) sheetDrawings.get("data")).size());
      }
      else {
        logger.debug("Sheet {} 没有浮动图片", sheetId);
      }
    }

    return drawingData;
  }

  /**
   * 处理单个XSSF sheet的浮动图片
   */
  private static ObjectNode processFloatingImagesForXSSFSheet(XSSFDrawing drawing, String sheetId, String workbookId,
    String documentId, Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    ObjectNode sheetDrawings = objectMapper.createObjectNode();
    ObjectNode dataObj = objectMapper.createObjectNode();
    ArrayNode orderArray = objectMapper.createArrayNode();

    java.util.List<XSSFShape> shapes = drawing.getShapes();
    for (XSSFShape shape : shapes) {
      if (shape instanceof XSSFPicture) {
        XSSFPicture picture = (XSSFPicture) shape;
        try {
          processFloatingImageFromXSSF(picture, sheetId, workbookId, documentId, userId, documentAttachmentService,
            objectMapper, dataObj, orderArray);
        }
        catch (Exception e) {
          logger.warn("提取浮动图片失败: {}", e.getMessage(), e);
        }
      }
    }

    if (dataObj.size() > 0) {
      sheetDrawings.set("data", dataObj);
      sheetDrawings.set("order", orderArray);
      return sheetDrawings;
    }

    return null;
  }

  /**
   * 处理单个XSSF浮动图片
   */
  private static void processFloatingImageFromXSSF(XSSFPicture picture, String sheetId, String workbookId,
    String documentId, Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper,
    ObjectNode dataObj, ArrayNode orderArray) {
    XSSFPictureData pictureData = picture.getPictureData();
    byte[] imageBytes = pictureData.getData();
    String extension = pictureData.suggestFileExtension();

    ClientAnchor anchor = picture.getClientAnchor();
    String imageUrl = DcExcelHelper.uploadImage(imageBytes, extension, documentId, userId, documentAttachmentService);
    if (imageUrl == null) {
      return;
    }

    String drawingId = UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    ObjectNode drawingObj;

    if (anchor != null) {
      boolean isFloating = DcExcelHelper.isFloatingPicture(anchor);
      logger.debug("浮动图片处理: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}, dx2={}, dy2={}, isFloating={}",
        anchor.getCol1(), anchor.getCol2(), anchor.getRow1(), anchor.getRow2(), anchor.getDx1(), anchor.getDy1(),
        anchor.getDx2(), anchor.getDy2(), isFloating);

      if (!isFloating) {
        return;
      }

      logger.debug("处理浮动图片: col1={}, col2={}, row1={}, row2={}", anchor.getCol1(), anchor.getCol2(), anchor.getRow1(),
        anchor.getRow2());

      int[] imageSize = DcExcelHelper.calculateImageSize(imageBytes, anchor);
      int width = imageSize[0];
      int height = imageSize[1];
      drawingObj = DcExcelHelper.createFloatingDrawingObject(objectMapper, imageUrl, anchor, sheetId, workbookId,
        drawingId, width, height);
    }
    else {
      logger.debug("处理 anchor 为 null 的图片（可能是截图），作为浮动图片处理");
      int[] imageSize = DcExcelHelper.calculateImageSizeFromBytes(imageBytes);
      int width = imageSize[0];
      int height = imageSize[1];
      drawingObj = DcExcelHelper.createFloatingDrawingObjectWithoutAnchor(objectMapper, imageUrl, sheetId, workbookId,
        drawingId, width, height);
      logger.debug("成功处理 anchor 为 null 的图片，作为浮动图片: drawingId={}", drawingId);
    }

    dataObj.set(drawingId, drawingObj);
    orderArray.add(drawingId);
  }

  /**
   * 提取并上传单元格图片 - HSSF
   */
  private static void extractAndUploadCellImagesFromHSSF(HSSFSheet sheet, ObjectNode cellDataNode, String documentId,
    Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    HSSFPatriarch drawing = sheet.getDrawingPatriarch();
    if (drawing == null) {
      return;
    }

    java.util.List<HSSFShape> shapes = drawing.getChildren();
    for (HSSFShape shape : shapes) {
      if (shape instanceof HSSFPicture) {
        HSSFPicture picture = (HSSFPicture) shape;
        try {
          processCellImageFromHSSF(picture, cellDataNode, documentId, userId, documentAttachmentService, objectMapper);
        }
        catch (Exception e) {
          logger.warn("提取单元格图片失败（HSSF）: {}", e.getMessage(), e);
        }
      }
    }

    int processedCount = countProcessedCellImages(cellDataNode);
    logger.debug("Sheet {} 处理了 {} 个单元格图片（HSSF）", sheet.getSheetName(), processedCount);
  }

  /**
   * 处理单个HSSF单元格图片
   */
  private static void processCellImageFromHSSF(HSSFPicture picture, ObjectNode cellDataNode, String documentId,
    Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    org.apache.poi.hssf.usermodel.HSSFPictureData pictureData = picture.getPictureData();
    byte[] imageBytes = pictureData.getData();
    String extension = pictureData.suggestFileExtension();

    HSSFClientAnchor anchor = (HSSFClientAnchor) picture.getAnchor();
    if (anchor == null) {
      logger.debug("图片 anchor 为 null（HSSF），将在浮动图片处理中处理（可能是截图）");
      return;
    }

    if (DcExcelHelper.isFloatingPicture(anchor)) {
      logger.debug("跳过浮动图片（HSSF），将在 extractFloatingImagesFromHSSF 中处理: col1={}, col2={}, row1={}, row2={}",
        anchor.getCol1(), anchor.getCol2(), anchor.getRow1(), anchor.getRow2());
      return;
    }

    logger.debug("单元格图片处理（HSSF）: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}, dx2={}, dy2={}, isFloating=false",
      anchor.getCol1(), anchor.getCol2(), anchor.getRow1(), anchor.getRow2(), anchor.getDx1(), anchor.getDy1(),
      anchor.getDx2(), anchor.getDy2());

    int rowNum = anchor.getRow1();
    int colNum = anchor.getCol1();

    String imageUrl = DcExcelHelper.uploadImage(imageBytes, extension, documentId, userId, documentAttachmentService);
    if (imageUrl == null) {
      logger.warn("图片上传失败（HSSF）: row={}, col={}", rowNum, colNum);
      return;
    }

    int[] imageSize = DcExcelHelper.calculateImageSize(imageBytes, anchor);
    int width = imageSize[0];
    int height = imageSize[1];

    String drawingId = UUID.randomUUID().toString().replace("-", "").substring(0, 22);
    ObjectNode cellNode = DcExcelHelper.getOrCreateCellNode(cellDataNode, rowNum, colNum, objectMapper);
    ObjectNode pNode = createCellImagePNode(objectMapper, imageUrl, width, height, drawingId);
    cellNode.set("p", pNode);

    handleCellValueForImage(cellNode, rowNum, colNum);
    logger.debug("成功添加单元格图片到 cellData[{}][{}]（HSSF）", rowNum, colNum);
  }

  /**
   * 提取浮动图片 - HSSF
   */
  private static ObjectNode extractFloatingImagesFromHSSF(Map<String, Sheet> sheetIdMap, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper, String workbookId) {
    ObjectNode drawingData = objectMapper.createObjectNode();

    for (Map.Entry<String, Sheet> entry : sheetIdMap.entrySet()) {
      String sheetId = entry.getKey();
      Sheet sheet = entry.getValue();

      if (!(sheet instanceof HSSFSheet)) {
        continue;
      }

      HSSFPatriarch drawing = ((HSSFSheet) sheet).getDrawingPatriarch();
      if (drawing == null) {
        continue;
      }

      ObjectNode sheetDrawings = processFloatingImagesForHSSFSheet(drawing, sheetId, workbookId, documentId, userId,
        documentAttachmentService, objectMapper);
      if (sheetDrawings != null && sheetDrawings.has("data")) {
        drawingData.set(sheetId, sheetDrawings);
        logger.debug("Sheet {} 提取到 {} 个浮动图片（HSSF）", sheetId, ((ObjectNode) sheetDrawings.get("data")).size());
      }
      else {
        logger.debug("Sheet {} 没有浮动图片（HSSF）", sheetId);
      }
    }

    return drawingData;
  }

  /**
   * 处理单个HSSF sheet的浮动图片
   */
  private static ObjectNode processFloatingImagesForHSSFSheet(HSSFPatriarch drawing, String sheetId, String workbookId,
    String documentId, Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper) {
    ObjectNode sheetDrawings = objectMapper.createObjectNode();
    ObjectNode dataObj = objectMapper.createObjectNode();
    ArrayNode orderArray = objectMapper.createArrayNode();

    java.util.List<HSSFShape> shapes = drawing.getChildren();
    for (HSSFShape shape : shapes) {
      if (shape instanceof HSSFPicture) {
        HSSFPicture picture = (HSSFPicture) shape;
        try {
          processFloatingImageFromHSSF(picture, sheetId, workbookId, documentId, userId, documentAttachmentService,
            objectMapper, dataObj, orderArray);
        }
        catch (Exception e) {
          logger.warn("提取浮动图片失败（HSSF）: {}", e.getMessage(), e);
        }
      }
    }

    if (dataObj.size() > 0) {
      sheetDrawings.set("data", dataObj);
      sheetDrawings.set("order", orderArray);
      return sheetDrawings;
    }

    return null;
  }

  /**
   * 处理单个HSSF浮动图片
   */
  private static void processFloatingImageFromHSSF(HSSFPicture picture, String sheetId, String workbookId,
    String documentId, Long userId, IDocumentAttachmentService documentAttachmentService, ObjectMapper objectMapper,
    ObjectNode dataObj, ArrayNode orderArray) {
    org.apache.poi.hssf.usermodel.HSSFPictureData pictureData = picture.getPictureData();
    byte[] imageBytes = pictureData.getData();
    String extension = pictureData.suggestFileExtension();

    HSSFClientAnchor anchor = (HSSFClientAnchor) picture.getAnchor();
    String imageUrl = DcExcelHelper.uploadImage(imageBytes, extension, documentId, userId, documentAttachmentService);
    if (imageUrl == null) {
      return;
    }

    String drawingId = UUID.randomUUID().toString().replace("-", "").substring(0, 7);
    ObjectNode drawingObj;

    if (anchor != null) {
      boolean isFloating = DcExcelHelper.isFloatingPicture(anchor);
      logger.debug("浮动图片处理（HSSF）: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}, dx2={}, dy2={}, isFloating={}",
        anchor.getCol1(), anchor.getCol2(), anchor.getRow1(), anchor.getRow2(), anchor.getDx1(), anchor.getDy1(),
        anchor.getDx2(), anchor.getDy2(), isFloating);

      if (!isFloating) {
        return;
      }

      logger.debug("处理浮动图片（HSSF）: col1={}, col2={}, row1={}, row2={}", anchor.getCol1(), anchor.getCol2(),
        anchor.getRow1(), anchor.getRow2());

      int[] imageSize = DcExcelHelper.calculateImageSize(imageBytes, anchor);
      int width = imageSize[0];
      int height = imageSize[1];
      drawingObj = DcExcelHelper.createFloatingDrawingObject(objectMapper, imageUrl, anchor, sheetId, workbookId,
        drawingId, width, height);
    }
    else {
      logger.debug("处理 anchor 为 null 的图片（HSSF，可能是截图），作为浮动图片处理");
      int[] imageSize = DcExcelHelper.calculateImageSizeFromBytes(imageBytes);
      int width = imageSize[0];
      int height = imageSize[1];
      drawingObj = DcExcelHelper.createFloatingDrawingObjectWithoutAnchor(objectMapper, imageUrl, sheetId, workbookId,
        drawingId, width, height);
      logger.debug("成功处理 anchor 为 null 的图片，作为浮动图片（HSSF）: drawingId={}", drawingId);
    }

    dataObj.set(drawingId, drawingObj);
    orderArray.add(drawingId);
  }

}
