package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * {@link ContentTypeUtil} 单元测试。
 *
 * <p>覆盖扩展名到 MIME 的自定义映射、空/未知扩展名回退、大小写归一化，以及 image/video/audio/text/office
 * 谓词判定。MimeTypeUtils.parseMimeType 对纯扩展名（无 "/"）会抛异常被 catch，故实际走自定义映射表。</p>
 */
class ContentTypeUtilTest {

  @Test
  void getContentType_blankFileName_returnsOctetStream() {
    assertThat(ContentTypeUtil.getContentType(null)).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(ContentTypeUtil.getContentType("")).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(ContentTypeUtil.getContentType("   ")).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
  }

  @Test
  void getContentType_noExtension_returnsOctetStream() {
    assertThat(ContentTypeUtil.getContentType("filename")).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
  }

  @Test
  void getContentTypeByExtension_blank_returnsOctetStream() {
    assertThat(ContentTypeUtil.getContentTypeByExtension(null)).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(ContentTypeUtil.getContentTypeByExtension("")).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
  }

  @Test
  void getContentTypeByExtension_customMapped_returnsMappedMime() {
    assertThat(ContentTypeUtil.getContentTypeByExtension("docx"))
      .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    assertThat(ContentTypeUtil.getContentTypeByExtension("mp4")).isEqualTo("video/mp4");
    assertThat(ContentTypeUtil.getContentTypeByExtension("mp3")).isEqualTo("audio/mpeg");
    assertThat(ContentTypeUtil.getContentTypeByExtension("json")).isEqualTo("application/json");
    assertThat(ContentTypeUtil.getContentTypeByExtension("csv")).isEqualTo("text/csv");
    assertThat(ContentTypeUtil.getContentTypeByExtension("svg")).isEqualTo("image/svg+xml");
  }

  @Test
  void getContentTypeByExtension_unknownExtension_returnsOctetStream() {
    assertThat(ContentTypeUtil.getContentTypeByExtension("xyzunknown"))
      .isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
  }

  @Test
  void getContentTypeByExtension_uppercaseNormalizesToLower() {
    assertThat(ContentTypeUtil.getContentTypeByExtension("MP4")).isEqualTo("video/mp4");
  }

  @Test
  void getContentType_resolvesFromCustomMap() {
    assertThat(ContentTypeUtil.getContentType("report.mp4")).isEqualTo("video/mp4");
    assertThat(ContentTypeUtil.getContentType("song.mp3")).isEqualTo("audio/mpeg");
  }

  @Test
  void isImageFile_customMappedImage_returnsTrue() {
    assertThat(ContentTypeUtil.isImageFile("a.bmp")).isTrue();
    assertThat(ContentTypeUtil.isImageFile("a.svg")).isTrue();
    assertThat(ContentTypeUtil.isImageFile("a.doc")).isFalse();
  }

  @Test
  void isVideoFile_returnsTrueForVideo() {
    assertThat(ContentTypeUtil.isVideoFile("clip.mp4")).isTrue();
    assertThat(ContentTypeUtil.isVideoFile("clip.avi")).isTrue();
    assertThat(ContentTypeUtil.isVideoFile("a.doc")).isFalse();
  }

  @Test
  void isAudioFile_returnsTrueForAudio() {
    assertThat(ContentTypeUtil.isAudioFile("song.mp3")).isTrue();
    assertThat(ContentTypeUtil.isAudioFile("song.wav")).isTrue();
    assertThat(ContentTypeUtil.isAudioFile("a.doc")).isFalse();
  }

  @Test
  void isTextFile_includesJsonXmlAndText() {
    assertThat(ContentTypeUtil.isTextFile("a.json")).isTrue();
    assertThat(ContentTypeUtil.isTextFile("a.xml")).isTrue();
    assertThat(ContentTypeUtil.isTextFile("a.csv")).isTrue();
    assertThat(ContentTypeUtil.isTextFile("a.doc")).isFalse();
  }

  @Test
  void isOfficeDocument_checksOfficeExtensions() {
    assertThat(ContentTypeUtil.isOfficeDocument("a.doc")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.docx")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.xls")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.xlsx")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.ppt")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.pptx")).isTrue();
    assertThat(ContentTypeUtil.isOfficeDocument("a.pdf")).isFalse();
  }

  @Test
  void getSupportedExtensions_containsKnownExtensions() {
    assertThat(ContentTypeUtil.getSupportedExtensions()).contains("docx", "mp4", "mp3", "json", "svg");
  }
}
