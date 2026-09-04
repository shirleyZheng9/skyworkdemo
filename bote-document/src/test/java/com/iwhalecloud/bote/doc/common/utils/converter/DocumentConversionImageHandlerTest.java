package com.iwhalecloud.bote.doc.common.utils.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import java.lang.reflect.Method;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.relationships.Relationship;
import org.junit.jupiter.api.Test;

/**
 * {@link DocumentConversionImageHandler} 单元测试。
 *
 * <p>覆盖 {@code handleImage} 全部分支：参数校验失败、非图片 BinaryPart、缓存命中、
 * 上传成功并缓存、上传异常、空字节数组；私有 {@code generateImageFileName}/
 * {@code extractRelId}/{@code getCachedUrl}/{@code validateInputs}/{@code extractImagePart}
 * 经反射覆盖。@Mock IDocumentAttachmentService + docx4j BinaryPart/Relationship。
 * 不启动 Spring 容器、不联网、不连 DB。</p>
 */
class DocumentConversionImageHandlerTest {

  // ---- handleImage 参数校验 ----

  @Test
  void handleImage_returnsNull_whenAttachmentServiceNull() throws Exception {
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(null, "doc1", 1L);
    assertThat(handler.handleImage(null, null, null)).isNull();
  }

  @Test
  void handleImage_returnsNull_whenUserIdNull() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", null);
    assertThat(handler.handleImage(null, null, null)).isNull();
  }

  @Test
  void handleImage_returnsNull_whenDocumentIdBlank() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "", 1L);
    assertThat(handler.handleImage(null, null, null)).isNull();

    DocumentConversionImageHandler handler2 = new DocumentConversionImageHandler(service, null, 1L);
    assertThat(handler2.handleImage(null, null, null)).isNull();
  }

  // ---- handleImage 非图片 BinaryPart ----

  @Test
  void handleImage_returnsNull_whenBinaryPartNotImage() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPart plainPart = mock(BinaryPart.class);
    assertThat(handler.handleImage(null, null, plainPart)).isNull();
    verify(service, never()).upload(any(), any(), any());
  }

  // ---- handleImage 上传成功并缓存 ----

  @Test
  void handleImage_returnsUrlAndCaches_onSuccessfulUpload() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl("/files/img.png");
    when(service.upload(any(), eq("doc1"), eq(1L))).thenReturn(dto);

    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getBytes()).thenReturn(new byte[] {1, 2, 3});
    when(imagePart.getContentType()).thenReturn("image/png");
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId1");

    String url = handler.handleImage(null, rel, imagePart);
    assertThat(url).isEqualTo("/files/img.png");

    // 第二次调用同一 relId 应命中缓存，不再调 service
    String cached = handler.handleImage(null, rel, imagePart);
    assertThat(cached).isEqualTo("/files/img.png");
    verify(service, times(1)).upload(any(), eq("doc1"), eq(1L));
  }

  @Test
  void handleImage_returnsNull_whenImageBytesEmpty() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getBytes()).thenReturn(new byte[0]);
    when(imagePart.getContentType()).thenReturn("image/png");
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId1");

    assertThat(handler.handleImage(null, rel, imagePart)).isNull();
    verify(service, never()).upload(any(), any(), any());
  }

  @Test
  void handleImage_returnsNull_whenImageBytesNull() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getBytes()).thenReturn(null);
    when(imagePart.getContentType()).thenReturn("image/png");
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId1");

    assertThat(handler.handleImage(null, rel, imagePart)).isNull();
    verify(service, never()).upload(any(), any(), any());
  }

  @Test
  void handleImage_returnsNull_whenServiceReturnsBlankUrl() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentAttachmentDTO dto = new DocumentAttachmentDTO();
    dto.setUrl("");
    when(service.upload(any(), eq("doc1"), eq(1L))).thenReturn(dto);

    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getBytes()).thenReturn(new byte[] {1});
    when(imagePart.getContentType()).thenReturn("image/png");
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId1");

    assertThat(handler.handleImage(null, rel, imagePart)).isNull();
  }

  @Test
  void handleImage_returnsNull_whenServiceThrows() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    when(service.upload(any(), eq("doc1"), eq(1L))).thenThrow(new RuntimeException("boom"));

    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getBytes()).thenReturn(new byte[] {1});
    when(imagePart.getContentType()).thenReturn("image/png");
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId1");

    assertThat(handler.handleImage(null, rel, imagePart)).isNull();
  }

  // ---- generateImageFileName (private, reflection) ----

  @Test
  void generateImageFileName_mapsContentTypeToExtension() throws Exception {
    assertThat(invokeGenerateImageFileName("r1", "image/jpeg")).isEqualTo("image_r1.jpg");
    assertThat(invokeGenerateImageFileName("r1", "image/jpg")).isEqualTo("image_r1.jpg");
    assertThat(invokeGenerateImageFileName("r1", "image/png")).isEqualTo("image_r1.png");
    assertThat(invokeGenerateImageFileName("r1", "image/gif")).isEqualTo("image_r1.gif");
    assertThat(invokeGenerateImageFileName("r1", "image/bmp")).isEqualTo("image_r1.bmp");
    // 未知 contentType -> 默认 png
    assertThat(invokeGenerateImageFileName("r1", "image/tiff")).isEqualTo("image_r1.png");
    // null contentType -> 默认 png
    assertThat(invokeGenerateImageFileName("r1", null)).isEqualTo("image_r1.png");
  }

  // ---- extractRelId (private, reflection) ----

  @Test
  void extractRelId_returnsRelationshipId_whenRelationshipNotNull() throws Exception {
    Relationship rel = mock(Relationship.class);
    when(rel.getId()).thenReturn("rId9");
    assertThat(invokeExtractRelId(rel, null)).isEqualTo("rId9");
  }

  @Test
  void extractRelId_fallsBackToPartName_whenRelationshipNull() throws Exception {
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    org.docx4j.openpackaging.parts.PartName partName = mock(org.docx4j.openpackaging.parts.PartName.class);
    when(partName.getName()).thenReturn("/word/media/image1.png");
    when(imagePart.getPartName()).thenReturn(partName);
    assertThat(invokeExtractRelId(null, imagePart)).isEqualTo("/word/media/image1.png");
  }

  // ---- getCachedUrl / validateInputs / extractImagePart (private, reflection) ----

  @Test
  void getCachedUrl_returnsNull_whenRelIdNullOrNotCached() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    assertThat(invokeGetCachedUrl(handler, null)).isNull();
    assertThat(invokeGetCachedUrl(handler, "absent")).isNull();
  }

  @Test
  void validateInputs_returnsFalseWhenAnyMissing_andTrueWhenAllPresent() throws Exception {
    assertThat(invokeValidateInputs(new DocumentConversionImageHandler(null, "doc1", 1L))).isFalse();
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    assertThat(invokeValidateInputs(new DocumentConversionImageHandler(service, null, 1L))).isFalse();
    assertThat(invokeValidateInputs(new DocumentConversionImageHandler(service, "doc1", null))).isFalse();
    assertThat(invokeValidateInputs(new DocumentConversionImageHandler(service, "  ", 1L))).isFalse();
    assertThat(invokeValidateInputs(new DocumentConversionImageHandler(service, "doc1", 1L))).isTrue();
  }

  @Test
  void extractImagePart_returnsItselfForImagePart_andNullOtherwise() throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "doc1", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    assertThat(invokeExtractImagePart(handler, imagePart)).isSameAs(imagePart);
    BinaryPart plainPart = mock(BinaryPart.class);
    assertThat(invokeExtractImagePart(handler, plainPart)).isNull();
  }

  // ---- helpers ----

  private String invokeGenerateImageFileName(String relId, String contentType) throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "d", 1L);
    BinaryPartAbstractImage imagePart = mock(BinaryPartAbstractImage.class);
    when(imagePart.getContentType()).thenReturn(contentType);
    Method m = DocumentConversionImageHandler.class.getDeclaredMethod(
      "generateImageFileName", String.class, BinaryPartAbstractImage.class);
    m.setAccessible(true);
    return (String) m.invoke(handler, relId, imagePart);
  }

  private String invokeExtractRelId(Relationship rel, BinaryPart binaryPart) throws Exception {
    IDocumentAttachmentService service = mock(IDocumentAttachmentService.class);
    DocumentConversionImageHandler handler = new DocumentConversionImageHandler(service, "d", 1L);
    Method m = DocumentConversionImageHandler.class.getDeclaredMethod(
      "extractRelId", Relationship.class, BinaryPart.class);
    m.setAccessible(true);
    return (String) m.invoke(handler, rel, binaryPart);
  }

  private String invokeGetCachedUrl(DocumentConversionImageHandler handler, String relId) throws Exception {
    Method m = DocumentConversionImageHandler.class.getDeclaredMethod("getCachedUrl", String.class);
    m.setAccessible(true);
    return (String) m.invoke(handler, relId);
  }

  private boolean invokeValidateInputs(DocumentConversionImageHandler handler) throws Exception {
    Method m = DocumentConversionImageHandler.class.getDeclaredMethod("validateInputs");
    m.setAccessible(true);
    return (boolean) m.invoke(handler);
  }

  private BinaryPartAbstractImage invokeExtractImagePart(DocumentConversionImageHandler handler, BinaryPart part)
    throws Exception {
    Method m = DocumentConversionImageHandler.class.getDeclaredMethod("extractImagePart", BinaryPart.class);
    m.setAccessible(true);
    return (BinaryPartAbstractImage) m.invoke(handler, part);
  }
}
