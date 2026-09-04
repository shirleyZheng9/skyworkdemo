package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿锁定状态响应DTO
 *
 * @author Aiqing
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString
@Schema(description = "工作簿锁定状态")
public class WorkbookLockStatusDTO {

  @Schema(description = "文档ID")
  private String documentId;

  @Schema(description = "是否锁定：true-已锁定，false-未锁定")
  private Boolean locked;

  @Schema(description = "锁定用户ID")
  private Long lockUserId;

  @Schema(description = "锁定会话ID")
  private String lockSessionId;

  @Schema(description = "锁定用户名称")
  private String lockUserName;

  @Schema(description = "锁定时间")
  private Date lockTime;

  @Schema(description = "锁定过期时间")
  private Date expireTime;

  @Schema(description = "当前用户是否可以编辑：true-可编辑，false-不可编辑")
  private Boolean canEdit;

  @Schema(description = "文档当前版本号")
  private long revision;
}
