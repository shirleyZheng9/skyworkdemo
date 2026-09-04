package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import com.iwhalecloud.bote.doc.common.validate.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档创建请求参数
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Getter
@Setter
@ToString
public class DocumentCreateRequestDTO extends TenantBaseRO {

  @Schema(description = "文档库ID, 留空为创建个人文档库文档", example = "lbr20")
  private String libraryId;

  @Schema(description = "父ID", example = "fod10")
  private String parentId;

  @Schema(description = "文档类型. WORD-文本文档，EXCEL-表格文档，FOLDER-文件夹, DIM_TABLE-多维表格", example = "WORD_ONLINE", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty(message = "文档类型不能为空")
  @EnumValue(values = {"WORD_ONLINE", "EXCEL_ONLINE", "FOLDER", "DIM_TABLE"}, message = "文档类型值不正确")
  private String documentType;

  @Schema(description = "文档名称（可选，用于指定文档名称，不传则自动生成）", example = "我的文档.docx")
  private String documentName;

  @Schema(description = "文件信息ID（可选，用于文件上传场景）", example = "123456789")
  private Long fileInfoId;

  @Schema(description = "是否转换为在线文档（可选，T-是，F-否，默认F）", example = "F")
  private String isConvert;

  @Schema(description = "前置文档ID（可选，用于指定文档顺序，null表示放在最前方）", example = "doc123456")
  private String prevDocumentId;

  @Schema(description = "在线网页地址")
  private String dataUrl;
}
