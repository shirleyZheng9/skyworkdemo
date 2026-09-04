package com.iwhalecloud.bote.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

/**
 * {@link FileUtil} 单元测试。
 *
 * <p>覆盖 getFileName 的 Content-Disposition 与 URL 回退分支，以及 getFileExtension 的
 * Content-Type、八进制流回退、未知 MIME 异常回退与文件名提取分支。</p>
 */
class FileUtilTest {

  @Test
  void getFileName_fromContentDisposition() {
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.txt");
    when(response.getHeaders()).thenReturn(headers);
    assertThat(FileUtil.getFileName(response, URI.create("https://h/x"))).isEqualTo("test.txt");
  }

  @Test
  void getFileName_emptyContentDisposition_fallsBackToUrl() {
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    when(response.getHeaders()).thenReturn(new HttpHeaders());
    assertThat(FileUtil.getFileName(response, URI.create("https://h/path/to/file.csv")))
      .isEqualTo("file.csv");
  }

  @Test
  void getFileName_nullContentDisposition_fallsBackToUrl() {
    ClientHttpResponse response = mock(ClientHttpResponse.class);
    HttpHeaders headers = new HttpHeaders();
    when(response.getHeaders()).thenReturn(headers);
    assertThat(FileUtil.getFileName(response, URI.create("https://h/a/b/report.pdf")))
      .isEqualTo("report.pdf");
  }

  @Test
  void getFileExtension_fromContentType() {
    assertThat(FileUtil.getFileExtension(MediaType.APPLICATION_PDF, null)).isEqualTo("pdf");
  }

  @Test
  void getFileExtension_octetStream_fallsBackToFilename() {
    assertThat(FileUtil.getFileExtension(MediaType.APPLICATION_OCTET_STREAM, "a.txt")).isEqualTo("txt");
  }

  @Test
  void getFileExtension_nullContentType_fallsBackToFilename() {
    assertThat(FileUtil.getFileExtension(null, "archive.zip")).isEqualTo("zip");
  }

  @Test
  void getFileExtension_unknownMime_returnsEmptyInsteadOfFilename() {
    // Tika 对未注册但合法的 MIME 不抛 MimeTypeException，而是返回空扩展名；
    // 因此不会回退到文件名（catch 分支仅作防御，无法经合法 MediaType 触发）。
    assertThat(FileUtil.getFileExtension(MediaType.valueOf("application/x-zzz-unknown"), "f.bak"))
      .isEmpty();
  }

  @Test
  void getFileExtension_octetStreamNoFilename_returnsNull() {
    assertThat(FileUtil.getFileExtension(MediaType.APPLICATION_OCTET_STREAM, null)).isNull();
  }
}
