package com.iwhalecloud.bote.doc.common.utils.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iwhalecloud.bote.doc.module.document.dto.AttachmentUrlInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.imageio.ImageIO;
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
import org.junit.jupiter.api.Test;

/**
 * {@link DcExcelHelper} 单元测试。
 *
 * <p>该类为纯静态工具类，覆盖全部公有静态方法的正常/分支/边界路径：
 * MIME 类型、文件名、附件 URL 解析、DISPIMG 单元格清洗、浮动图片判定、
 * 图片尺寸计算、Luckysheet JSON 节点构造、Base64 图片加载、单元格数据提取等。
 * POI Sheet/Row/Cell/ClientAnchor 用 Mockito mock；图片字节数组用内存 BufferedImage 生成。
 * 不启动 Spring 容器、不联网、不连 DB。</p>
 */
class DcExcelHelperTest {

  private static final ObjectMapper OM = new ObjectMapper();

  // ---- getImageMimeType ----

  @Test
  void getImageMimeType_returnsPng_whenBlank() {
    assertThat(DcExcelHelper.getImageMimeType(null)).isEqualTo("image/png");
    assertThat(DcExcelHelper.getImageMimeType("")).isEqualTo("image/png");
    assertThat(DcExcelHelper.getImageMimeType("   ")).isEqualTo("image/png");
  }

  @Test
  void getImageMimeType_returnsMappedType_forKnownExtensions() {
    assertThat(DcExcelHelper.getImageMimeType("png")).isEqualTo("image/png");
    assertThat(DcExcelHelper.getImageMimeType("PNG")).isEqualTo("image/png");
    assertThat(DcExcelHelper.getImageMimeType("jpg")).isEqualTo("image/jpeg");
    assertThat(DcExcelHelper.getImageMimeType("jpeg")).isEqualTo("image/jpeg");
    assertThat(DcExcelHelper.getImageMimeType("gif")).isEqualTo("image/gif");
    assertThat(DcExcelHelper.getImageMimeType("bmp")).isEqualTo("image/bmp");
    assertThat(DcExcelHelper.getImageMimeType("webp")).isEqualTo("image/webp");
  }

  @Test
  void getImageMimeType_returnsPng_forUnknownExtension() {
    assertThat(DcExcelHelper.getImageMimeType("tiff")).isEqualTo("image/png");
    assertThat(DcExcelHelper.getImageMimeType("svg")).isEqualTo("image/png");
  }

  // ---- createPictureFileName ----

  @Test
  void createPictureFileName_usesNanoFallbackAndDefaultPng_whenSuggestedNameBlank() {
    String result = DcExcelHelper.createPictureFileName(null, "png");
    assertThat(result).startsWith("excel_image_").endsWith(".png");

    // extension 为空/blank 时回退默认 png
    String resultNoExt = DcExcelHelper.createPictureFileName(null, "");
    assertThat(resultNoExt).startsWith("excel_image_").endsWith(".png");

    String resultNullExt = DcExcelHelper.createPictureFileName(null, null);
    assertThat(resultNullExt).startsWith("excel_image_").endsWith(".png");
  }

  @Test
  void createPictureFileName_appendsExtension_whenNotPresent() {
    assertThat(DcExcelHelper.createPictureFileName("img1", "png")).isEqualTo("img1.png");
    assertThat(DcExcelHelper.createPictureFileName("img1", "jpg")).isEqualTo("img1.jpg");
  }

  @Test
  void createPictureFileName_keepsBaseName_whenAlreadyEndsWithExtensionCaseInsensitive() {
    // 大小写不一致也应识别为已含扩展名，原样返回（保留原大小写）
    assertThat(DcExcelHelper.createPictureFileName("img1.PNG", "png")).isEqualTo("img1.PNG");
    assertThat(DcExcelHelper.createPictureFileName("img1.png", "png")).isEqualTo("img1.png");
  }

  @Test
  void createPictureFileName_appendsDefaultPng_whenExtensionBlank() {
    assertThat(DcExcelHelper.createPictureFileName("img1", "")).isEqualTo("img1.png");
    assertThat(DcExcelHelper.createPictureFileName("img1", null)).isEqualTo("img1.png");
  }

  // ---- isAttachmentUrl ----

  @Test
  void isAttachmentUrl_returnsFalse_forNullOrEmpty() {
    assertThat(DcExcelHelper.isAttachmentUrl(null)).isFalse();
    assertThat(DcExcelHelper.isAttachmentUrl("")).isFalse();
  }

