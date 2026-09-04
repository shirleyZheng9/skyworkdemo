package com.iwhalecloud.bote.doc.common.utils;

import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.consts.IdRulePrefixEnum;
import com.iwhalecloud.bss.litchi.util.sequence.UUIDUtils;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * 文档中心设计唯一ID生成
 *
 * @author Aiqing
 * @since 2025/8/14
 */
public final class DcIdUtils {

  private DcIdUtils() {
  }

  /**
   * 获取目录的节点ID
   *
   * @return ID
   */
  public static String createFoldId() {
    return IdRulePrefixEnum.FOLD.getPrefix() + UUIDUtils.base58Uuid(UUID.randomUUID());
  }

  /**
   * 创建文档ID
   *
   * @param documentType 文档类型
   * @return 文档ID
   */
  public static String createDocumentId(DocumentTypeEnum documentType) {
    IdRulePrefixEnum prefix = switch (documentType) {
      case ROOT -> IdRulePrefixEnum.ROOT;
      case WORD, WORD_ONLINE, TXT, MD -> IdRulePrefixEnum.DOCUMENT;
      case EXCEL, EXCEL_ONLINE -> IdRulePrefixEnum.WORKBOOK;
      case FOLDER -> IdRulePrefixEnum.FOLD;
      case DIM_TABLE -> IdRulePrefixEnum.DIM_TABLE;
      default -> IdRulePrefixEnum.NODE;
    };
    return prefix.getPrefix() + UUIDUtils.base58Uuid(UUID.randomUUID());
  }

  /**
   * 根据文件名确定文档类型
   */
  public static String determineDocumentType(String fileName) {
    String extension = StringUtils.substringAfterLast(fileName, ".").toLowerCase();
    return switch (extension) {
      case "doc", "docx" -> DocumentTypeEnum.WORD.getCode();
      case "xls", "xlsx" -> DocumentTypeEnum.EXCEL.getCode();
      case "ppt", "pptx" -> DocumentTypeEnum.PPT.getCode();
      case "pdf" -> DocumentTypeEnum.PDF.getCode();
      case "txt" -> DocumentTypeEnum.TXT.getCode();
      case "md", "mdx" -> DocumentTypeEnum.MD.getCode();
      case "png", "jpg", "jpeg" -> DocumentTypeEnum.IMAGE.getCode();
      default -> DocumentTypeEnum.FILE.getCode();
    };
  }

  /**
   * 创建文档库ID
   *
   * @return 文档库ID
   */
  public static String createLibraryId() {
    return IdRulePrefixEnum.LIBRARY.getPrefix() + UUIDUtils.base58Uuid(UUID.randomUUID());
  }

  /**
   * 创建知识库ID
   *
   * @return 知识库ID
   */
  public static String createKbId() {
    return IdRulePrefixEnum.KNOWLEDGE_BASE.getPrefix() + UUIDUtils.base58Uuid(UUID.randomUUID());
  }

  /**
   * 创建批量任务ID
   *
   * @return 批量任务ID
   */
  public static String createBatchTaskId() {
    return "batch_build_" + UUIDUtils.base58Uuid(UUID.randomUUID());
  }
}
