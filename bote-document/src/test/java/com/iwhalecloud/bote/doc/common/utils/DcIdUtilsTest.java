package com.iwhalecloud.bote.doc.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * {@link DcIdUtils} 单元测试。
 *
 * <p>UUIDUtils.base58Uuid 以 mockStatic 注入固定后缀，使 ID 生成确定可断言；
 * determineDocumentType 为纯逻辑直接覆盖各扩展名分支。</p>
 */
class DcIdUtilsTest {

  private static final String FIXED_SUFFIX = "XYZ12345";

  private static MockedStatic<UUIDUtils> mockBase58() {
    MockedStatic<UUIDUtils> uuid = mockStatic(UUIDUtils.class);
    uuid.when(() -> UUIDUtils.base58Uuid(any(UUID.class))).thenReturn(FIXED_SUFFIX);
    return uuid;
  }

  @Test
  void createFoldId_usesFoldPrefix() {
    try (MockedStatic<UUIDUtils> u = mockBase58()) {
      assertThat(DcIdUtils.createFoldId()).isEqualTo("fod" + FIXED_SUFFIX);
    }
  }

  @Test
  void createDocumentId_mapsTypeToPrefix() {
    try (MockedStatic<UUIDUtils> u = mockBase58()) {
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.ROOT)).isEqualTo("rot" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.WORD)).isEqualTo("doc" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.WORD_ONLINE)).isEqualTo("doc" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.TXT)).isEqualTo("doc" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.MD)).isEqualTo("doc" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.EXCEL)).isEqualTo("wkb" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.EXCEL_ONLINE)).isEqualTo("wkb" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.FOLDER)).isEqualTo("fod" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.DIM_TABLE)).isEqualTo("dtb" + FIXED_SUFFIX);
      // PPT/PDF/IMAGE/FILE 未在 switch 显式列出，走 default -> NODE
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.PPT)).isEqualTo("nod" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.PDF)).isEqualTo("nod" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.IMAGE)).isEqualTo("nod" + FIXED_SUFFIX);
      assertThat(DcIdUtils.createDocumentId(DocumentTypeEnum.FILE)).isEqualTo("nod" + FIXED_SUFFIX);
    }
  }

  @Test
  void determineDocumentType_mapsExtensionToCode() {
    assertThat(DcIdUtils.determineDocumentType("a.doc")).isEqualTo("WORD");
    assertThat(DcIdUtils.determineDocumentType("a.docx")).isEqualTo("WORD");
    assertThat(DcIdUtils.determineDocumentType("b.xls")).isEqualTo("EXCEL");
    assertThat(DcIdUtils.determineDocumentType("b.xlsx")).isEqualTo("EXCEL");
    assertThat(DcIdUtils.determineDocumentType("c.ppt")).isEqualTo("PPT");
    assertThat(DcIdUtils.determineDocumentType("c.pptx")).isEqualTo("PPT");
    assertThat(DcIdUtils.determineDocumentType("d.pdf")).isEqualTo("PDF");
    assertThat(DcIdUtils.determineDocumentType("e.txt")).isEqualTo("TXT");
    assertThat(DcIdUtils.determineDocumentType("f.md")).isEqualTo("MD");
    assertThat(DcIdUtils.determineDocumentType("f.mdx")).isEqualTo("MD");
    assertThat(DcIdUtils.determineDocumentType("g.png")).isEqualTo("IMAGE");
    assertThat(DcIdUtils.determineDocumentType("g.jpg")).isEqualTo("IMAGE");
    assertThat(DcIdUtils.determineDocumentType("g.jpeg")).isEqualTo("IMAGE");
    assertThat(DcIdUtils.determineDocumentType("h.unknown")).isEqualTo("FILE");
    assertThat(DcIdUtils.determineDocumentType("noext")).isEqualTo("FILE");
    assertThat(DcIdUtils.determineDocumentType("UPPER.PDF")).isEqualTo("PDF");
  }

  @Test
  void createLibraryId_usesLibraryPrefix() {
    try (MockedStatic<UUIDUtils> u = mockBase58()) {
      assertThat(DcIdUtils.createLibraryId()).isEqualTo("lbr" + FIXED_SUFFIX);
    }
  }

  @Test
  void createKbId_usesKbPrefix() {
    try (MockedStatic<UUIDUtils> u = mockBase58()) {
      assertThat(DcIdUtils.createKbId()).isEqualTo("klb" + FIXED_SUFFIX);
    }
  }

  @Test
  void createBatchTaskId_usesBatchBuildPrefix() {
    try (MockedStatic<UUIDUtils> u = mockBase58()) {
      assertThat(DcIdUtils.createBatchTaskId()).isEqualTo("batch_build_" + FIXED_SUFFIX);
    }
  }
}