  @Test
  void isAttachmentUrl_recognizesApiAndNonApiPrefix() {
    assertThat(DcExcelHelper.isAttachmentUrl("/api/bote/dc/document/attachment/files/doc1/file.png")).isTrue();
    assertThat(DcExcelHelper.isAttachmentUrl("/bote/dc/document/attachment/files/doc1/file.png")).isTrue();
    assertThat(DcExcelHelper.isAttachmentUrl("http://other/path/file.png")).isFalse();
    assertThat(DcExcelHelper.isAttachmentUrl("/api/bote/dc/document/attachment/files/doc1/file.png?token=x")).isTrue();
  }

  // ---- parseAttachmentUrl ----

  @Test
  void parseAttachmentUrl_returnsNull_forNullOrEmpty() {
    assertThat(DcExcelHelper.parseAttachmentUrl(null)).isNull();
    assertThat(DcExcelHelper.parseAttachmentUrl("")).isNull();
  }

  @Test
  void parseAttachmentUrl_extractsDocumentIdAndFileName_whenMatched() {
    AttachmentUrlInfoDTO dto = DcExcelHelper
      .parseAttachmentUrl("/api/bote/dc/document/attachment/files/doc1/report.xlsx");
    assertThat(dto).isNotNull();
    assertThat(dto.getDocumentId()).isEqualTo("doc1");
    assertThat(dto.getFileName()).isEqualTo("report.xlsx");

    AttachmentUrlInfoDTO dto2 = DcExcelHelper
      .parseAttachmentUrl("/bote/dc/document/attachment/files/docId2/sheet.png");
    assertThat(dto2.getDocumentId()).isEqualTo("docId2");
    assertThat(dto2.getFileName()).isEqualTo("sheet.png");
  }

  @Test
  void parseAttachmentUrl_returnsNull_whenNotMatched() {
    assertThat(DcExcelHelper.parseAttachmentUrl("http://other/path/file.png")).isNull();
  }

  // ---- hasCellImage ----

  @Test
  void hasCellImage_returnsFalse_forNullOrNullNode() {
    assertThat(DcExcelHelper.hasCellImage(null)).isFalse();
    assertThat(DcExcelHelper.hasCellImage(NullNode.getInstance())).isFalse();
  }

  @Test
  void hasCellImage_returnsFalse_whenNoPNode() {
    ObjectNode cell = OM.createObjectNode();
    cell.put("v", "text");
    assertThat(DcExcelHelper.hasCellImage(cell)).isFalse();
  }

  @Test
  void hasCellImage_returnsFalse_whenNoDrawingsOrEmpty() {
    ObjectNode cell = OM.createObjectNode();
    cell.putObject("p"); // p 存在但无 drawings
    assertThat(DcExcelHelper.hasCellImage(cell)).isFalse();

    ObjectNode cell2 = OM.createObjectNode();
    ObjectNode p2 = cell2.putObject("p");
    p2.set("drawings", NullNode.getInstance()); // drawings 为 null
    assertThat(DcExcelHelper.hasCellImage(cell2)).isFalse();

    ObjectNode cell3 = OM.createObjectNode();
    ObjectNode p3 = cell3.putObject("p");
    p3.putArray("drawings"); // 空 drawings 数组
    assertThat(DcExcelHelper.hasCellImage(cell3)).isFalse();
  }

  @Test
  void hasCellImage_returnsTrue_whenDrawingsNonEmpty() {
    ObjectNode cell = OM.createObjectNode();
    ObjectNode p = cell.putObject("p");
    ArrayNode drawings = p.putArray("drawings");
    drawings.add(OM.createObjectNode().put("id", "d1"));
    assertThat(DcExcelHelper.hasCellImage(cell)).isTrue();
  }

  // ---- cleanDispimgCells ----

