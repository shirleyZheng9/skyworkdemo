package com.iwhalecloud.bote.doc.module.collaboration.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档历史信息
 *
 * @author Aiqing
 * @since 2025/8/26
 */
@Getter
@Setter
@ToString
public class DocumentHistoryVO {
  @Schema(description = "文档库ID")
  private String libraryId;
  @Schema(description = "文档ID")
  private String documentId;
  @Schema(description = "文档内容")
  private String content;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "更新时间")
  private Date updatedTime;
}
