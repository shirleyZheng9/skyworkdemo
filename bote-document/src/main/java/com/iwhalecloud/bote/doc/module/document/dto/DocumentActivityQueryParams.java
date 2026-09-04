package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.PageParams;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 文档动态记录查询参数
 *
 * @author Aiqing
 * @since 2025-08-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "文档动态记录查询参数")
public class DocumentActivityQueryParams extends PageParams {

  @Schema(description = "文档ID")
  private String documentId;

  @Schema(description = "文档库ID")
  private String libraryId;

  @Schema(description = "操作用户ID")
  private Long userId;

  @Schema(description = "操作类型：VIEW-查看，EDIT-编辑，DOWNLOAD-下载，DELETE-删除，CREATE-创建")
  private String actionType;

  @Schema(description = "开始时间")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startTime;

  @Schema(description = "结束时间")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime endTime;
}