  @Test
  void cleanDispimgCells_removesOrStripsDispimgCellsAndDropsEmptyRows() {
    ObjectNode cellData = OM.createObjectNode();

    // row 0: cell0 纯 DISPIMG(无 p 无其他字段)->remove; cell1 DISPIMG+有 p->keep; cell2 DISPIMG+其他字段->removeV
    ObjectNode row0 = cellData.putObject("0");
    row0.putObject("0").put("v", "_xlfn.DISPIMG(ID1)");
    ObjectNode cell1 = row0.putObject("1");
    cell1.put("v", "_xlfn.DISPIMG(ID2)");
    cell1.putObject("p");
    ObjectNode cell2 = row0.putObject("2");
    cell2.put("v", "_xlfn.DISPIMG(ID3)");
    cell2.put("s", 1);

    // row 1: 普通值保留
    ObjectNode row1 = cellData.putObject("1");
    row1.putObject("0").put("v", "normal");

    // row 2: 仅一个纯 DISPIMG 单元格 -> 单元格删除后行变空 -> 行删除
    ObjectNode row2 = cellData.putObject("2");
    row2.putObject("0").put("v", "DISPIMG(ID4)");

    // row 3: 无 v 字段的单元格 -> keep
    ObjectNode row3 = cellData.putObject("3");
    row3.putObject("0").put("s", 2);

    DcExcelHelper.cleanDispimgCells(cellData);

    // row 0 保留，cell0 已删，cell1 保留含 p，cell2 保留但 v 已删
    assertThat(cellData.has("0")).isTrue();
    ObjectNode afterRow0 = (ObjectNode) cellData.get("0");
    assertThat(afterRow0.has("0")).isFalse();
    assertThat(afterRow0.has("1")).isTrue();
    assertThat(afterRow0.get("1").has("v")).isTrue();
    assertThat(afterRow0.get("1").has("p")).isTrue();
    assertThat(afterRow0.has("2")).isTrue();
    assertThat(afterRow0.get("2").has("v")).isFalse();
    assertThat(afterRow0.get("2").get("s").asInt()).isEqualTo(1);

    // row 1 保留
    assertThat(cellData.has("1")).isTrue();
    assertThat(cellData.get("1").get("0").get("v").asText()).isEqualTo("normal");

    // row 2 已删除（变空）
    assertThat(cellData.has("2")).isFalse();

    // row 3 保留（无 v 字段，keep）
    assertThat(cellData.has("3")).isTrue();
    assertThat(cellData.get("3").get("0").has("v")).isFalse();
  }

  // ---- isFloatingPicture ----

  @Test
  void isFloatingPicture_returnsFalse_forNullAnchor() {
    assertThat(DcExcelHelper.isFloatingPicture(null)).isFalse();
  }

