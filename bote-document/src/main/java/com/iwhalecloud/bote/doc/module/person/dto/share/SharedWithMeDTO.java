package com.iwhalecloud.bote.doc.module.person.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 与我共享的文档 DTO
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "与我共享的文档")
public class SharedWithMeDTO {
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
  /** 授权人用户ID（内部使用，不对外暴露） */
  @Schema(hidden = true)
  private Long sharerUserId;
  /** 共享者 */
  private UserInfo sharer;
  /** 最近共享时间 */
  private Date shareTime;
  /** 最近修改时间 */
  private Date lastModifiedTime;
  /** 权限（对我授予的权限） */
  private String permissions;
  /** 是否收藏 */
  private Boolean isFavorite;
  /** 是否固定 */
  private Boolean isPinned;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
}


