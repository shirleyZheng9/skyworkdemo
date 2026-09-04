package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 平台版本信息
 *
 * @author bianjp
 * @since 2025-08-16
 */
@Getter
@Setter
@ToString
@Schema(description = "平台版本信息")
public class PlatformVersionInfoDTO {
  @Schema(description = "构建分支")
  private String branch;
  @Schema(description = "提交记录")
  private String commitId;
  @Schema(description = "提交时间")
  private String commitTime;
  @Schema(description = "构建主机")
  private String buildHost;
  @Schema(description = "构建时间")
  private String buildTime;
}
