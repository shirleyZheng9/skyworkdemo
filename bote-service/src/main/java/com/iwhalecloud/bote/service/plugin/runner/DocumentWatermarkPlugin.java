package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.DocumentWatermarkPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openpdf.text.Image;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfGState;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;
import org.springframework.stereotype.Component;

/**
 * 文档水印插件
 * 支持PDF、Word、Excel文档添加水印
 *
 * @author zhao.xu104
 * @since 2025-11-28
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentWatermarkPlugin extends AbstractFilePlugin<DocumentWatermarkPluginParams> {
  /** 支持的水印位置 */
  private static final List<String> SUPPORTED_POSITIONS = List.of(
    "top-left", "top-right", "bottom-left", "bottom-right", "center", "repeat"
  );
  /** 支持的文档格式 */
  private static final List<String> SUPPORTED_FORMATS = List.of(
    "pdf", "doc", "docx", "xls", "xlsx"
  );

  public DocumentWatermarkPlugin(IFileStoreService fileStoreService) {
    super(DocumentWatermarkPluginParams.class, fileStoreService);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOCUMENT_WATERMARK;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileUrl", "文件地址", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("watermarkText", "水印文本内容（与watermarkImageId二选一）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("watermarkImageId", "水印图片文件ID（可选，如果提供则使用图片水印）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("position", "水印位置（top-left/top-right/bottom-left/bottom-right/center/repeat，默认center）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fontSize", "字体大小（可选，默认24，仅文本水印）", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("color", "字体颜色（可选，默认#808080，仅文本水印）", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("opacity", "透明度（0.0-1.0，可选，默认0.5）", AttrDataType.NUMBER));
    children.add(ParameterSpec.newProperty("angle", "旋转角度（可选，默认-45，单位：度）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("fileId", "带水印的文档文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newProperty("fileName", "文件名", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileSize", "文件大小（字节）", AttrDataType.INTEGER));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(DocumentWatermarkPluginParams params) {
    validateFileParams(params);
    if (StringUtils.isBlank(params.getWatermarkText()) && params.getWatermarkImageId() == null) {
      throw new BssException("watermarkText和watermarkImageId不能同时为空");
    }
    String position = params.getPosition();
    if (StringUtils.isNotBlank(position)) {
      if (!SUPPORTED_POSITIONS.contains(position.toLowerCase(Locale.ROOT))) {
        throw new BssException("不支持的水印位置: " + position +
          "，支持的位置: " + String.join(", ", SUPPORTED_POSITIONS));
      }
    }
    if (params.getFontSize() != null && params.getFontSize() <= 0) {
      throw new BssException("字体大小必须大于0");
    }
    if (params.getOpacity() != null) {
      float opacity = params.getOpacity();
      if (opacity < 0.0f || opacity > 1.0f) {
        throw new BssException("透明度参数必须在0.0-1.0之间");
      }
    }
  }

  @Override
  public Object doRun(DocumentWatermarkPluginParams pluginParams) {
    try {
      byte[] originalBytes = downloadFile(pluginParams);
      String fileName = getOriginalFileName(pluginParams, "document.pdf");
      String fileExtension = getFileExtension(fileName, "pdf");
      // 根据文件格式选择不同的处理方式
      byte[] watermarkedBytes = switch (fileExtension.toLowerCase()) {
        case "pdf" -> addWatermarkToPdf(originalBytes, pluginParams);
        case "docx" -> addWatermarkToWord(originalBytes, pluginParams);
        case "doc" -> addWatermarkToLegacyWord(originalBytes, pluginParams);
        case "xlsx", "xls" -> addWatermarkToExcel(originalBytes, pluginParams, fileExtension);
        default -> throw new BssException("不支持的文档格式: " + fileExtension +
          "，支持的格式: " + String.join(", ", SUPPORTED_FORMATS));
      };
      // 生成新文件名
      String baseName = FilenameUtils.getBaseName(fileName);
      if (StringUtils.isBlank(baseName)) {
        baseName = "watermarked_document";
      }
      String newFileName = baseName + "_watermark." + fileExtension;
      // 上传带水印的文档
      FileInfoVO watermarkedFileInfo = uploadFile(watermarkedBytes, newFileName, fileExtension);
      logger.info("文档水印添加成功, sourceFileId={}, watermarkText={}, newFileId={}",
        pluginParams.getFileId(), pluginParams.getWatermarkText(), watermarkedFileInfo.getFileId());
      Map<String, Object> result = new HashMap<>();
      result.put("fileId", watermarkedFileInfo.getFileId());
      result.put("fileName", newFileName);
      result.put("fileSize", watermarkedBytes.length);
      return result;
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      logger.error("文档水印添加失败: {}", e.getMessage(), e);
      throw new BssException("文档水印添加失败: " + e.getMessage(), e);
    }
  }


  /**
   * 为PDF文档添加水印
   */
  private byte[] addWatermarkToPdf(byte[] pdfBytes, DocumentWatermarkPluginParams params) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PdfReader reader = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    PdfStamper stamper = null; //NOPMD - suppressed CloseResource - 已使用 finally 关闭
    try {
      reader = new PdfReader(pdfBytes);
      stamper = new PdfStamper(reader, outputStream);
      PdfWatermarkConfig config = extractPdfWatermarkConfig(params);
      PdfWatermarkContent watermarkContent = getPdfWatermarkContent(params);
      addWatermarkToAllPages(stamper, reader.getNumberOfPages(), config, watermarkContent, params);
      stamper.close();
      stamper = null;
      return outputStream.toByteArray();
    }
    catch (Exception e) {
      throw new BssException("PDF水印添加失败: " + e.getMessage(), e);
    }
    finally {
      closePdfResources(stamper, reader);
    }
  }

  /**
   * 提取PDF水印配置参数
   */
  private PdfWatermarkConfig extractPdfWatermarkConfig(DocumentWatermarkPluginParams params) {
    PdfWatermarkConfig config = new PdfWatermarkConfig();
    config.position = StringUtils.isNotBlank(params.getPosition())
      ? params.getPosition().toLowerCase(Locale.ROOT)
      : "center";
    config.repeat = "repeat".equals(config.position);
    config.opacity = params.getOpacity() != null ? params.getOpacity() : 0.5f;
    config.angle = params.getAngle() != null ? params.getAngle() : -45;
    return config;
  }

  /**
   * 获取PDF水印内容
   */
  private PdfWatermarkContent getPdfWatermarkContent(DocumentWatermarkPluginParams params) {
    PdfWatermarkContent content = new PdfWatermarkContent();
    content.text = params.getWatermarkText();
    if (params.getWatermarkImageId() != null) {
      byte[] imageBytes = fileStoreService.downloadFile(params.getWatermarkImageId());
      if (imageBytes == null || imageBytes.length == 0) {
        throw new BssException("水印图片不存在, watermarkImageId=" + params.getWatermarkImageId());
      }
      content.imageBytes = imageBytes;
    }
    return content;
  }

  /**
   * 为所有页面添加水印
   */
  private void addWatermarkToAllPages(PdfStamper stamper, int pageCount,
                                      PdfWatermarkConfig config, PdfWatermarkContent watermarkContent,
                                      DocumentWatermarkPluginParams params) throws IOException {
    for (int i = 1; i <= pageCount; i++) {
      PdfContentByte content = stamper.getOverContent(i);
      addWatermarkToPage(content, config, watermarkContent, params);
    }
  }

  /**
   * 为单个页面添加水印
   */
  private void addWatermarkToPage(PdfContentByte content, PdfWatermarkConfig config,
                                  PdfWatermarkContent watermarkContent,
                                  DocumentWatermarkPluginParams params) throws IOException {
    content.saveState();
    // 先设置透明度
    setPdfOpacity(content, config.opacity);
    // 再设置颜色（如果是文本水印）
    if (StringUtils.isNotBlank(watermarkContent.text)) {
      Color color = parseColor(params.getColor());
      content.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
    }
    if (watermarkContent.imageBytes != null) {
      addImageWatermarkToPdf(content, watermarkContent.imageBytes, config.position, config.angle, config.repeat);
    }
    else if (StringUtils.isNotBlank(watermarkContent.text)) {
      addTextWatermarkToPdf(content, watermarkContent.text, params, config.position, config.angle, config.repeat);
    }
    content.restoreState();
  }

  /**
   * 设置PDF透明度
   */
  private void setPdfOpacity(PdfContentByte content, float opacity) {
    PdfGState gState = new PdfGState();
    gState.setFillOpacity(opacity);
    gState.setStrokeOpacity(opacity);
    content.setGState(gState);
  }

  /**
   * 关闭PDF资源
   */
  private void closePdfResources(PdfStamper stamper, PdfReader reader) {
    if (stamper != null) {
      try {
        stamper.close();
      }
      catch (Exception e) {
        logger.warn("关闭PdfStamper失败: {}", e.getMessage());
      }
    }
    if (reader != null) {
      reader.close();
    }
  }

  /**
   * 为PDF添加文本水印
   * 注意：颜色已在 addWatermarkToPage 中设置，这里只需要设置字体和绘制
   */
  private void addTextWatermarkToPdf(PdfContentByte content, String text, DocumentWatermarkPluginParams params,
                                     String position, int angle, boolean repeat) throws IOException {
    int fontSize = params.getFontSize() != null ? params.getFontSize() : 24;
    try {
      BaseFont baseFont = createPdfBaseFont(text);
      content.setFontAndSize(baseFont, fontSize);
      // 获取页面尺寸
      float pageWidth = content.getPdfDocument().getPageSize().getWidth();
      float pageHeight = content.getPdfDocument().getPageSize().getHeight();
      if (repeat) {
        drawRepeatPdfWatermark(content, text, baseFont, fontSize, pageWidth, pageHeight, angle, params);
      }
      else {
        drawSinglePdfWatermark(content, text, baseFont, fontSize, pageWidth, pageHeight, position, angle);
      }
    }
    catch (Exception e) {
      throw new IOException("添加PDF文本水印失败: " + e.getMessage(), e);
    }
  }

  /**
   * 创建PDF基础字体（支持中文）
   */
  private BaseFont createPdfBaseFont(String text) throws IOException {
    boolean hasChinese = text.chars().anyMatch(c -> c >= 0x4E00 && c <= 0x9FFF);
    if (!hasChinese) {
      return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }
    // 尝试使用STSong-Light（OpenPDF自带的中文字体）
    try {
      return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
    }
    catch (Exception e) {
      logger.warn("无法加载STSong-Light字体，尝试使用HELVETICA: {}", e.getMessage());
      // 如果失败，使用HELVETICA（不支持中文，但至少不会报错）
      return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
    }
  }

  /**
   * 绘制重复的PDF水印
   */
  private void drawRepeatPdfWatermark(PdfContentByte content, String text, BaseFont baseFont,
                                      int fontSize, float pageWidth, float pageHeight, int angle,
                                      DocumentWatermarkPluginParams params) {
    float textWidth = baseFont.getWidthPoint(text, fontSize);
    float spacing = textWidth * 1.5f;
    int maxX = (int) ((pageWidth + spacing) / spacing);
    int maxY = (int) ((pageHeight + spacing) / spacing);
    // 在循环外设置颜色，避免重复解析
    Color color = parseColor(params.getColor());
    for (int i = 0; i <= maxX; i++) {
      for (int j = 0; j <= maxY; j++) {
        float x = i * spacing;
        float y = j * spacing;
        content.saveState();
        // 每次绘制前重新设置颜色和字体（因为saveState会重置状态）
        content.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
        content.beginText();
        content.setFontAndSize(baseFont, fontSize);
        content.showTextAligned(org.openpdf.text.Element.ALIGN_CENTER, text, x, y, angle);
        content.endText();
        content.restoreState();
      }
    }
  }

  /**
   * 绘制单个PDF水印
   */
  private void drawSinglePdfWatermark(PdfContentByte content, String text, BaseFont baseFont,
                                      int fontSize, float pageWidth, float pageHeight,
                                      String position, int angle) {
    float[] positionXY = calculatePdfPosition(text, baseFont, fontSize, pageWidth, pageHeight, position);
    content.beginText();
    content.showTextAligned(org.openpdf.text.Element.ALIGN_CENTER, text, positionXY[0], positionXY[1], angle);
    content.endText();
  }

  /**
   * 为PDF添加图片水印
   */
  private void addImageWatermarkToPdf(PdfContentByte content, byte[] imageBytes, String position,
                                      int angle, boolean repeat) throws IOException {
    try {
      Image image = Image.getInstance(imageBytes);
      float pageWidth = content.getPdfDocument().getPageSize().getWidth();
      float pageHeight = content.getPdfDocument().getPageSize().getHeight();
      if (repeat) {
        drawRepeatPdfImageWatermark(content, image, pageWidth, pageHeight, angle);
      }
      else {
        drawSinglePdfImageWatermark(content, image, pageWidth, pageHeight, position, angle);
      }
    }
    catch (Exception e) {
      throw new IOException("添加PDF图片水印失败: " + e.getMessage(), e);
    }
  }

  /**
   * 绘制重复的PDF图片水印
   */
  private void drawRepeatPdfImageWatermark(PdfContentByte content, Image image,
                                           float pageWidth, float pageHeight, int angle) {
    float spacing = Math.max(image.getWidth(), image.getHeight()) * 1.5f;
    int maxX = (int) ((pageWidth + spacing) / spacing);
    int maxY = (int) ((pageHeight + spacing) / spacing);
    for (int i = 0; i <= maxX; i++) {
      for (int j = 0; j <= maxY; j++) {
        float x = i * spacing;
        float y = j * spacing;
        content.saveState();
        image.setAbsolutePosition(x, y);
        if (angle != 0) {
          content.addImage(image, image.getWidth(), 0, 0, image.getHeight(), x, y);
        }
        else {
          content.addImage(image);
        }
        content.restoreState();
      }
    }
  }

  /**
   * 绘制单个PDF图片水印
   */
  private void drawSinglePdfImageWatermark(PdfContentByte content, Image image,
                                           float pageWidth, float pageHeight,
                                           String position, int angle) {
    float[] positionXY = calculatePdfImagePosition(image, pageWidth, pageHeight, position);
    image.setAbsolutePosition(positionXY[0], positionXY[1]);
    if (angle != 0) {
      applyImageRotation(content, image, positionXY, angle);
    }
    else {
      content.addImage(image);
    }
  }

  /**
   * 应用图片旋转
   */
  private void applyImageRotation(PdfContentByte content, Image image, float[] positionXY, int angle) {
    float centerX = positionXY[0] + image.getWidth() / 2;
    float centerY = positionXY[1] + image.getHeight() / 2;
    content.saveState();
    content.concatCTM(1, 0, 0, 1, centerX, centerY);
    content.concatCTM(
      (float) Math.cos(Math.toRadians(angle)), (float) Math.sin(Math.toRadians(angle)),
      -(float) Math.sin(Math.toRadians(angle)), (float) Math.cos(Math.toRadians(angle)),
      0, 0
    );
    content.concatCTM(1, 0, 0, 1, -centerX, -centerY);
    image.setAbsolutePosition(positionXY[0], positionXY[1]);
    content.addImage(image);
    content.restoreState();
  }

  /**
   * 计算PDF文本水印位置
   */
  private float[] calculatePdfPosition(String text, BaseFont font, float fontSize,
                                       float pageWidth, float pageHeight, String position) {
    float textWidth = font.getWidthPoint(text, fontSize);
    float x;
    float y = switch (position.toLowerCase(Locale.ROOT)) {
      case "top-left" -> {
        x = textWidth / 2 + 20;
        yield pageHeight - fontSize - 20;
      }
      case "top-right" -> {
        x = pageWidth - textWidth / 2 - 20;
        yield pageHeight - fontSize - 20;
      }
      case "bottom-left" -> {
        x = textWidth / 2 + 20;
        yield fontSize + 20;
      }
      case "bottom-right" -> {
        x = pageWidth - textWidth / 2 - 20;
        yield fontSize + 20;
      }
      default -> {
        x = pageWidth / 2;
        yield pageHeight / 2;
      }
    };
    return new float[]{x, y};
  }

  /**
   * 计算PDF图片水印位置
   */
  private float[] calculatePdfImagePosition(Image image, float pageWidth, float pageHeight, String position) {
    image.getWidth();
    float x;
    float y = switch (position.toLowerCase(Locale.ROOT)) {
      case "top-left" -> {
        x = 20;
        yield pageHeight - image.getHeight() - 20;
      }
      case "top-right" -> {
        x = pageWidth - image.getWidth() - 20;
        yield pageHeight - image.getHeight() - 20;
      }
      case "bottom-left" -> {
        x = 20;
        yield 20;
      }
      case "bottom-right" -> {
        x = pageWidth - image.getWidth() - 20;
        yield 20;
      }
      default -> {
        x = (pageWidth - image.getWidth()) / 2;
        yield (pageHeight - image.getHeight()) / 2;
      }
    };
    return new float[]{x, y};
  }

  /**
   * 为Word文档添加水印
   */
  private byte[] addWatermarkToWord(byte[] wordBytes, DocumentWatermarkPluginParams params) throws IOException {
    try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(wordBytes));
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      String watermarkText = params.getWatermarkText();
      byte[] watermarkImageBytes = null;
      if (params.getWatermarkImageId() != null) {
        watermarkImageBytes = fileStoreService.downloadFile(params.getWatermarkImageId());
        if (watermarkImageBytes == null || watermarkImageBytes.length == 0) {
          throw new BssException("水印图片不存在, watermarkImageId=" + params.getWatermarkImageId());
        }
      }
      // 创建页眉或页脚来添加水印
      XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
      if (headerFooterPolicy == null) {
        headerFooterPolicy = document.createHeaderFooterPolicy();
      }
      // 使用页眉添加水印（可以覆盖整个页面）
      XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
      XWPFParagraph paragraph = header.createParagraph();
      paragraph.setAlignment(ParagraphAlignment.CENTER);
      if (watermarkImageBytes != null) {
        // 图片水印
        try {
          document.addPictureData(watermarkImageBytes, XWPFDocument.PICTURE_TYPE_PNG);
          XWPFRun run = paragraph.createRun();
          int width = Units.toEMU(200); // 宽度
          int height = Units.toEMU(100); // 高度
          run.addPicture(new ByteArrayInputStream(watermarkImageBytes), PictureType.PNG, "watermark.png", width, height);
        }
        catch (Exception e) {
          logger.warn("添加Word图片水印失败，改用文本水印: {}", e.getMessage());
          addTextWatermarkToWordParagraph(paragraph, watermarkText, params);
        }
      }
      else if (StringUtils.isNotBlank(watermarkText)) {
        // 文本水印
        addTextWatermarkToWordParagraph(paragraph, watermarkText, params);
      }
      document.write(outputStream);
      return outputStream.toByteArray();
    }
    catch (Exception e) {
      throw new IOException("Word水印添加失败: " + e.getMessage(), e);
    }
  }

  /**
   * 为Word段落添加文本水印
   */
  private void addTextWatermarkToWordParagraph(XWPFParagraph paragraph, String text, DocumentWatermarkPluginParams params) {
    int fontSize = params.getFontSize() != null ? params.getFontSize() : 24;
    Color color = parseColor(params.getColor());
    XWPFRun run = paragraph.createRun();
    run.setText(text);
    run.setFontSize(fontSize);
    run.setColor(String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
    run.setBold(true);
  }

  /**
   * 为旧版Word文档（.doc格式）添加水印
   * 注意：.doc格式的API支持有限，主要通过页眉添加水印
   */
  private byte[] addWatermarkToLegacyWord(byte[] wordBytes, DocumentWatermarkPluginParams params) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(wordBytes))) {
      String watermarkText = params.getWatermarkText();
      if (params.getWatermarkImageId() != null) {
        logger.warn(".doc格式暂不支持图片水印，将使用文本水印");
      }
      // 对于.doc格式，在文档开头插入水印文本
      // 注意：HWPFDocument的API支持有限，无法直接修改页眉，只能在文档内容中添加
      if (StringUtils.isNotBlank(watermarkText)) {
        try {
          Range range = document.getRange();
          // 在文档开头插入水印文本
          range.insertBefore(watermarkText + "\n");
        }
        catch (Exception e) {
          logger.error("无法添加.doc格式水印: {}", e.getMessage());
          throw new IOException("无法添加.doc格式水印: " + e.getMessage(), e);
        }
      }
      document.write(outputStream);
      return outputStream.toByteArray();
    }
    catch (Exception e) {
      throw new IOException("旧版Word文档水印添加失败: " + e.getMessage(), e);
    }
  }

  /**
   * 为Excel文档添加水印
   */
  private byte[] addWatermarkToExcel(byte[] excelBytes, DocumentWatermarkPluginParams params, String fileExtension) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (Workbook workbook = createWorkbook(new ByteArrayInputStream(excelBytes), fileExtension)) {
      WatermarkContent watermarkContent = getWatermarkContent(params);
      addWatermarkToAllSheets(workbook, watermarkContent);
      workbook.write(outputStream);
      return outputStream.toByteArray();
    }
    catch (Exception e) {
      throw new IOException("Excel水印添加失败: " + e.getMessage(), e);
    }
  }

  /**
   * 创建Workbook
   */
  private Workbook createWorkbook(InputStream inputStream, String fileExtension) throws IOException {
    if ("xlsx".equals(fileExtension)) {
      return new XSSFWorkbook(inputStream);
    }
    return new HSSFWorkbook(inputStream);
  }

  /**
   * 获取水印内容
   */
  private WatermarkContent getWatermarkContent(DocumentWatermarkPluginParams params) {
    WatermarkContent content = new WatermarkContent();
    content.text = params.getWatermarkText();
    if (params.getWatermarkImageId() != null) {
      byte[] imageBytes = fileStoreService.downloadFile(params.getWatermarkImageId());
      if (imageBytes == null || imageBytes.length == 0) {
        throw new BssException("水印图片不存在, watermarkImageId=" + params.getWatermarkImageId());
      }
      content.imageBytes = imageBytes;
    }
    return content;
  }

  /**
   * 为所有工作表添加水印
   */
  private void addWatermarkToAllSheets(Workbook workbook, WatermarkContent watermarkContent) {
    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
      Sheet sheet = workbook.getSheetAt(i);
      addWatermarkToSheet(sheet, watermarkContent);
    }
  }

  /**
   * 为单个工作表添加水印
   */
  private void addWatermarkToSheet(Sheet sheet, WatermarkContent watermarkContent) {
    Header header = sheet.getHeader();
    if (watermarkContent.imageBytes != null) {
      addImageWatermarkToSheet(sheet, watermarkContent);
    }
    else if (StringUtils.isNotBlank(watermarkContent.text)) {
      header.setCenter("&C" + watermarkContent.text);
    }
  }

  /**
   * 为工作表添加图片水印
   */
  private void addImageWatermarkToSheet(Sheet sheet, WatermarkContent watermarkContent) {
    Header header = sheet.getHeader();
    try {
      addImageWatermarkToExcel(sheet, watermarkContent.imageBytes);
    }
    catch (Exception e) {
      logger.warn("添加Excel图片水印失败，改用文本水印: {}", e.getMessage());
      if (StringUtils.isNotBlank(watermarkContent.text)) {
        header.setCenter("&C" + watermarkContent.text);
      }
    }
  }

  /**
   * 为Excel添加图片水印
   */
  private void addImageWatermarkToExcel(Sheet sheet, byte[] imageBytes) throws IOException {
    try {
      // Excel图片水印需要添加到绘图对象中
      Drawing<?> drawing = sheet.createDrawingPatriarch();
      // 创建图片锚点（覆盖整个工作表）
      ClientAnchor anchor;
      if (sheet instanceof XSSFSheet) {
        anchor = new XSSFClientAnchor(0, 0, 0, 0, 0, 0, 10, 10);
      }
      else {
        anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 10, 10);
      }
      // 添加图片
      int pictureIdx = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
      Picture picture = drawing.createPicture(anchor, pictureIdx);
      // 设置图片大小和位置
      picture.resize();
    }
    catch (Exception e) {
      throw new IOException("添加Excel图片水印失败: " + e.getMessage(), e);
    }
  }

  /**
   * 解析颜色字符串
   * 支持十六进制颜色码（如#FF0000）、RGB格式（如rgb(255,0,0)）和颜色名称（如red）
   */
  private Color parseColor(String colorStr) {
    if (StringUtils.isBlank(colorStr)) {
      return new Color(128, 128, 128); // 默认灰色 #808080
    }
    String color = colorStr.trim();
    // 处理十六进制颜色
    if (color.startsWith("#")) {
      try {
        return Color.decode(color);
      }
      catch (NumberFormatException e) {
        logger.warn("无法解析颜色值: {}, 使用默认灰色", color);
        return new Color(128, 128, 128);
      }
    }
    // 处理RGB格式：rgb(255,0,0) 或 RGB(255,0,0)
    if (color.startsWith("rgb(") || color.startsWith("RGB(")) {
      try {
        String rgb = color.substring(color.indexOf("(") + 1, color.length() - 1);
        String[] parts = rgb.split(",");
        if (parts.length == 3) {
          int r = Integer.parseInt(parts[0].trim());
          int g = Integer.parseInt(parts[1].trim());
          int b = Integer.parseInt(parts[2].trim());
          // 确保RGB值在0-255范围内
          r = Math.max(0, Math.min(255, r));
          g = Math.max(0, Math.min(255, g));
          b = Math.max(0, Math.min(255, b));
          return new Color(r, g, b);
        }
      }
      catch (Exception e) {
        logger.warn("无法解析RGB颜色值: {}, 使用默认灰色", color);
      }
    }
    // 处理颜色名称（如red、blue、green等）
    try {
      // 使用反射获取Color类的静态字段
      java.lang.reflect.Field field = Color.class.getField(color.toUpperCase(Locale.ROOT));
      return (Color) field.get(null);
    }
    catch (Exception e) {
      logger.warn("无法解析颜色名称: {}, 使用默认灰色", color);
    }

    // 如果都解析失败，尝试使用Color.decode（可能支持其他格式）
    try {
      return Color.decode("#" + color);
    }
    catch (Exception e) {
      logger.warn("无法解析颜色: {}, 使用默认灰色", color);
      return new Color(128, 128, 128);
    }
  }

  /**
   * PDF水印配置内部类
   */
  private static final class PdfWatermarkConfig {
    String position;
    boolean repeat;
    float opacity;
    int angle;
  }

  /**
   * PDF水印内容内部类
   */
  private static final class PdfWatermarkContent {
    String text;
    byte[] imageBytes;
  }

  /**
   * 水印内容内部类
   */
  private static final class WatermarkContent {
    String text;
    byte[] imageBytes;
  }
}
