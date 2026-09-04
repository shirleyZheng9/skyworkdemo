package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 搜索文档
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "搜索文档")
public class SearchDocumentDTO {
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "文档名称")
  private String documentName;
  @Schema(description = "内容来源：ONLINE-在线文档，UPLOAD-上传文件")
  private String contentSource;
  @Schema(description = "文档类型")
  private String documentType;
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档库名称")
  private String libraryName;
  @Schema(description = "文档库路径")
  private String libraryPath;
  @Schema(description = "文档库路径编码")
  private String libraryPathCode;
  @Schema(description = "创建人id")
  private Long creatorId;
  @Schema(description = "创建人信息")
  private UserInfo creator;
  @Schema(description = "最后修改时间")
  private Date lastModifiedTime;
  @Schema(description = "预览地址")
  private String previewUrl;
  @Schema(description = "最近一次编辑信息")
  private DocumentActivityDTO editActivity;
  @Schema(description = "最近一次访问信息")
  private DocumentActivityDTO viewActivity;
  @Schema(description = "路径")
  private String documentPath;
  @Schema(description = "路径编码")
  private String documentPathCode;
  @Schema(description = "租户ID")
  protected Long tenantId;
  @Schema(description = "企业空间ID")
  protected Long spaceId;
  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;
}
