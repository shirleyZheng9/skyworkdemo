package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.common.utils.converter.ImageLoadResult;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * {@link DcExcelutil} 单元测试。
 *
 * <p>该类大部分方法深耦合 POI Workbook/Drawing 与 DocumentAttachmentHelper（文件 IO），
 * 故按可测性聚焦：经反射覆盖纯逻辑私有方法（图片类型、EMU 尺寸、transform 解析、
 * 浮动图片位置、资源查找、图片加载分发），以及对公共入口 {@code convertExcelToLuckysheet}
 * 做内存 XSSF 往返与非 Excel 守卫测试。不启动 Spring 容器、不联网、不连 DB。</p>
 */
class DcExcelutilTest {

  private static final ObjectMapper OM = new ObjectMapper();

  // ---- 反射调用辅助 ----

  private int getPictureTypeFromFileName(String fileName) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("getPictureTypeFromFileName", String.class);
    m.setAccessible(true);
    return (int) m.invoke(null, fileName);
  }

  private int[] calculateAndLimitCellImageSize(double w, double h) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod(
      "calculateAndLimitCellImageSize", double.class, double.class);
    m.setAccessible(true);
    return (int[]) m.invoke(null, w, h);
  }

  private double[] getDimensionsFromTransform(JsonNode transform, int aw, int ah) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod(
      "getDimensionsFromTransform", JsonNode.class, int.class, int.class);
    m.setAccessible(true);
    return (double[]) m.invoke(null, transform, aw, ah);
  }

  private int[] getImageActualSize(byte[] imageBytes) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("getImageActualSize", byte[].class);
    m.setAccessible(true);
    return (int[]) m.invoke(null, (Object) imageBytes);
  }

  private String extractImageSource(JsonNode drawingObj) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("extractImageSource", JsonNode.class);
    m.setAccessible(true);
    return (String) m.invoke(null, drawingObj);
  }

  private int[] extractFloatingImagePosition(JsonNode drawingObj) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("extractFloatingImagePosition", JsonNode.class);
    m.setAccessible(true);
    return (int[]) m.invoke(null, drawingObj);
  }

  private JsonNode findDrawingPluginResource(JsonNode resources) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("findDrawingPluginResource", JsonNode.class);
    m.setAccessible(true);
    return (JsonNode) m.invoke(null, resources);
  }

  private ImageLoadResult loadImageData(String imageSource,
    com.iwhalecloud.bote.doc.module.document.service.helper.DocumentAttachmentHelper helper) throws Exception {
    Method m = DcExcelutil.class.getDeclaredMethod("loadImageData", String.class,
      com.iwhalecloud.bote.doc.module.document.service.helper.DocumentAttachmentHelper.class);
    m.setAccessible(true);
    return (ImageLoadResult) m.invoke(null, imageSource, helper);
  }

  // ============ getPictureTypeFromFileName ============

  @Test
  void getPictureTypeFromFileName_mapsByExtension() throws Exception {
    assertThat(getPictureTypeFromFileName("a.jpg")).isEqualTo(Workbook.PICTURE_TYPE_JPEG);
    assertThat(getPictureTypeFromFileName("a.JPEG")).isEqualTo(Workbook.PICTURE_TYPE_JPEG);
    assertThat(getPictureTypeFromFileName("a.png")).isEqualTo(Workbook.PICTURE_TYPE_PNG);
    // GIF 在 POI 不支持，回落 PNG
    assertThat(getPictureTypeFromFileName("a.gif")).isEqualTo(Workbook.PICTURE_TYPE_PNG);
    // 未知扩展名也回落 PNG
    assertThat(getPictureTypeFromFileName("a.bmp")).isEqualTo(Workbook.PICTURE_TYPE_PNG);
  }

  // ============ calculateAndLimitCellImageSize ============

  @Test
  void calculateAndLimitCellImageSize_withinCell_noScaling() throws Exception {
    int[] size = calculateAndLimitCellImageSize(10, 10);
    assertThat(size[0]).isEqualTo(10 * 9525);
    assertThat(size[1]).isEqualTo(10 * 9525);
  }

  @Test
  void calculateAndLimitCellImageSize_tooWide_scalesByWidth() throws Exception {
    // 默认单元格宽 88px、高 24px。宽 200 超限 -> 按宽比例缩放
    int[] size = calculateAndLimitCellImageSize(200, 20);
    assertThat(size[0]).isEqualTo(88 * 9525);
    // height = 20*9525 * (88/200)
    int expectedHeight = (int) (20 * 9525 * ((double) (88 * 9525) / (200 * 9525)));
    assertThat(size[1]).isEqualTo(expectedHeight);
  }

  @Test
  void calculateAndLimitCellImageSize_tooTall_scalesByHeight() throws Exception {
    int[] size = calculateAndLimitCellImageSize(50, 100);
    assertThat(size[1]).isEqualTo(24 * 9525);
    int expectedWidth = (int) (50 * 9525 * ((double) (24 * 9525) / (100 * 9525)));
    assertThat(size[0]).isEqualTo(expectedWidth);
  }

  // ============ getDimensionsFromTransform ============

  @Test
  void getDimensionsFromTransform_usesTransformValues() throws Exception {
    JsonNode t = OM.readTree("{\"width\":300,\"height\":400}");
    double[] dims = getDimensionsFromTransform(t, 0, 0);
    assertThat(dims[0]).isEqualTo(300.0);
    assertThat(dims[1]).isEqualTo(400.0);
  }

  @Test
  void getDimensionsFromTransform_missingFields_defaults() throws Exception {
    JsonNode t = OM.readTree("{}");
    double[] dims = getDimensionsFromTransform(t, 0, 0);
    assertThat(dims[0]).isEqualTo(100.0);
    assertThat(dims[1]).isEqualTo(20.0);
  }

  @Test
  void getDimensionsFromTransform_tooSmall_fallsBackToActual() throws Exception {
    // width < 20 且 actualWidth > 0 -> 用实际宽
    JsonNode t = OM.readTree("{\"width\":10,\"height\":5}");
    double[] dims = getDimensionsFromTransform(t, 200, 50);
    assertThat(dims[0]).isEqualTo(200.0);
    assertThat(dims[1]).isEqualTo(50.0);
  }

  // ============ getImageActualSize ============

  @Test
  void getImageActualSize_validPng_returnsDimensions() throws Exception {
    byte[] png = makePng(2, 3);
    int[] size = getImageActualSize(png);
    assertThat(size[0]).isEqualTo(2);
    assertThat(size[1]).isEqualTo(3);
  }

  @Test
  void getImageActualSize_invalidBytes_returnsZero() throws Exception {
    int[] size = getImageActualSize(new byte[]{1, 2, 3});
    assertThat(size[0]).isZero();
    assertThat(size[1]).isZero();
  }

  // ============ extractImageSource ============

  @Test
  void extractImageSource_present_returnsText() throws Exception {
    JsonNode obj = OM.readTree("{\"source\":\"https://x/y.png\"}");
    assertThat(extractImageSource(obj)).isEqualTo("https://x/y.png");
  }

  @Test
  void extractImageSource_missingOrEmpty_returnsNull() throws Exception {
    assertThat(extractImageSource(OM.readTree("{}"))).isNull();
    assertThat(extractImageSource(OM.readTree("{\"source\":\"\"}"))).isNull();
    assertThat(extractImageSource(OM.readTree("{\"source\":null}"))).isNull();
  }

  // ============ extractFloatingImagePosition ============

  @Test
  void extractFloatingImagePosition_noSheetTransform_returnsNull() throws Exception {
    assertThat(extractFloatingImagePosition(OM.readTree("{}"))).isNull();
  }

  @Test
  void extractFloatingImagePosition_withToNode_returnsFromTo() throws Exception {
    JsonNode obj = OM.readTree(
      "{\"sheetTransform\":{\"from\":{\"column\":1,\"row\":2},\"to\":{\"column\":3,\"row\":5}}}");
    assertThat(extractFloatingImagePosition(obj)).containsExactly(1, 2, 3, 5);
  }

  @Test
  void extractFloatingImagePosition_toMissingColumn_defaultsToStart() throws Exception {
    JsonNode obj = OM.readTree(
      "{\"sheetTransform\":{\"from\":{\"column\":1,\"row\":2},\"to\":{}}}");
    assertThat(extractFloatingImagePosition(obj)).containsExactly(1, 2, 1, 2);
  }

  @Test
  void extractFloatingImagePosition_noTo_calculatesFromTransform() throws Exception {
    JsonNode obj = OM.readTree(
      "{\"transform\":{\"width\":200,\"height\":100},"
        + "\"sheetTransform\":{\"from\":{\"column\":1,\"row\":2}}}");
    // endCol = 1 + ceil(200/88) = 1 + 3 = 4; endRow = 2 + ceil(100/24) = 2 + 5 = 7
    assertThat(extractFloatingImagePosition(obj)).containsExactly(1, 2, 4, 7);
  }

  @Test
  void extractFloatingImagePosition_noToNoTransform_returnsStart() throws Exception {
    JsonNode obj = OM.readTree("{\"sheetTransform\":{\"from\":{\"column\":1,\"row\":2}}}");
    assertThat(extractFloatingImagePosition(obj)).containsExactly(1, 2, 1, 2);
  }

  @Test
  void extractFloatingImagePosition_noFrom_returnsNull() throws Exception {
    JsonNode obj = OM.readTree("{\"sheetTransform\":{}}");
    assertThat(extractFloatingImagePosition(obj)).isNull();
  }

  // ============ findDrawingPluginResource ============

  @Test
  void findDrawingPluginResource_foundParsesData() throws Exception {
    JsonNode resources = OM.readTree(
      "[{\"name\":\"OTHER\",\"data\":\"{}\"},"
        + "{\"name\":\"SHEET_DRAWING_PLUGIN\",\"data\":\"{\\\"x\\\":1}\"}]");
    JsonNode result = findDrawingPluginResource(resources);
    assertThat(result).isNotNull();
    assertThat(result.get("x").asInt()).isEqualTo(1);
  }

  @Test
  void findDrawingPluginResource_notFound_returnsNull() throws Exception {
    JsonNode resources = OM.readTree("[{\"name\":\"OTHER\",\"data\":\"{}\"}]");
    assertThat(findDrawingPluginResource(resources)).isNull();
  }

  @Test
  void findDrawingPluginResource_emptyDataSkipped() throws Exception {
    JsonNode resources = OM.readTree(
      "[{\"name\":\"SHEET_DRAWING_PLUGIN\",\"data\":\"\"}]");
    assertThat(findDrawingPluginResource(resources)).isNull();
  }

  @Test
  void findDrawingPluginResource_invalidData_returnsNull() throws Exception {
    JsonNode resources = OM.readTree(
      "[{\"name\":\"SHEET_DRAWING_PLUGIN\",\"data\":\"{invalid\"}]");
    assertThat(findDrawingPluginResource(resources)).isNull();
  }

  // ============ loadImageData（unsupported 分支） ============

  @Test
  void loadImageData_unsupportedScheme_returnsFailureWithPngType() throws Exception {
    ImageLoadResult result = loadImageData("ftp://example.com/x", null);
    assertThat(result.getSuccess()).isFalse();
    assertThat(result.getPictureType()).isEqualTo(Workbook.PICTURE_TYPE_PNG);
  }

  // ============ convertExcelToLuckysheet（公共） ============

  @Test
  void convertExcelToLuckysheet_roundTripsInMemoryXlsx() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet("数据");
      Row row = sheet.createRow(0);
      row.createCell(0).setCellValue("名称");
      row.createCell(1).setCellValue(123);
      wb.write(baos);
    }
    String json = DcExcelutil.convertExcelToLuckysheet(
      new ByteArrayInputStream(baos.toByteArray()), "doc-1", 1L, null);

    JsonNode root = OM.readTree(json);
    assertThat(root.has("sheets")).isTrue();
    assertThat(root.has("sheetOrder")).isTrue();
    assertThat(root.has("resources")).isTrue();
    // sheetOrder 与 sheets 数量一致
    JsonNode sheetOrder = root.get("sheetOrder");
    JsonNode sheets = root.get("sheets");
    assertThat(sheets.size()).isEqualTo(sheetOrder.size()).isEqualTo(1);
    // 资源数组含预定义资源名
    JsonNode resources = root.get("resources");
    assertThat(resources.size()).isGreaterThan(0);
    boolean hasDrawingPlugin = false;
    for (JsonNode r : resources) {
      if ("SHEET_DRAWING_PLUGIN".equals(r.get("name").asText())) {
        hasDrawingPlugin = true;
        // documentAttachmentService 为 null -> data 为 "{}"
        assertThat(r.get("data").asText()).isEqualTo("{}");
      }
    }
    assertThat(hasDrawingPlugin).isTrue();
  }

  @Test
  void convertExcelToLuckysheet_nonExcel_throws() {
    InputStream is = new ByteArrayInputStream("not an excel file".getBytes(StandardCharsets.UTF_8));
    assertThatThrownBy(() -> DcExcelutil.convertExcelToLuckysheet(is, "doc-1", 1L, null))
      .isInstanceOf(IOException.class)
      .hasMessageContaining("Excel");
  }

  // ---- 辅助：生成指定尺寸 PNG ----

  private static byte[] makePng(int w, int h) throws IOException {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(img, "png", out);
    return out.toByteArray();
  }
}
