package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iwhalecloud.bote.doc.common.support.serializer.ShortTimeDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 常用文档库
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "常用文档库")
public class FrequentLibraryDTO {
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档库名称")
  private String libraryName;
  @Schema(description = "文档库类型")
  private Integer libraryType;
  @Schema(description = "最后更新时间")
  @JsonSerialize(using = ShortTimeDateSerializer.class)
  private Date lastUpdateTime;
  @Schema(description = "最后更新人")
  private UserInfo lastUpdater;
  @Schema(description = "文档数量")
  private Integer documentCount;
  @Schema(description = "访问次数")
  private Integer accessCount;
  @Schema(description = "访问评分")
  private Double accessScore;

  @Schema(hidden = true)
  private Long lastUpdaterUserId;
  @Schema(hidden = true)
  private String lastUpdaterUsername;
  @Schema(hidden = true)
  private Date lastAccessTime;

  private String libraryIcon;
  private String color;

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;
  @Schema(description = "创建时间", hidden = true)
  private Date createdTime;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;

}
