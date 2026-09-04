package com.iwhalecloud.bote.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

/**
 * {@link FileTypeUtil} 单元测试
 *
 * <p>覆盖 getType(InputStream, suffix) 各分支（单一匹配、多匹配后缀优先、未匹配返回后缀）、
 * getType(String hexHead) 魔数匹配、getType(MultipartFile)、readBytes 边界（零长度、部分读取、
 * 完整读取）、以及 isPicture/isDocument/isVideo/isExcel 判断方法。</p>
 */
class FileTypeUtilTest {

  // ==================== getType(InputStream, suffix) ====================

  @Test
  void getType_jpegMagic_returnsJpg() throws IOException {
    // JPEG magic: ff d8 ff
    InputStream in = new ByteArrayInputStream(hexToBytes("ffd8ffe000104a464946"));
    String type = FileTypeUtil.getType(in, "jpg");
    assertThat(type).isEqualTo("jpg");
  }

  @Test
  void getType_pngMagic_returnsPng() throws IOException {
    InputStream in = new ByteArrayInputStream(hexToBytes("89504e470d0a1a0a"));
    String type = FileTypeUtil.getType(in, "png");
    assertThat(type).isEqualTo("png");
  }

  @Test
  void getType_zipMagicWithXlsxSuffix_returnsXlsx() throws IOException {
    // zip magic: 50 4B 03 04（对应多种文件类型，xlsx 在列表中）
    InputStream in = new ByteArrayInputStream(hexToBytes("504b0304140008000000"));
    String type = FileTypeUtil.getType(in, "xlsx");
    assertThat(type).isEqualTo("xlsx");
  }

  @Test
  void getType_zipMagicWithUnknownSuffix_returnsFirstType() throws IOException {
    // zip magic 对应多种类型，后缀不在列表中时返回第一个（zip）
    InputStream in = new ByteArrayInputStream(hexToBytes("504b0304140008000000"));
    String type = FileTypeUtil.getType(in, "unknown");
    assertThat(type).isEqualTo("zip");
  }

  @Test
  void getType_zipMagicWithNullSuffix_returnsFirstType() throws IOException {
    InputStream in = new ByteArrayInputStream(hexToBytes("504b0304140008000000"));
    String type = FileTypeUtil.getType(in, null);
    assertThat(type).isEqualTo("zip");
  }

  @Test
  void getType_unknownMagicWithSuffix_returnsSuffix() throws IOException {
    // 非已知魔数
    InputStream in = new ByteArrayInputStream(hexToBytes("deadbeef"));
    String type = FileTypeUtil.getType(in, "custom");
    assertThat(type).isEqualTo("custom");
  }

  @Test
  void getType_unknownMagicWithNullSuffix_returnsNull() throws IOException {
    InputStream in = new ByteArrayInputStream(hexToBytes("deadbeef"));
    String type = FileTypeUtil.getType(in, null);
    assertThat(type).isNull();
  }

  @Test
  void getType_pdfMagic_returnsPdf() throws IOException {
    InputStream in = new ByteArrayInputStream(hexToBytes("255044462d312e"));
    String type = FileTypeUtil.getType(in, "pdf");
    assertThat(type).isEqualTo("pdf");
  }

