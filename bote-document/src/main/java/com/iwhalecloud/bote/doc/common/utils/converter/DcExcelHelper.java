package com.iwhalecloud.bote.doc.common.utils.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iwhalecloud.bote.doc.module.document.dto.AttachmentUrlInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Files;

@SuppressWarnings("PMD.GuardLogStatement")
public final class DcExcelHelper {
  private static final Logger logger = LoggerFactory.getLogger(DcExcelHelper.class);

  /**
   * 私有构造函数，防止实例化工具类
   */
  private DcExcelHelper() {
    // 工具类，禁止实例化
  }
  // 文档附件URL路径正则表达式，匹配格式：/api/bote/dc/document/attachment/files/{documentId}/{fileName} 或
  // /bote/dc/document/attachment/files/{documentId}/{fileName}
  private static final Pattern ATTACHMENT_URL_PATTERN = Pattern
    .compile(".*/(api/)?bote/dc/document/attachment/files/([^/]+)/([^/?#]+).*");
  private static final String LUCKYSHEET_APP_VERSION = "0.10.5";

  private static final String LUCKYSHEET_LOCALE = "zhCN";

  private static final int WORKBOOK_ID_LENGTH = 6;

  /**
   * 从图片字节数组计算图片尺寸（没有 anchor 的情况下，如截图）
   */
  public static int[] calculateImageSizeFromBytes(byte[] imageBytes) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
      // 尝试从图片字节数组读取实际尺寸
      BufferedImage image = ImageIO.read(bais);
      if (image != null) {
        return new int[] {
          image.getWidth(), image.getHeight()
        };
      }
    }
    catch (Exception e) {
      logger.debug("无法从字节数组读取图片尺寸: {}", e.getMessage());
    }

    // 如果无法读取，使用默认尺寸
    logger.warn("无法从字节数组读取图片尺寸，使用默认尺寸 200x200");
    return new int[] {
      200, 200
    };
  }

  /**
   * 根据文件扩展名获取 MIME 类型
   */
  public static String getImageMimeType(String extension) {
    if (StringUtils.isBlank(extension)) {
      return "image/png";
    }
    String lowerExt = extension.toLowerCase();
    if ("png".equals(lowerExt)) {
      return "image/png";
    }
    else if ("jpg".equals(lowerExt) || "jpeg".equals(lowerExt)) {
      return "image/jpeg";
    }
    else if ("gif".equals(lowerExt)) {
      return "image/gif";
    }
    else if ("bmp".equals(lowerExt)) {
      return "image/bmp";
    }
    else if ("webp".equals(lowerExt)) {
      return "image/webp";
    }
    return "image/png"; // 默认 PNG
  }
  /**
   * 创建图片文件名
   */
  public static String createPictureFileName(String suggestedName, String extension) {
    String baseName = StringUtils.isNotBlank(suggestedName) ? suggestedName : "excel_image_" + System.nanoTime();
    if (StringUtils.isNotBlank(extension)) {
      if (!baseName.toLowerCase().endsWith("." + extension.toLowerCase())) {
        return baseName + "." + extension;
      }
      return baseName;
    }
    return baseName + ".png"; // 默认扩展名
  }
  /**
   * 创建浮动图片对象（没有 anchor 的情况下，如截图）
   */
  public static ObjectNode createFloatingDrawingObjectWithoutAnchor(ObjectMapper objectMapper, String imageUrl,
    String sheetId, String workbookId, String drawingId, int width, int height) {
    ObjectNode drawingObj = objectMapper.createObjectNode();
    drawingObj.put("drawingId", drawingId);
    drawingObj.put("drawingType", 0);
    drawingObj.put("imageSourceType", "URL");
    drawingObj.put("source", imageUrl);
    drawingObj.put("unitId", workbookId);
    drawingObj.put("subUnitId", sheetId);

    // 顶层字段（单元格位置和尺寸，默认位置为 0,0）
    drawingObj.put("column", 0);
    drawingObj.put("columnOffset", 0);
    drawingObj.put("row", 0);
    drawingObj.put("rowOffset", 0);
    drawingObj.put("width", width);
    drawingObj.put("height", height);

    // sheetTransform 对象（单元格位置，默认位置为 0,0）
    ObjectNode sheetTransform = objectMapper.createObjectNode();
    ObjectNode from = objectMapper.createObjectNode();
    from.put("column", 0);
    from.put("columnOffset", 0);
    from.put("row", 0);
    from.put("rowOffset", 0);
    sheetTransform.set("from", from);

    ObjectNode to = objectMapper.createObjectNode();
    // 根据图片尺寸估算结束位置（假设单元格宽度88，高度24）
    int endCol = Math.max(1, (int) Math.ceil(width / 88.0));
    int endRow = Math.max(1, (int) Math.ceil(height / 24.0));
    to.put("column", endCol);
    to.put("columnOffset", width % 88);
    to.put("row", endRow);
    to.put("rowOffset", height % 24);
    sheetTransform.set("to", to);

    sheetTransform.put("flipY", false);
    sheetTransform.put("flipX", false);
    sheetTransform.put("angle", 0);
    sheetTransform.put("skewX", 0);
    sheetTransform.put("skewY", 0);
    drawingObj.set("sheetTransform", sheetTransform);

    // transform 对象（像素位置和大小，默认位置为 0,0）
    ObjectNode transform = objectMapper.createObjectNode();
    transform.put("left", 0);
    transform.put("top", 0);
    transform.put("width", width);
    transform.put("height", height);
    transform.put("flipY", false);
    transform.put("flipX", false);
    transform.put("angle", 0);
    transform.put("skewX", 0);
    transform.put("skewY", 0);
    drawingObj.set("transform", transform);

    drawingObj.put("behindDoc", 0);
    drawingObj.put("title", "");
    drawingObj.put("description", "");
    drawingObj.put("layoutType", 0);
    drawingObj.put("wrapText", 0);
    drawingObj.put("distB", 0);
    drawingObj.put("distL", 0);
    drawingObj.put("distR", 0);
    drawingObj.put("distT", 0);

    return drawingObj;
  }
  /**
   * 统计图片数量（用于调试）- HSSF
   */
  public static int countCellImagesFromHSSF(HSSFSheet sheet) {
    HSSFPatriarch drawing = sheet.getDrawingPatriarch();
    if (drawing == null) {
      return 0;
    }
    int count = 0;
    for (HSSFShape shape : drawing.getChildren()) {
      if (shape instanceof HSSFPicture) {
        count++;
      }
    }
    return count;
  }

  /**
   * 从输入流读取字节数组
   * <p>注意：此方法会完全读取输入流，但不会关闭输入流。调用方负责关闭输入流。</p>
   *
   * @param inputStream 输入流（调用方负责关闭）
   * @return 字节数组
   * @throws IOException 如果读取失败
   */
  public static byte[] readInputStream(InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return outputStream.toByteArray();
    }
  }
  /**
   * 检查单元格是否有图片
   */
  public static boolean hasCellImage(JsonNode cellObj) {
    if (cellObj == null || cellObj.isNull()) {
      return false;
    }
    JsonNode pObj = cellObj.get("p");
    if (pObj == null || pObj.isNull()) {
      return false;
    }
    JsonNode drawings = pObj.get("drawings");
    return drawings != null && !drawings.isNull() && drawings.size() > 0;
  }
  /**
   * 判断是否是附件URL
   */
  public static boolean isAttachmentUrl(String url) {
    if (url == null || url.isEmpty()) {
      logger.debug("isAttachmentUrl: url 为 null 或空");
      return false;
    }
    boolean matches = ATTACHMENT_URL_PATTERN.matcher(url).matches();
    logger.debug("isAttachmentUrl: url={}, matches={}", url, matches);
    return matches;
  }

  /**
   * 解析附件URL，提取 documentId 和 fileName
   */
  public static AttachmentUrlInfoDTO parseAttachmentUrl(String url) {
    if (url == null || url.isEmpty()) {
      logger.debug("parseAttachmentUrl: url 为 null 或空");
      return null;
    }
    try {
      Matcher matcher = ATTACHMENT_URL_PATTERN.matcher(url);
      if (matcher.matches()) {
        String documentId = matcher.group(2);
        String fileName = matcher.group(3);
        logger.debug("parseAttachmentUrl: 解析成功, url={}, documentId={}, fileName={}", url, documentId, fileName);
        return new AttachmentUrlInfoDTO(documentId, fileName);
      }
      else {
        logger.debug("parseAttachmentUrl: URL不匹配模式, url={}", url);
      }
    }
    catch (Exception e) {
      logger.warn("解析附件URL失败: url={}, error={}", url, e.getMessage(), e);
    }
    return null;
  }

  /**
   * 清理 cellData 中所有包含 DISPIMG 但没有 p 节点的单元格 这些单元格可能是浮动图片或处理失败的单元格图片
   */
  public static void cleanDispimgCells(ObjectNode cellDataNode) {
    Iterator<String> rowIterator = cellDataNode.fieldNames();
    List<String> rowsToRemove = new ArrayList<>();

    while (rowIterator.hasNext()) {
      String rowKey = rowIterator.next();
      ObjectNode rowNode = (ObjectNode) cellDataNode.get(rowKey);
      if (rowNode == null) {
        continue;
      }

      List<String> colsToRemove = processDispimgCellsInRow(rowNode, rowKey);
      removeMarkedColumns(rowNode, colsToRemove);

      if (rowNode.size() == 0) {
        rowsToRemove.add(rowKey);
      }
    }

    removeEmptyRows(cellDataNode, rowsToRemove);
  }

  /**
   * 处理行中所有包含DISPIMG的单元格
   */
  private static List<String> processDispimgCellsInRow(ObjectNode rowNode, String rowKey) {
    List<String> colsToRemove = new ArrayList<>();
    Iterator<String> colIterator = rowNode.fieldNames();

    while (colIterator.hasNext()) {
      String colKey = colIterator.next();
      ObjectNode cellNode = (ObjectNode) rowNode.get(colKey);
      if (cellNode == null) {
        continue;
      }

      String action = processDispimgCell(cellNode, rowKey, colKey);
      if ("remove".equals(action)) {
        colsToRemove.add(colKey);
      }
    }

    return colsToRemove;
  }

  /**
   * 处理单个包含DISPIMG的单元格
   * @return "keep" - 保留单元格和v字段, "remove" - 删除单元格, "removeV" - 删除v字段但保留单元格
   */
  private static String processDispimgCell(ObjectNode cellNode, String rowKey, String colKey) {
    if (!cellNode.has("v")) {
      return "keep";
    }

    String cellValue = cellNode.get("v").asText();
    if (cellValue == null || (!cellValue.trim().startsWith("_xlfn.DISPIMG") && !cellValue.trim().startsWith("DISPIMG"))) {
      return "keep";
    }

    if (cellNode.has("p")) {
      logger.debug("保留单元格图片的 DISPIMG 文本值: row={}, col={}, value={}", rowKey, colKey, cellValue);
      return "keep";
    }

    if (hasOtherFields(cellNode)) {
      logger.debug("删除单元格的 DISPIMG 文本值（保留样式信息）: row={}, col={}, value={}", rowKey, colKey, cellValue);
      cellNode.remove("v");
      return "removeV";
    }

    logger.debug("清理包含 DISPIMG 但没有 p 节点和其他信息的单元格: row={}, col={}, value={}", rowKey, colKey, cellValue);
    return "remove";
  }

  /**
   * 检查单元格是否有除v字段外的其他字段
   */
  private static boolean hasOtherFields(ObjectNode cellNode) {
    Iterator<String> fieldIterator = cellNode.fieldNames();
    while (fieldIterator.hasNext()) {
      String fieldName = fieldIterator.next();
      if (!"v".equals(fieldName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 删除标记的列
   */
  private static void removeMarkedColumns(ObjectNode rowNode, List<String> colsToRemove) {
    for (String colKey : colsToRemove) {
      rowNode.remove(colKey);
    }
  }

  /**
   * 删除空行
   */
  private static void removeEmptyRows(ObjectNode cellDataNode, List<String> rowsToRemove) {
    for (String rowKey : rowsToRemove) {
      cellDataNode.remove(rowKey);
    }
  }

  /**
   * 判断是否为浮动图片（不是严格绑定到单元格的图片） 单元格图片的特征： - 绑定在特定单元格上（col1 == col2 且 row1 == row2，或虽有跨越但绑定在起始单元格） - dx1/dy1 偏移较小（通常为 0
   * 或很小的值，表示在单元格内对齐） - 图片位置相对于单元格边界对齐 浮动图片的特征： - 跨越多个单元格（col1 != col2 或 row1 != row2） - 且有显著的偏移（dx1/dy1 不为
   * 0，表示不在单元格边界上） - 图片位置不完全绑定单元格边界 判断逻辑： - 如果图片跨越多个单元格（col1 != col2 或 row1 != row2），可能是浮动图片 - 但如果 dx1/dy1 都很小（接近
   * 0），说明图片还是对齐在单元格边界上，应该作为单元格图片 - 只有当图片跨越多个单元格且有不小的偏移时，才判断为浮动图片 注意：这是一个启发式判断，可能无法完全准确，但能覆盖大部分情况
   */
  public static boolean isFloatingPicture(ClientAnchor anchor) {
    if (anchor == null) {
      return false;
    }

    int col1 = anchor.getCol1();
    int col2 = anchor.getCol2();
    int row1 = anchor.getRow1();
    int row2 = anchor.getRow2();
    int dx1 = anchor.getDx1();
    int dy1 = anchor.getDy1();

    // 如果图片严格在单个单元格内（col1 == col2 && row1 == row2），通常是单元格图片
    if (col1 == col2 && row1 == row2) {
      // 但是如果 dx1/dy1 有显著偏移，仍然可能是浮动图片（图片在单元格内但位置不固定）
      // 单元格内的小幅偏移（< 10% 单元格尺寸）认为是单元格图片
      int dxThreshold = 84000; // 约 1/10 单元格宽度
      int dyThreshold = 25200; // 约 1/10 单元格高度

      if (Math.abs(dx1) > dxThreshold || Math.abs(dy1) > dyThreshold) {
        logger.debug("判断为浮动图片（单单元格但有显著偏移）: col={}, row={}, dx1={}, dy1={}", col1, row1, dx1, dy1);
        return true;
      }

      logger.debug("判断为单元格图片（单单元格）: col={}, row={}", col1, row1);
      return false;
    }

    // 如果图片跨越多个单元格
    boolean spansMultipleCells = (col1 != col2) || (row1 != row2);
    if (spansMultipleCells) {
      // Excel 中，dx1/dy1 的单位是 EMU (English Metric Units)，1 EMU = 1/914400 inch
      // 一个默认单元格宽度约 88 像素，假设 96 DPI，约 0.92 inch = 840000 EMU
      // 单元格内的小幅偏移可能是对齐导致的，不应该判断为浮动图片
      // 调整阈值：只有当偏移超过单元格的 1/5 时，才判断为浮动图片
      int dxThreshold = 168000; // 约 1/5 单元格宽度的偏移（原来 1/10 太敏感）
      int dyThreshold = 50400; // 约 1/5 单元格高度的偏移（原来 1/10 太敏感）

      // 如果跨越多个单元格，且有显著偏移，判断为浮动图片
      if (Math.abs(dx1) > dxThreshold || Math.abs(dy1) > dyThreshold) {
        logger.debug("判断为浮动图片（跨越多个单元格且有显著偏移）: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}", col1, col2, row1,
          row2, dx1, dy1);
        return true;
      }
      else {
        // 跨越多个单元格但偏移很小，可能是单元格图片（大图片占多个单元格）
        logger.debug("判断为单元格图片（跨越多个单元格但偏移小）: col1={}, col2={}, row1={}, row2={}, dx1={}, dy1={}", col1, col2, row1,
          row2, dx1, dy1);
        return false;
      }
    }

    // 理论上不会到这里，但为了安全返回 false
    logger.debug("判断为单元格图片（默认）: col1={}, row1={}, col2={}, row2={}", col1, row1, col2, row2);
    return false;
  }

  /**
   * 计算图片尺寸（优先使用图片实际尺寸，否则从 anchor 计算）
   */
  public static int[] calculateImageSize(byte[] imageBytes, ClientAnchor anchor) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
      // 尝试从图片字节数组读取实际尺寸
      BufferedImage image = ImageIO.read(bais);
      if (image != null) {
        return new int[] {
          image.getWidth(), image.getHeight()
        };
      }
    }
    catch (Exception e) {
      logger.debug("无法读取图片尺寸，将从 anchor 计算: {}", e.getMessage());
    }

    // 从 anchor 计算尺寸（EMU 转像素）
    // 1 EMU = 1/914400 英寸，96 DPI 下 1 像素 = 914400/96 = 9525 EMU
    int width = (anchor.getDx2() - anchor.getDx1()) / 9525;
    int height = (anchor.getDy2() - anchor.getDy1()) / 9525;

    // 如果计算出的尺寸太小或无效，使用单元格估算
    if (width <= 0 || height <= 0) {
      width = Math.max((anchor.getCol2() - anchor.getCol1()) * 88, 88);
      height = Math.max((anchor.getRow2() - anchor.getRow1()) * 24, 24);
    }

    return new int[] {
      width, height
    };
  }

  /**
   * 创建单元格图片对象
   */
  public static ObjectNode createDrawingObject(ObjectMapper objectMapper, String imageUrl, int width, int height,
    String drawingId) {
    ObjectNode drawingObj = objectMapper.createObjectNode();
    drawingObj.put("unitId", "d");
    drawingObj.put("subUnitId", "d");
    drawingObj.put("drawingId", drawingId);
    drawingObj.put("drawingType", 0);
    drawingObj.put("imageSourceType", "URL");
    drawingObj.put("source", imageUrl);

    // transform 对象（相对位置和大小）
    ObjectNode transform = objectMapper.createObjectNode();
    transform.put("left", 0);
    transform.put("top", 0);
    transform.put("width", width);
    transform.put("height", height);
    transform.put("flipY", false);
    transform.put("flipX", false);
    transform.put("angle", 0);
    transform.put("skewX", 0);
    transform.put("skewY", 0);
    drawingObj.set("transform", transform);

    // docTransform 对象（文档样式转换）
    ObjectNode docTransform = objectMapper.createObjectNode();
    ObjectNode size = objectMapper.createObjectNode();
    size.put("width", width);
    size.put("height", height);
    docTransform.set("size", size);

    ObjectNode positionH = objectMapper.createObjectNode();
    positionH.put("relativeFrom", 0);
    positionH.put("posOffset", 0);
    docTransform.set("positionH", positionH);

    ObjectNode positionV = objectMapper.createObjectNode();
    positionV.put("relativeFrom", 1);
    positionV.put("posOffset", 0);
    docTransform.set("positionV", positionV);

    docTransform.put("angle", 0);
    drawingObj.set("docTransform", docTransform);

    drawingObj.put("behindDoc", 0);
    drawingObj.put("title", "");
    drawingObj.put("description", "");
    drawingObj.put("layoutType", 0);
    drawingObj.put("wrapText", 0);
    drawingObj.put("distB", 0);
    drawingObj.put("distL", 0);
    drawingObj.put("distR", 0);
    drawingObj.put("distT", 0);

    return drawingObj;
  }

  /**
   * 创建浮动图片对象
   */
  public static ObjectNode createFloatingDrawingObject(ObjectMapper objectMapper, String imageUrl, ClientAnchor anchor,
    String sheetId, String workbookId, String drawingId, int width, int height) {
    ObjectNode drawingObj = objectMapper.createObjectNode();
    drawingObj.put("drawingId", drawingId);
    drawingObj.put("drawingType", 0);
    drawingObj.put("imageSourceType", "URL");
    drawingObj.put("source", imageUrl);
    drawingObj.put("unitId", workbookId);
    drawingObj.put("subUnitId", sheetId);

    // 顶层字段（单元格位置和尺寸）
    drawingObj.put("column", anchor.getCol1());
    drawingObj.put("columnOffset", 0);
    drawingObj.put("row", anchor.getRow1());
    drawingObj.put("rowOffset", 0);
    drawingObj.put("width", width);
    drawingObj.put("height", height);

    // sheetTransform 对象（单元格位置）
    ObjectNode sheetTransform = objectMapper.createObjectNode();
    ObjectNode from = objectMapper.createObjectNode();
    from.put("column", anchor.getCol1());
    from.put("columnOffset", 0);
    from.put("row", anchor.getRow1());
    from.put("rowOffset", 0);
    sheetTransform.set("from", from);

    ObjectNode to = objectMapper.createObjectNode();
    to.put("column", anchor.getCol2());
    to.put("columnOffset", 48); // 估算偏移量
    to.put("row", anchor.getRow2());
    to.put("rowOffset", 14); // 估算偏移量
    sheetTransform.set("to", to);

    sheetTransform.put("flipY", false);
    sheetTransform.put("flipX", false);
    sheetTransform.put("angle", 0);
    sheetTransform.put("skewX", 0);
    sheetTransform.put("skewY", 0);
    drawingObj.set("sheetTransform", sheetTransform);

    // transform 对象（像素位置和大小）
    ObjectNode transform = objectMapper.createObjectNode();
    // 将单元格位置转换为像素
    int left = anchor.getCol1() * 88 + (anchor.getDx1() / 9525); // 1 EMU = 1/9525 厘米
    int top = anchor.getRow1() * 24 + (anchor.getDy1() / 9525);

    transform.put("left", left);
    transform.put("top", top);
    transform.put("width", width);
    transform.put("height", height);
    transform.put("flipY", false);
    transform.put("flipX", false);
    transform.put("angle", 0);
    transform.put("skewX", 0);
    transform.put("skewY", 0);
    drawingObj.set("transform", transform);

    drawingObj.put("behindDoc", 0);
    drawingObj.put("title", "");
    drawingObj.put("description", "");
    drawingObj.put("layoutType", 0);
    drawingObj.put("wrapText", 0);
    drawingObj.put("distB", 0);
    drawingObj.put("distL", 0);
    drawingObj.put("distR", 0);
    drawingObj.put("distT", 0);

    return drawingObj;
  }

  /**
   * 上传图片并返回URL
   */
  public static String uploadImage(byte[] imageBytes, String extension, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) {
    if (!validateUploadParams(imageBytes, documentId, userId, documentAttachmentService)) {
      return null;
    }

    File tempFile = null;
    try {
      ImageUploadResult result = performImageUpload(imageBytes, extension, documentId, userId,
        documentAttachmentService);
      tempFile = result.getTempFile();
      return result.getImageUrl();
    }
    catch (Exception e) {
      logger.warn("上传Excel图片失败: documentId={}, error={}", documentId, e.getMessage(), e);
    }
    finally {
      cleanupTempFile(tempFile);
    }
    return null;
  }

  /**
   * 验证上传参数
   */
  private static boolean validateUploadParams(byte[] imageBytes, String documentId, Long userId,
    IDocumentAttachmentService documentAttachmentService) {
    if (imageBytes == null || imageBytes.length == 0) {
      return false;
    }
    if (documentAttachmentService == null || userId == null || StringUtils.isBlank(documentId)) {
      logger.debug("图片上传参数不完整，跳过上传: documentId={}, userId={}", documentId, userId);
      return false;
    }
    return true;
  }


  /**
   * 执行图片上传
   */
  private static ImageUploadResult performImageUpload(byte[] imageBytes, String extension, String documentId,
    Long userId, IDocumentAttachmentService documentAttachmentService) throws IOException {
    String fileName = createPictureFileName(null, extension);
    File tempFile = UploadingPicturesManager.createFileFromBytes(imageBytes, fileName);

    DocumentAttachmentDTO attachmentDTO = documentAttachmentService.upload(tempFile, documentId, userId);
    if (attachmentDTO != null && StringUtils.isNotBlank(attachmentDTO.getUrl())) {
      String imageUrl = "/api" + attachmentDTO.getUrl();
      logger.debug("图片上传成功: documentId={}, fileName={}, url={}", documentId, fileName, imageUrl);
      return new ImageUploadResult(imageUrl, tempFile);
    }
    return new ImageUploadResult(null, tempFile);
  }

  /**
   * 清理临时文件
   */
  private static void cleanupTempFile(File tempFile) {
    if (tempFile != null) {
      try {
        Files.deleteIfExists(tempFile.toPath());
      }
      catch (IOException e) {
        logger.debug("删除临时文件失败: {}", tempFile.getAbsolutePath(), e);
      }
    }
  }
  /**
   * 统计图片数量（用于调试）- XSSF
   */
  public static int countCellImagesFromXSSF(XSSFSheet sheet) {
    XSSFDrawing drawing = sheet.getDrawingPatriarch();
    if (drawing == null) {
      return 0;
    }
    int count = 0;
    for (XSSFShape shape : drawing.getShapes()) {
      if (shape instanceof XSSFPicture) {
        count++;
      }
    }
    return count;
  }

  /**
   * 构建LuckySheet根节点
   */
  public static ObjectNode buildRootNode(ObjectMapper objectMapper) {
    ObjectNode rootNode = objectMapper.createObjectNode();
    String workbookId = UUID.randomUUID().toString().replace("-", "").substring(0, WORKBOOK_ID_LENGTH);
    rootNode.put("id", workbookId);
    rootNode.put("name", "");
    rootNode.put("appVersion", LUCKYSHEET_APP_VERSION);
    rootNode.put("locale", LUCKYSHEET_LOCALE);
    rootNode.putObject("styles");
    return rootNode;
  }

  /**
   * 提取单元格数据
   */
  public static void extractCellData(Sheet sheet, ObjectNode cellDataNode, DataFormatter dataFormatter) {
    ObjectMapper objectMapper = JsonUtil.getObjectMapper();
    int lastRowNum = sheet.getLastRowNum();

    for (int rowNum = 0; rowNum <= lastRowNum; rowNum++) {
      Row row = sheet.getRow(rowNum);
      if (row == null) {
        continue;
      }

      processRowForCellData(row, rowNum, cellDataNode, dataFormatter, objectMapper);
    }
  }

  /**
   * 处理单行数据提取
   */
  private static void processRowForCellData(Row row, int rowNum, ObjectNode cellDataNode, DataFormatter dataFormatter,
    ObjectMapper objectMapper) {
    int lastCellNum = row.getLastCellNum();
    if (lastCellNum < 0) {
      return;
    }

    ObjectNode rowNode = null;

    for (int colNum = 0; colNum < lastCellNum; colNum++) {
      Cell cell = row.getCell(colNum);
      if (cell == null) {
        continue;
      }

      String cellValue = dataFormatter.formatCellValue(cell);
      if (cellValue == null || cellValue.trim().isEmpty()) {
        continue;
      }

      if (rowNode == null) {
        rowNode = objectMapper.createObjectNode();
        cellDataNode.set(String.valueOf(rowNum), rowNode);
      }

      createCellNode(rowNode, colNum, cellValue, rowNum, objectMapper);
    }
  }

  /**
   * 创建单元格节点
   */
  private static void createCellNode(ObjectNode rowNode, int colNum, String cellValue, int rowNum,
    ObjectMapper objectMapper) {
    ObjectNode cellNode = objectMapper.createObjectNode();
    cellNode.put("v", cellValue);
    rowNode.set(String.valueOf(colNum), cellNode);

    if (isDispimgFunction(cellValue)) {
      logger.debug("extractCellData: 单元格包含 DISPIMG 函数，将在图片处理逻辑中处理: row={}, col={}, value={}", rowNum, colNum,
        cellValue);
    }
  }

  /**
   * 判断是否为DISPIMG函数
   */
  private static boolean isDispimgFunction(String cellValue) {
    if (cellValue == null) {
      return false;
    }
    String trimmed = cellValue.trim();
    return trimmed.startsWith("_xlfn.DISPIMG") || trimmed.startsWith("DISPIMG");
  }

  /**
   * 加载Base64格式图片
   */
  public static ImageLoadResult loadBase64Image(String imageSource) {
    try {
      String base64Data = imageSource.substring(imageSource.indexOf(",") + 1);
      byte[] imageBytes = Base64.getDecoder().decode(base64Data);
      int pictureType = Workbook.PICTURE_TYPE_PNG;
      if (imageSource.startsWith("data:image/jpeg") || imageSource.startsWith("data:image/jpg")) {
        pictureType = Workbook.PICTURE_TYPE_JPEG;
      }
      else if (imageSource.startsWith("data:image/png")) {
        pictureType = Workbook.PICTURE_TYPE_PNG;
      }
      return new ImageLoadResult(true, imageBytes, pictureType);
    }
    catch (Exception e) {
      logger.warn("解析Base64图片失败: {}", e.getMessage());
      return new ImageLoadResult(false, null, Workbook.PICTURE_TYPE_PNG);
    }
  }

  /**
   * 从drawingObj提取图片源
   */
  public static String extractImageSourceFromDrawing(JsonNode drawingObj, int rowNum, int colNum) {
    if (drawingObj == null || drawingObj.isNull()) {
      logger.debug("addImageToCell: drawingObj 为 null, row={}, col={}", rowNum, colNum);
      return null;
    }

    JsonNode sourceNode = drawingObj.get("source");
    if (sourceNode == null || sourceNode.isNull()) {
      logger.debug("addImageToCell: drawingObj 中没有 source 节点, row={}, col={}", rowNum, colNum);
      return null;
    }

    String imageSource = sourceNode.asText();
    if (imageSource == null || imageSource.isEmpty()) {
      logger.debug("addImageToCell: source 为空, row={}, col={}", rowNum, colNum);
      return null;
    }

    return imageSource;
  }

  /**
   * 设置单元格值
   */
  public static void setCellValue(Cell cell, JsonNode cellObj) {
    JsonNode valueNode = cellObj.get("v");
    String value = valueNode != null ? valueNode.asText() : "";
    // 跳过 DISPIMG 函数（这是 Excel 的图片显示函数，不应该作为文本值）
    if (value != null && !value.trim().startsWith("_xlfn.DISPIMG") && !value.trim().startsWith("DISPIMG")) {
      cell.setCellValue(value);
    }
  }

  /**
   * 验证加载结果是否有效
   */
  public static boolean isValidLoadResult(ImageLoadResult loadResult) {
    return loadResult != null && Boolean.TRUE.equals(loadResult.getSuccess()) && loadResult.getImageBytes() != null
      && loadResult.getImageBytes().length > 0;
  }

  /**
   * 获取或创建单元格节点
   */
  public static ObjectNode getOrCreateCellNode(ObjectNode cellDataNode, int rowNum, int colNum,
    ObjectMapper objectMapper) {
    String rowKey = String.valueOf(rowNum);
    String colKey = String.valueOf(colNum);

    ObjectNode rowNode = (ObjectNode) cellDataNode.get(rowKey);
    if (rowNode == null) {
      rowNode = objectMapper.createObjectNode();
      cellDataNode.set(rowKey, rowNode);
    }

    ObjectNode cellNode = (ObjectNode) rowNode.get(colKey);
    if (cellNode == null) {
      cellNode = objectMapper.createObjectNode();
      rowNode.set(colKey, cellNode);
    }

    return cellNode;
  }

  /**
   * 创建documentStyle对象
   */
  public static ObjectNode createDocumentStyle(ObjectMapper objectMapper) {
    ObjectNode documentStyle = objectMapper.createObjectNode();
    ObjectNode pageSize = objectMapper.createObjectNode();
    pageSize.putNull("width");
    pageSize.putNull("height");
    documentStyle.set("pageSize", pageSize);
    documentStyle.put("marginTop", 0);
    documentStyle.put("marginBottom", 2);
    documentStyle.put("marginRight", 2);
    documentStyle.put("marginLeft", 2);

    ObjectNode renderConfig = objectMapper.createObjectNode();
    renderConfig.put("horizontalAlign", 0);
    renderConfig.put("verticalAlign", 0);
    renderConfig.put("centerAngle", 0);
    renderConfig.put("vertexAngle", 0);
    renderConfig.put("wrapStrategy", 0);
    renderConfig.put("zeroWidthParagraphBreak", 1);
    documentStyle.set("renderConfig", renderConfig);

    return documentStyle;
  }

  /**
   * 创建单元格图片的body对象
   */
  public static ObjectNode createCellImageBody(ObjectMapper objectMapper, String drawingId) {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("dataStream", "\b\r\n");
    body.putArray("textRuns");

    ArrayNode paragraphs = objectMapper.createArrayNode();
    ObjectNode paragraph = objectMapper.createObjectNode();
    paragraph.put("startIndex", 1);
    ObjectNode paragraphStyle = objectMapper.createObjectNode();
    paragraphStyle.put("horizontalAlign", 0);
    paragraph.set("paragraphStyle", paragraphStyle);
    paragraphs.add(paragraph);
    body.set("paragraphs", paragraphs);

    ArrayNode sectionBreaks = objectMapper.createArrayNode();
    ObjectNode sectionBreak = objectMapper.createObjectNode();
    sectionBreak.put("startIndex", 2);
    sectionBreaks.add(sectionBreak);
    body.set("sectionBreaks", sectionBreaks);

    ArrayNode customBlocks = objectMapper.createArrayNode();
    ObjectNode customBlock = objectMapper.createObjectNode();
    customBlock.put("startIndex", 0);
    customBlock.put("blockId", drawingId);
    customBlocks.add(customBlock);
    body.set("customBlocks", customBlocks);

    body.putArray("customRanges");
    body.putArray("customDecorations");

    return body;
  }
}
