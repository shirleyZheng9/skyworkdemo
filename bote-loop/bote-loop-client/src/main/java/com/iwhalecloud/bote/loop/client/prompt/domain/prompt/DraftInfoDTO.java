package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 草稿信息DTO
 * 迁移对应关系: Thrift struct DraftInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftInfoDTO {

  @Schema(description = "用户ID")
  private String userId;

  @Schema(description = "基础版本")
  private String baseVersion;

  @Schema(description = "是否已修改")
  private Boolean isModified;

  @Schema(description = "创建时间")
  private Long createdAt;

  @Schema(description = "更新时间")
  private Long updatedAt;
}