  @Test
  void getType_emptyStream_returnsSuffixOrNull() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[0]);
    // 空流读出 28 个 0x00 字节，不匹配任何魔数
    assertThat(FileTypeUtil.getType(in, "txt")).isEqualTo("txt");
  }

  // ==================== getType(String hexHead) ====================

  @Test
  void getTypeByHex_jpeg_returnsJpg() {
    assertThat(FileTypeUtil.getType("FFD8FFE000104A464946")).isEqualTo("jpg");
  }

  @Test
  void getTypeByHex_png_returnsPng() {
    assertThat(FileTypeUtil.getType("89504E470D0A1A0A")).isEqualTo("png");
  }

  @Test
  void getTypeByHex_gif87_returnsGif() {
    assertThat(FileTypeUtil.getType("4749463837")).isEqualTo("gif");
  }

  @Test
  void getTypeByHex_gif89_returnsGif() {
    assertThat(FileTypeUtil.getType("4749463839")).isEqualTo("gif");
  }

  @Test
  void getTypeByHex_pdf_returnsPdf() {
    assertThat(FileTypeUtil.getType("255044462D312E")).isEqualTo("pdf");
  }

  @Test
  void getTypeByHex_zip_returnsZip() {
    assertThat(FileTypeUtil.getType("504B030414000800")).isEqualTo("zip");
  }

  @Test
  void getTypeByHex_unknown_returnsNull() {
    assertThat(FileTypeUtil.getType("DEADBEEF")).isNull();
  }

  @Test
  void getTypeByHex_emptyString_returnsNull() {
    assertThat(FileTypeUtil.getType("")).isNull();
  }

  // ==================== getType(MultipartFile) ====================

  @Test
  void getType_multipartFileWithJpeg_returnsJpg() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(file.getOriginalFilename()).thenReturn("photo.jpg");
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(hexToBytes("ffd8ffe000104a464946")));

    String type = FileTypeUtil.getType(file);
    assertThat(type).isEqualTo("jpg");
  }

  @Test
  void getType_multipartFileWithUnknownType_throwsBssException() throws IOException {
    MultipartFile file = mock(MultipartFile.class);
    // 原始文件名为 null -> getExtension 返回 null -> getType 返回 null -> Assert.notNull 抛异常
    when(file.getOriginalFilename()).thenReturn(null);
    when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {(byte) 0xDE, (byte) 0xAD}));

    assertThatThrownBy(() -> FileTypeUtil.getType(file))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("未知的文件类型");
  }

  // ==================== readBytes ====================

  @Test
  void readBytes_lengthZero_returnsEmptyArray() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
    assertThat(FileTypeUtil.readBytes(in, 0)).isEmpty();
  }

  @Test
  void readBytes_negativeLength_returnsEmptyArray() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
    assertThat(FileTypeUtil.readBytes(in, -1)).isEmpty();
  }

  @Test
  void readBytes_fullRead_returnsAllBytes() throws IOException {
    byte[] data = {1, 2, 3, 4, 5};
    InputStream in = new ByteArrayInputStream(data);
    assertThat(FileTypeUtil.readBytes(in, 5)).isEqualTo(data);
  }

  @Test
  void readBytes_partialRead_returnsTrimmedArray() throws IOException {
    // 流只有 3 字节，请求 28 字节
    byte[] data = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};
    InputStream in = new ByteArrayInputStream(data);
    byte[] result = FileTypeUtil.readBytes(in, 28);
    assertThat(result).hasSize(3);
    assertThat(result).isEqualTo(data);
  }

  @Test
  void readBytes_emptyStream_returnsFullZeroArray() throws IOException {
    InputStream in = new ByteArrayInputStream(new byte[0]);
    byte[] result = FileTypeUtil.readBytes(in, 28);
    // read() 返回 -1，走 else 分支返回原数组（全 0）
    assertThat(result).hasSize(28);
    for (byte b : result) {
      assertThat(b).isEqualTo((byte) 0);
    }
  }

  // ==================== isPicture / isDocument / isVideo / isExcel ====================

  @Test
  void isPicture_validTypes_returnTrue() {
    assertThat(FileTypeUtil.isPicture("jpg")).isTrue();
    assertThat(FileTypeUtil.isPicture("PNG")).isTrue();
    assertThat(FileTypeUtil.isPicture("gif")).isTrue();
  }

  @Test
  void isPicture_invalidType_returnFalse() {
    assertThat(FileTypeUtil.isPicture("pdf")).isFalse();
  }

  @Test
  void isPicture_null_returnFalse() {
    assertThat(FileTypeUtil.isPicture(null)).isFalse();
  }

  @Test
  void isPicture_empty_returnFalse() {
    assertThat(FileTypeUtil.isPicture("")).isFalse();
  }

  @Test
  void isDocument_validTypes_returnTrue() {
    assertThat(FileTypeUtil.isDocument("pdf")).isTrue();
    assertThat(FileTypeUtil.isDocument("PDF")).isTrue();
    assertThat(FileTypeUtil.isDocument("jsp")).isTrue();
  }

  @Test
  void isDocument_invalidType_returnFalse() {
    assertThat(FileTypeUtil.isDocument("jpg")).isFalse();
  }

  @Test
  void isDocument_null_returnFalse() {
    assertThat(FileTypeUtil.isDocument(null)).isFalse();
  }

  @Test
  void isVideo_validTypes_returnTrue() {
    assertThat(FileTypeUtil.isVideo("mp4")).isTrue();
    assertThat(FileTypeUtil.isVideo("MP3")).isTrue();
    assertThat(FileTypeUtil.isVideo("avi")).isTrue();
  }

  @Test
  void isVideo_invalidType_returnFalse() {
    assertThat(FileTypeUtil.isVideo("pdf")).isFalse();
  }

  @Test
  void isVideo_null_returnFalse() {
    assertThat(FileTypeUtil.isVideo(null)).isFalse();
  }

  @Test
  void isExcel_validTypes_returnTrue() {
    assertThat(FileTypeUtil.isExcel("xls")).isTrue();
    assertThat(FileTypeUtil.isExcel("XLSX")).isTrue();
    assertThat(FileTypeUtil.isExcel("csv")).isTrue();
  }

  @Test
  void isExcel_invalidType_returnFalse() {
    assertThat(FileTypeUtil.isExcel("pdf")).isFalse();
  }

  @Test
  void isExcel_null_returnFalse() {
    assertThat(FileTypeUtil.isExcel(null)).isFalse();
  }

  // ==================== 辅助方法 ====================

  private static byte[] hexToBytes(String hex) {
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }
}
