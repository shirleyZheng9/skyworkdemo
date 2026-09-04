package com.iwhalecloud.bote.doc.common.utils.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Base64;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.junit.jupiter.api.Test;

/**
 * {@link UploadingPicturesManager} 单元测试。
 *
 * <p>覆盖 {@code savePicture} 的全部分支：空内容、Base64 回退（参数缺失）、上传成功、
 * 上传返回空 URL 回退、上传异常回退；以及公共静态 {@code createFileFromBytes} 的临时文件写入。
 * 私有 {@code createPictureFileName}/{@code buildBase64DataUri} 经反射覆盖。
 * @Mock IDocumentAttachmentService；不启动 Spring 容器、不联网、不连 DB。</p>
 */
class UploadingPicturesManagerTest {

  // ---- savePicture ----

  @Test
  void savePicture_returnsEmpty_whenContentNullOrEmpty() {
    UploadingPicturesManager mgr = new UploadingPicturesManager(null, "doc1", 1L);
    assertThat(mgr.savePicture(null, PictureType.PNG, "img", 10, 10)).isEmpty();
    assertThat(mgr.savePicture(new byte[0], PictureType.PNG, "img", 10, 10)).isEmpty();
  }

  @Test
  void savePicture_returnsBase64_whenServiceOrUserIdOrDocumentIdMissing() {
    // service null
    UploadingPicturesManager mgrNoService = new UploadingPicturesManager(null, "doc1", 1L);
    String result = mgrNoService.savePicture(new byte[] {1, 2}, PictureType.PNG, "img", 10, 10);
    assertThat(result).startsWith("data:image/png;base64,");

    // userId null
    UploadingPicturesManager mgrNoUser = new UploadingPicturesManager(
      mock(IDocumentAttachmentService.class), "doc1", null);
    assertThat(mgrNoUser.savePicture(new byte[] {1}, PictureType.JPEG, "img", 10, 10))
      .startsWith("data:image/jpeg;base64,");

    // documentId blank
    UploadingPicturesManager mgrNoDoc = new UploadingPicturesManager(
      mock(IDocumentAttachmentService.class), "", 1L);
    assertThat(mgrNoDoc.savePicture(new byte[] {1}, PictureType.PNG, null, 10, 10))
      .startsWith("data:image/png;base64,");
  }

  @Test
  void savePicture_returnsUrl_whenUploadSucceeds() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl("/files/x.png");
    when(service.upload(any(), eq("doc1"), eq(1L))).thenReturn(dto);

    UploadingPicturesManager mgr = new UploadingPicturesManager(service, "doc1", 1L);
    String url = mgr.savePicture(new byte[] {1, 2, 3}, PictureType.PNG, "img", 10, 10);
    assertThat(url).isEqualTo("/files/x.png");
  }

  @Test
  void savePicture_fallsBackToBase64_whenUploadReturnsBlankUrl() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl(""); // blank
    when(service.upload(any(), anyString(), anyLong())).thenReturn(dto);

    UploadingPicturesManager mgr = new UploadingPicturesManager(service, "doc1", 1L);
    String url = mgr.savePicture(new byte[] {1, 2}, PictureType.PNG, "img", 10, 10);
    assertThat(url).startsWith("data:image/png;base64,");
  }

  @Test
  void savePicture_fallsBackToBase64_whenUploadThrows() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    when(service.upload(any(), anyString(), anyLong())).thenThrow(new RuntimeException("boom"));

    UploadingPicturesManager mgr = new UploadingPicturesManager(service, "doc1", 1L);
    String url = mgr.savePicture(new byte[] {1, 2}, PictureType.PNG, "img", 10, 10);
    assertThat(url).startsWith("data:image/png;base64,");
  }

  @Test
  void savePicture_doesNotCallService_whenBase64Fallback() {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    UploadingPicturesManager mgr = new UploadingPicturesManager(null, "doc1", 1L);
    mgr.savePicture(new byte[] {1}, PictureType.PNG, "img", 10, 10);
    verify(service, never()).upload(any(), anyString(), anyLong());
  }

  // ---- createFileFromBytes ----

  @Test
  void createFileFromBytes_writesContentToTempFile() throws IOException {
    byte[] content = "hello".getBytes();
    File file = UploadingPicturesManager.createFileFromBytes(content, "test.png");
    try {
      assertThat(file.exists()).isTrue();
      assertThat(file.getName()).startsWith("upload_").endsWith("_test.png");
      assertThat(Files.readAllBytes(file.toPath())).isEqualTo(content);
    }
    finally {
      Files.deleteIfExists(file.toPath());
    }
  }

  // ---- createPictureFileName (private, reflection) ----

  @Test
  void createPictureFileName_appendsExtensionWhenNotPresent() throws Exception {
    assertThat(invokeCreatePictureFileName("img", PictureType.PNG)).isEqualTo("img.png");
    assertThat(invokeCreatePictureFileName("img", PictureType.JPEG)).isEqualTo("img.jpg");
  }

  @Test
  void createPictureFileName_keepsBaseNameWhenAlreadyEndsWithExtension() throws Exception {
    assertThat(invokeCreatePictureFileName("img.png", PictureType.PNG)).isEqualTo("img.png");
    assertThat(invokeCreatePictureFileName("IMG.PNG", PictureType.PNG)).isEqualTo("IMG.PNG");
  }

  @Test
  void createPictureFileName_usesNanoFallbackAndDefaultPngWhenSuggestedNameBlank() throws Exception {
    String result = invokeCreatePictureFileName(null, PictureType.JPEG);
    assertThat(result).startsWith("doc_image_").endsWith(".jpg");

    // pictureType null -> 默认 png 扩展名
    String resultNoType = invokeCreatePictureFileName("img", null);
    assertThat(resultNoType).isEqualTo("img.png");

    String resultBothNull = invokeCreatePictureFileName(null, null);
    assertThat(resultBothNull).startsWith("doc_image_").endsWith(".png");
  }

  // ---- buildBase64DataUri (private, reflection) ----

  @Test
  void buildBase64DataUri_encodesWithMimeFromPictureType() throws Exception {
    byte[] content = new byte[] {1, 2, 3};
    String expectedPng = "data:image/png;base64," + Base64.getEncoder().encodeToString(content);
    assertThat(invokeBuildBase64DataUri(content, PictureType.PNG)).isEqualTo(expectedPng);

    String expectedJpeg = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(content);
    assertThat(invokeBuildBase64DataUri(content, PictureType.JPEG)).isEqualTo(expectedJpeg);
  }

  @Test
  void buildBase64DataUri_usesDefaultPngMime_whenPictureTypeNull() throws Exception {
    byte[] content = new byte[] {9};
    String expected = "data:image/png;base64," + Base64.getEncoder().encodeToString(content);
    assertThat(invokeBuildBase64DataUri(content, null)).isEqualTo(expected);
  }

  // ---- helpers ----

  private String invokeCreatePictureFileName(String suggestedName, PictureType pictureType) throws Exception {
    Method m = UploadingPicturesManager.class.getDeclaredMethod(
      "createPictureFileName", String.class, PictureType.class);
    m.setAccessible(true);
    return (String) m.invoke(new UploadingPicturesManager(null, "d", 1L), suggestedName, pictureType);
  }

  private String invokeBuildBase64DataUri(byte[] content, PictureType pictureType) throws Exception {
    Method m = UploadingPicturesManager.class.getDeclaredMethod(
      "buildBase64DataUri", byte[].class, PictureType.class);
    m.setAccessible(true);
    return (String) m.invoke(new UploadingPicturesManager(null, "d", 1L), content, pictureType);
  }
}
