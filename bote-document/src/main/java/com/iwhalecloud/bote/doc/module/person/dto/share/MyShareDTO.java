package com.iwhalecloud.bote.doc.module.person.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 我共享的文档 DTO
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "我共享的文档")
public class MyShareDTO {
  /** 文档ID */
  private String documentId;
  /** 文档名称 */
  private String documentName;
  /** 文档类型 */
  private String documentType;
  /** 内容来源 */
  private String contentSource;
  /** 文件图标（暂不返回，可为null） */
  private String fileIcon;
  /** 文档库路径 */
  private String libraryPath;
  /** 文档库路径编码 */
  private String libraryPathCode;
  /** 文档库ID */
  private String libraryId;
  /** 文档库编码（暂不返回） */
  private String libraryCode;
  /** 最近共享时间 */
  private Date shareTime;
  /** 最近修改时间 */
  private Date lastModifiedTime;
  /** 我分享给的对象列表 */
  private List<SharedTargetDTO> sharedTo;
  /** 是否收藏 */
  private Boolean isFavorite;
  /** 是否置顶 */
  private Boolean isPinned;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  /** 以下为内部装配字段，不对外暴露 */
  @Schema(hidden = true)
  private String subjectType;
  @Schema(hidden = true)
  private Long subjectId;
  @Schema(hidden = true)
  private String permissionType;
  @Schema(hidden = true)
  private Date createdTime;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
}


