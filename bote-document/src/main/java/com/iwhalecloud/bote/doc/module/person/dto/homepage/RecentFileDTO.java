package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 最近访问文件
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "最近访问文件")
public class RecentFileDTO {
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @Schema(description = "文档类型")
  private String documentType;
  @Schema(description = "文件图标")
  private String fileIcon;
  @Schema(description = "文档库路径")
  private String libraryPath;
  @Schema(description = "文档库路径编码")
  private String libraryPathCode;
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "创建人")
  private UserInfo creator;
  @Schema(description = "最后打开时间")
  private Date lastOpenTime;
  @Schema(description = "最后修改时间")
  private Date lastModifiedTime;
  @Schema(description = "是否收藏")
  private Boolean isFavorite;
  @Schema(description = "权限")
  private String permissions;

  // 临时字段，用于存储从数据库查询到的数据
  @Schema(hidden = true)
  private Long creatorUserId;
  @Schema(hidden = true)
  private String creatorUsername;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;

}
