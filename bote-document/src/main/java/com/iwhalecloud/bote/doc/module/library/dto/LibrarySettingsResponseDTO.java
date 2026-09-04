package com.iwhalecloud.bote.doc.module.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "文档库设置响应")
public class LibrarySettingsResponseDTO {

  @Schema(description = "文档库ID")
  private String libraryId;

  @Schema(description = "受影响的成员ID")
  private Long affectedMemberId;

  @Schema(description = "操作类型描述")
  private String action;

  @Schema(description = "新的权限类型")
  private String newPermission;

  @Schema(description = "操作结果消息")
  private String message;
}
