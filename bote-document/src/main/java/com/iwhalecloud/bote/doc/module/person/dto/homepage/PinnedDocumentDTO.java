package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.utils.PinLinkedSortHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 置顶文档
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "置顶文档")
public class PinnedDocumentDTO implements PinLinkedSortHelper.LinkedListNode<PinnedDocumentDTO> {
  @Schema(description = "置顶记录ID")
  private Long pinId;
  @Schema(description = "前一个置顶记录ID")
  private Long prevPinId;
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "文档类型")
  private String documentType;
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档库路径")
  private String libraryPath;
  @Schema(description = "文档库路径编码")
  private String libraryPathCode;
  @Schema(description = "创建人信息")
  private UserInfo creator;
  @Schema(description = "租户id")
  private String tenantId;
  @Schema(description = "置顶时间")
  private Date pinTime;
  @Schema(description = "权限")
  private String permissions;

  // 临时字段，用于Service层处理，不暴露给前端
  @Schema(hidden = true)
  private Long creatorUserId;
  @Schema(hidden = true)
  private String creatorUsername;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
}
