package com.iwhalecloud.bote.doc.module.control.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "文档库全局设置")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LibraryGlobalFeature {

  @Schema(description = "邀请协作者的权限等级", example = "2")
  private Boolean inviteLevel;

  @Schema(description = "评论的最低权限等级", example = "2")
  private Boolean commentLevel;

  @Schema(description = "是否启用水印", example = "false")
  private Boolean watermarkEnable;

  @Schema(description = "是否允许复制", example = "true")
  private Boolean allowCopyDataToExternal;
}
