package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交信息DTO
 * 迁移对应关系: Thrift struct CommitInfo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitInfoDTO {

  @Schema(description = "版本")
  private String version;

  @Schema(description = "基础版本")
  private String baseVersion;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "提交者")
  private String committedBy;

  @Schema(description = "提交时间")
  private Long committedAt;
}