  @Test
  void isFloatingPicture_returnsFalse_forSingleCellWithSmallOffset() {
    ClientAnchor anchor = stubAnchor(0, 0, 0, 0);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDy1()).thenReturn(0);
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isFalse();
  }

  @Test
  void isFloatingPicture_returnsTrue_forSingleCellWithLargeDxOffset() {
    ClientAnchor anchor = stubAnchor(1, 1, 1, 1);
    when(anchor.getDx1()).thenReturn(84001); // > 84000
    when(anchor.getDy1()).thenReturn(0);
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isTrue();
  }

  @Test
  void isFloatingPicture_returnsTrue_forSingleCellWithLargeDyOffset() {
    ClientAnchor anchor = stubAnchor(0, 0, 0, 0);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDy1()).thenReturn(25201); // > 25200
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isTrue();
  }

  @Test
  void isFloatingPicture_returnsFalse_forMultiCellWithSmallOffset() {
    ClientAnchor anchor = stubAnchor(0, 2, 0, 0);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDy1()).thenReturn(0);
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isFalse();
  }

  @Test
  void isFloatingPicture_returnsTrue_forMultiCellWithLargeDxOffset() {
    ClientAnchor anchor = stubAnchor(0, 2, 0, 1);
    when(anchor.getDx1()).thenReturn(168001); // > 168000
    when(anchor.getDy1()).thenReturn(0);
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isTrue();
  }

  @Test
  void isFloatingPicture_returnsTrue_forMultiCellWithLargeDyOffset() {
    ClientAnchor anchor = stubAnchor(0, 2, 0, 1);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDy1()).thenReturn(50401); // > 50400
    assertThat(DcExcelHelper.isFloatingPicture(anchor)).isTrue();
  }

  // ---- calculateImageSize ----

  @Test
  void calculateImageSize_returnsActualSize_whenImageBytesReadable() throws IOException {
    byte[] png = pngBytes(10, 20);
    ClientAnchor anchor = mock(ClientAnchor.class);
    int[] size = DcExcelHelper.calculateImageSize(png, anchor);
    assertThat(size).containsExactly(10, 20);
  }

  @Test
  void calculateImageSize_usesAnchorEmu_whenImageBytesUnreadable() {
    byte[] bad = "not-an-image".getBytes();
    ClientAnchor anchor = stubAnchor(0, 0, 0, 0);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDx2()).thenReturn(9525); // 9525/9525 = 1px
    when(anchor.getDy1()).thenReturn(0);
    when(anchor.getDy2()).thenReturn(9525);
    int[] size = DcExcelHelper.calculateImageSize(bad, anchor);
    assertThat(size).containsExactly(1, 1);
  }

  @Test
  void calculateImageSize_fallsBackToCellEstimate_whenAnchorSizeInvalid() {
    byte[] bad = "not-an-image".getBytes();
    ClientAnchor anchor = stubAnchor(0, 2, 0, 3);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDx2()).thenReturn(0); // width 0
    when(anchor.getDy1()).thenReturn(0);
    when(anchor.getDy2()).thenReturn(0); // height 0
    int[] size = DcExcelHelper.calculateImageSize(bad, anchor);
    assertThat(size).containsExactly(176, 72); // (2-0)*88=176, (3-0)*24=72
  }

  @Test
  void calculateImageSize_fallsBackToMinCellSize_whenColsRowsZero() {
    byte[] bad = "not-an-image".getBytes();
    ClientAnchor anchor = stubAnchor(0, 0, 0, 0);
    when(anchor.getDx1()).thenReturn(0);
    when(anchor.getDx2()).thenReturn(0);
    when(anchor.getDy1()).thenReturn(0);
    when(anchor.getDy2()).thenReturn(0);
    int[] size = DcExcelHelper.calculateImageSize(bad, anchor);
    assertThat(size).containsExactly(88, 24);
  }

  // ---- calculateImageSizeFromBytes ----

  @Test
  void calculateImageSizeFromBytes_returnsActualSize_forValidImage() throws IOException {
    byte[] png = pngBytes(15, 25);
    assertThat(DcExcelHelper.calculateImageSizeFromBytes(png)).containsExactly(15, 25);
  }

  @Test
  void calculateImageSizeFromBytes_returnsDefault_forInvalidImage() {
    assertThat(DcExcelHelper.calculateImageSizeFromBytes("bad".getBytes())).containsExactly(200, 200);
    assertThat(DcExcelHelper.calculateImageSizeFromBytes(new byte[] {1, 2, 3})).containsExactly(200, 200);
  }

  // ---- createFloatingDrawingObjectWithoutAnchor ----

  @Test
  void createFloatingDrawingObjectWithoutAnchor_buildsExpectedStructure() {
    ObjectNode obj = DcExcelHelper
      .createFloatingDrawingObjectWithoutAnchor(OM, "http://img", "s1", "w1", "d1", 100, 50);
    assertThat(obj.get("drawingId").asText()).isEqualTo("d1");
    assertThat(obj.get("drawingType").asInt()).isEqualTo(0);
    assertThat(obj.get("imageSourceType").asText()).isEqualTo("URL");
    assertThat(obj.get("source").asText()).isEqualTo("http://img");
    assertThat(obj.get("unitId").asText()).isEqualTo("w1");
    assertThat(obj.get("subUnitId").asText()).isEqualTo("s1");
    assertThat(obj.get("column").asInt()).isZero();
    assertThat(obj.get("width").asInt()).isEqualTo(100);
    assertThat(obj.get("height").asInt()).isEqualTo(50);
    // sheetTransform.to 根据 100x50 估算
    JsonNode to = obj.get("sheetTransform").get("to");
    assertThat(to.get("column").asInt()).isEqualTo(2); // ceil(100/88)=2
    assertThat(to.get("row").asInt()).isEqualTo(3); // ceil(50/24)=3
    assertThat(to.get("columnOffset").asInt()).isEqualTo(100 % 88); // 12
    assertThat(to.get("rowOffset").asInt()).isEqualTo(50 % 24); // 2
    // transform
    assertThat(obj.get("transform").get("width").asInt()).isEqualTo(100);
    assertThat(obj.get("behindDoc").asInt()).isZero();
    assertThat(obj.get("wrapText").asInt()).isZero();
  }

  @Test
  void createFloatingDrawingObjectWithoutAnchor_usesMinEndCol_whenSmallImage() {
    ObjectNode obj = DcExcelHelper
      .createFloatingDrawingObjectWithoutAnchor(OM, "u", "s", "w", "d", 88, 24);
    JsonNode to = obj.get("sheetTransform").get("to");
    assertThat(to.get("column").asInt()).isEqualTo(1); // max(1, ceil(88/88)=1)
    assertThat(to.get("row").asInt()).isEqualTo(1); // max(1, ceil(24/24)=1)
  }

  // ---- createDrawingObject ----

  @Test
  void createDrawingObject_buildsExpectedStructure() {
    ObjectNode obj = DcExcelHelper.createDrawingObject(OM, "http://img", 80, 60, "d9");
    assertThat(obj.get("unitId").asText()).isEqualTo("d");
    assertThat(obj.get("subUnitId").asText()).isEqualTo("d");
    assertThat(obj.get("drawingId").asText()).isEqualTo("d9");
    assertThat(obj.get("source").asText()).isEqualTo("http://img");
    assertThat(obj.get("transform").get("width").asInt()).isEqualTo(80);
    assertThat(obj.get("transform").get("height").asInt()).isEqualTo(60);
    assertThat(obj.get("docTransform").get("size").get("width").asInt()).isEqualTo(80);
    assertThat(obj.get("docTransform").get("positionH").get("relativeFrom").asInt()).isZero();
    assertThat(obj.get("docTransform").get("positionV").get("relativeFrom").asInt()).isEqualTo(1);
    assertThat(obj.get("behindDoc").asInt()).isZero();
  }

  // ---- createFloatingDrawingObject ----

  @Test
  void createFloatingDrawingObject_buildsExpectedStructureFromAnchor() {
    ClientAnchor anchor = stubAnchor(1, 3, 2, 4);
    when(anchor.getDx1()).thenReturn(9525);
    when(anchor.getDy1()).thenReturn(9525);

    ObjectNode obj = DcExcelHelper
      .createFloatingDrawingObject(OM, "http://img", anchor, "s1", "w1", "d1", 50, 40);
    assertThat(obj.get("source").asText()).isEqualTo("http://img");
    assertThat(obj.get("unitId").asText()).isEqualTo("w1");
    assertThat(obj.get("subUnitId").asText()).isEqualTo("s1");
    assertThat(obj.get("column").asInt()).isEqualTo(1);
    assertThat(obj.get("row").asInt()).isEqualTo(2);
    JsonNode sheetTransform = obj.get("sheetTransform");
    assertThat(sheetTransform.get("from").get("column").asInt()).isEqualTo(1);
    assertThat(sheetTransform.get("from").get("row").asInt()).isEqualTo(2);
    assertThat(sheetTransform.get("to").get("column").asInt()).isEqualTo(3);
    assertThat(sheetTransform.get("to").get("row").asInt()).isEqualTo(4);
    // left = col1*88 + dx1/9525 = 88 + 1 = 89; top = row1*24 + dy1/9525 = 48 + 1 = 49
    assertThat(obj.get("transform").get("left").asInt()).isEqualTo(89);
    assertThat(obj.get("transform").get("top").asInt()).isEqualTo(49);
    assertThat(obj.get("transform").get("width").asInt()).isEqualTo(50);
  }

  // ---- readInputStream ----

  @Test
  void readInputStream_returnsAllBytes() throws IOException {
    byte[] data = "hello world".getBytes();
    InputStream in = new ByteArrayInputStream(data);
    assertThat(DcExcelHelper.readInputStream(in)).isEqualTo(data);
  }

  @Test
  void readInputStream_returnsEmpty_forEmptyStream() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[0]);
    assertThat(DcExcelHelper.readInputStream(in)).isEmpty();
  }

  // ---- loadBase64Image ----

  @Test
  void loadBase64Image_decodesPng() {
    String base64 = java.util.Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});
    ImageLoadResult result = DcExcelHelper.loadBase64Image("data:image/png;base64," + base64);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getImageBytes()).containsExactly(1, 2, 3);
    assertThat(result.getPictureType()).isEqualTo(Workbook.PICTURE_TYPE_PNG);
  }

  @Test
  void loadBase64Image_recognizesJpegAndJpgPrefix() {
    String base64 = java.util.Base64.getEncoder().encodeToString(new byte[] {9, 8});
    assertThat(DcExcelHelper.loadBase64Image("data:image/jpeg;base64," + base64).getPictureType())
      .isEqualTo(Workbook.PICTURE_TYPE_JPEG);
    assertThat(DcExcelHelper.loadBase64Image("data:image/jpg;base64," + base64).getPictureType())
      .isEqualTo(Workbook.PICTURE_TYPE_JPEG);
  }

  @Test
  void loadBase64Image_returnsFailure_forInvalidBase64() {
    ImageLoadResult result = DcExcelHelper.loadBase64Image("data:image/png;base64,@@@invalid@@@");
    assertThat(result.getSuccess()).isFalse();
    assertThat(result.getImageBytes()).isNull();
    assertThat(result.getPictureType()).isEqualTo(Workbook.PICTURE_TYPE_PNG);
  }

  // ---- isValidLoadResult ----

  @Test
  void isValidLoadResult_returnsFalse_forNull() {
    assertThat(DcExcelHelper.isValidLoadResult(null)).isFalse();
  }

  @Test
  void isValidLoadResult_returnsFalse_whenSuccessFalse() {
    assertThat(DcExcelHelper.isValidLoadResult(new ImageLoadResult(false, new byte[] {1}, Workbook.PICTURE_TYPE_PNG)))
      .isFalse();
  }

  @Test
  void isValidLoadResult_returnsFalse_whenBytesNullOrEmpty() {
    assertThat(DcExcelHelper.isValidLoadResult(new ImageLoadResult(true, null, Workbook.PICTURE_TYPE_PNG))).isFalse();
    assertThat(DcExcelHelper.isValidLoadResult(new ImageLoadResult(true, new byte[0], Workbook.PICTURE_TYPE_PNG)))
      .isFalse();
  }

  @Test
  void isValidLoadResult_returnsTrue_whenSuccessAndNonEmptyBytes() {
    assertThat(DcExcelHelper.isValidLoadResult(new ImageLoadResult(true, new byte[] {1, 2}, Workbook.PICTURE_TYPE_PNG)))
      .isTrue();
  }

  // ---- extractImageSourceFromDrawing ----

  @Test
  void extractImageSourceFromDrawing_returnsNull_forNullOrNullNode() {
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(null, 0, 0)).isNull();
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(NullNode.getInstance(), 0, 0)).isNull();
  }

  @Test
  void extractImageSourceFromDrawing_returnsNull_whenNoSourceOrBlank() {
    ObjectNode drawing = OM.createObjectNode();
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(drawing, 0, 0)).isNull();

    drawing.set("source", NullNode.getInstance());
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(drawing, 0, 0)).isNull();

    drawing.put("source", "");
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(drawing, 0, 0)).isNull();
  }

  @Test
  void extractImageSourceFromDrawing_returnsSource_whenPresent() {
    ObjectNode drawing = OM.createObjectNode();
    drawing.put("source", "http://img/x.png");
    assertThat(DcExcelHelper.extractImageSourceFromDrawing(drawing, 1, 2)).isEqualTo("http://img/x.png");
  }

  // ---- setCellValue ----

  @Test
  void setCellValue_setsValue_forNormalText() {
    Cell cell = mock(Cell.class);
    ObjectNode cellObj = OM.createObjectNode();
    cellObj.put("v", "hello");
    DcExcelHelper.setCellValue(cell, cellObj);
    verify(cell).setCellValue("hello");
  }

  @Test
  void setCellValue_setsEmpty_whenNoVNode() {
    Cell cell = mock(Cell.class);
    ObjectNode cellObj = OM.createObjectNode();
    DcExcelHelper.setCellValue(cell, cellObj);
    verify(cell).setCellValue("");
  }

  @Test
  void setCellValue_skipsDispimgValue() {
    Cell cell = mock(Cell.class);
    ObjectNode cellObj1 = OM.createObjectNode();
    cellObj1.put("v", "_xlfn.DISPIMG(ID1)");
    DcExcelHelper.setCellValue(cell, cellObj1);
    verify(cell, never()).setCellValue(org.mockito.ArgumentMatchers.anyString());

    ObjectNode cellObj2 = OM.createObjectNode();
    cellObj2.put("v", "DISPIMG(ID2)");
    DcExcelHelper.setCellValue(cell, cellObj2);
    verify(cell, never()).setCellValue(org.mockito.ArgumentMatchers.anyString());
  }

  // ---- getOrCreateCellNode ----

  @Test
  void getOrCreateCellNode_createsRowAndCell_whenAbsent() {
    ObjectNode cellData = OM.createObjectNode();
    ObjectNode cell = DcExcelHelper.getOrCreateCellNode(cellData, 0, 0, OM);
    assertThat(cell).isNotNull();
    assertThat(cellData.has("0")).isTrue();
    assertThat(cellData.get("0").has("0")).isTrue();
  }

  @Test
  void getOrCreateCellNode_reusesExistingRowAndCell() {
    ObjectNode cellData = OM.createObjectNode();
    ObjectNode row = cellData.putObject("1");
    ObjectNode existing = row.putObject("2");
    existing.put("v", "old");
    ObjectNode cell = DcExcelHelper.getOrCreateCellNode(cellData, 1, 2, OM);
    assertThat(cell).isSameAs(existing);
    assertThat(cell.get("v").asText()).isEqualTo("old");
  }

  @Test
  void getOrCreateCellNode_createsCellInExistingRow() {
    ObjectNode cellData = OM.createObjectNode();
    cellData.putObject("1"); // 已有行但无单元格
    ObjectNode cell = DcExcelHelper.getOrCreateCellNode(cellData, 1, 5, OM);
    assertThat(cell).isNotNull();
    assertThat(cellData.get("1").has("5")).isTrue();
  }

  // ---- buildRootNode ----

  @Test
  void buildRootNode_buildsLuckysheetRoot() {
    ObjectNode root = DcExcelHelper.buildRootNode(OM);
    assertThat(root.get("id").asText()).hasSize(6).matches("[0-9a-f]{6}");
    assertThat(root.get("name").asText()).isEmpty();
    assertThat(root.get("appVersion").asText()).isEqualTo("0.10.5");
    assertThat(root.get("locale").asText()).isEqualTo("zhCN");
    assertThat(root.get("styles").isObject()).isTrue();
  }

  // ---- createDocumentStyle ----

  @Test
  void createDocumentStyle_buildsExpectedStyle() {
    ObjectNode style = DcExcelHelper.createDocumentStyle(OM);
    assertThat(style.get("pageSize").get("width").isNull()).isTrue();
    assertThat(style.get("pageSize").get("height").isNull()).isTrue();
    assertThat(style.get("marginTop").asInt()).isZero();
    assertThat(style.get("marginBottom").asInt()).isEqualTo(2);
    assertThat(style.get("marginLeft").asInt()).isEqualTo(2);
    assertThat(style.get("marginRight").asInt()).isEqualTo(2);
    JsonNode renderConfig = style.get("renderConfig");
    assertThat(renderConfig.get("horizontalAlign").asInt()).isZero();
    assertThat(renderConfig.get("zeroWidthParagraphBreak").asInt()).isEqualTo(1);
  }

  // ---- createCellImageBody ----

  @Test
  void createCellImageBody_buildsExpectedBody() {
    ObjectNode body = DcExcelHelper.createCellImageBody(OM, "d1");
    assertThat(body.get("dataStream").asText()).isEqualTo("\b\r\n");
    assertThat(body.get("textRuns").isArray()).isTrue();
    assertThat(body.get("textRuns").size()).isZero();
    JsonNode paragraph = body.get("paragraphs").get(0);
    assertThat(paragraph.get("startIndex").asInt()).isEqualTo(1);
    assertThat(paragraph.get("paragraphStyle").get("horizontalAlign").asInt()).isZero();
    assertThat(body.get("sectionBreaks").get(0).get("startIndex").asInt()).isEqualTo(2);
    assertThat(body.get("customBlocks").get(0).get("blockId").asText()).isEqualTo("d1");
    assertThat(body.get("customRanges").isArray()).isTrue();
    assertThat(body.get("customDecorations").isArray()).isTrue();
  }

  // ---- countCellImagesFromHSSF ----

  @Test
  void countCellImagesFromHSSF_returnsZero_whenNoDrawing() {
    HSSFSheet sheet = mock(HSSFSheet.class);
    when(sheet.getDrawingPatriarch()).thenReturn(null);
    assertThat(DcExcelHelper.countCellImagesFromHSSF(sheet)).isZero();
  }

  @Test
  void countCellImagesFromHSSF_countsOnlyPictures() {
    HSSFSheet sheet = mock(HSSFSheet.class);
    HSSFPatriarch patriarch = mock(HSSFPatriarch.class);
    HSSFPicture pic1 = mock(HSSFPicture.class);
    HSSFPicture pic2 = mock(HSSFPicture.class);
    HSSFShape other = mock(HSSFShape.class);
    when(sheet.getDrawingPatriarch()).thenReturn(patriarch);
    when(patriarch.getChildren()).thenReturn(List.of(pic1, other, pic2));
    assertThat(DcExcelHelper.countCellImagesFromHSSF(sheet)).isEqualTo(2);
  }

  // ---- countCellImagesFromXSSF ----

  @Test
  void countCellImagesFromXSSF_returnsZero_whenNoDrawing() {
    XSSFSheet sheet = mock(XSSFSheet.class);
    when(sheet.getDrawingPatriarch()).thenReturn(null);
    assertThat(DcExcelHelper.countCellImagesFromXSSF(sheet)).isZero();
  }

  @Test
  void countCellImagesFromXSSF_countsOnlyPictures() {
    XSSFSheet sheet = mock(XSSFSheet.class);
    XSSFDrawing drawing = mock(XSSFDrawing.class);
    XSSFPicture pic1 = mock(XSSFPicture.class);
    XSSFShape other = mock(XSSFShape.class);
    when(sheet.getDrawingPatriarch()).thenReturn(drawing);
    when(drawing.getShapes()).thenReturn(List.of(pic1, other));
    assertThat(DcExcelHelper.countCellImagesFromXSSF(sheet)).isEqualTo(1);
  }

  // ---- extractCellData ----

  @Test
  void extractCellData_extractsCellValues() {
    Sheet sheet = mock(Sheet.class);
    Row row = mock(Row.class);
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    DataFormatter formatter = mock(DataFormatter.class);
    when(sheet.getLastRowNum()).thenReturn(0);
    when(sheet.getRow(0)).thenReturn(row);
    when(row.getLastCellNum()).thenReturn((short) 2);
    when(row.getCell(0)).thenReturn(cell0);
    when(row.getCell(1)).thenReturn(cell1);
    when(formatter.formatCellValue(cell0)).thenReturn("hello");
    when(formatter.formatCellValue(cell1)).thenReturn("world");

    ObjectNode cellData = OM.createObjectNode();
    DcExcelHelper.extractCellData(sheet, cellData, formatter);

    ObjectNode rowNode = (ObjectNode) cellData.get("0");
    assertThat(rowNode).isNotNull();
    assertThat(rowNode.get("0").get("v").asText()).isEqualTo("hello");
    assertThat(rowNode.get("1").get("v").asText()).isEqualTo("world");
  }

  @Test
  void extractCellData_skipsNullRowAndBlankValues() {
    Sheet sheet = mock(Sheet.class);
    Row row = mock(Row.class);
    Cell cell0 = mock(Cell.class);
    Cell cell1 = mock(Cell.class);
    DataFormatter formatter = mock(DataFormatter.class);
    when(sheet.getLastRowNum()).thenReturn(1);
    when(sheet.getRow(0)).thenReturn(null); // null row -> skip
    when(sheet.getRow(1)).thenReturn(row);
    when(row.getLastCellNum()).thenReturn((short) 2);
    when(row.getCell(0)).thenReturn(cell0);
    when(row.getCell(1)).thenReturn(null); // null cell -> skip
    when(formatter.formatCellValue(cell0)).thenReturn("   "); // blank value -> skip

    ObjectNode cellData = OM.createObjectNode();
    DcExcelHelper.extractCellData(sheet, cellData, formatter);
    assertThat(cellData.size()).isZero();
  }

  @Test
  void extractCellData_skipsRowWithNegativeLastCellNum() {
    Sheet sheet = mock(Sheet.class);
    Row row = mock(Row.class);
    DataFormatter formatter = mock(DataFormatter.class);
    when(sheet.getLastRowNum()).thenReturn(0);
    when(sheet.getRow(0)).thenReturn(row);
    when(row.getLastCellNum()).thenReturn((short) -1);

    ObjectNode cellData = OM.createObjectNode();
    DcExcelHelper.extractCellData(sheet, cellData, formatter);
    assertThat(cellData.size()).isZero();
  }

  // ---- uploadImage ----

  @Test
  void uploadImage_returnsNull_whenImageBytesNullOrEmpty() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    assertThat(DcExcelHelper.uploadImage(null, "png", "doc1", 1L, service)).isNull();
    assertThat(DcExcelHelper.uploadImage(new byte[0], "png", "doc1", 1L, service)).isNull();
  }

  @Test
  void uploadImage_returnsNull_whenParamsIncomplete() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    byte[] bytes = new byte[] {1};
    assertThat(DcExcelHelper.uploadImage(bytes, "png", "doc1", null, service)).isNull();
    assertThat(DcExcelHelper.uploadImage(bytes, "png", "", 1L, service)).isNull();
    assertThat(DcExcelHelper.uploadImage(bytes, "png", "doc1", 1L, null)).isNull();
  }

  @Test
  void uploadImage_returnsNull_whenServiceReturnsBlankUrl() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl(""); // blank
    when(service.upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("doc1"),
      org.mockito.ArgumentMatchers.eq(1L))).thenReturn(dto);
    assertThat(DcExcelHelper.uploadImage(new byte[] {1}, "png", "doc1", 1L, service)).isNull();
  }

  @Test
  void uploadImage_returnsNull_whenServiceReturnsNull() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    when(service.upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyLong())).thenReturn(null);
    assertThat(DcExcelHelper.uploadImage(new byte[] {1}, "png", "doc1", 1L, service)).isNull();
  }

  @Test
  void uploadImage_returnsApiPrefixedUrl_onSuccess() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl("/files/x.png");
    when(service.upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("doc1"),
      org.mockito.ArgumentMatchers.eq(1L))).thenReturn(dto);
    String url = DcExcelHelper.uploadImage(new byte[] {1, 2, 3}, "png", "doc1", 1L, service);
    assertThat(url).isEqualTo("/api/files/x.png");
  }

  @Test
  void uploadImage_returnsNull_whenServiceThrows() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    when(service.upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyLong())).thenThrow(new RuntimeException("boom"));
    assertThat(DcExcelHelper.uploadImage(new byte[] {1}, "png", "doc1", 1L, service)).isNull();
  }

  // ---- helpers ----

  /** 构造一个已 stub col1/col2/row1/row2 的 ClientAnchor mock（POI 中 col 返回 short，row 返回 int）。 */
  private static ClientAnchor stubAnchor(int col1, int col2, int row1, int row2) {
    ClientAnchor anchor = mock(ClientAnchor.class);
    when(anchor.getCol1()).thenReturn((short) col1);
    when(anchor.getCol2()).thenReturn((short) col2);
    when(anchor.getRow1()).thenReturn(row1);
    when(anchor.getRow2()).thenReturn(row2);
    return anchor;
  }

  private static byte[] pngBytes(int w, int h) throws IOException {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    return baos.toByteArray();
  }
}
