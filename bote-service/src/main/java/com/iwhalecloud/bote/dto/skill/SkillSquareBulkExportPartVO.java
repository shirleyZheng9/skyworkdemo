package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大批量异步导出任务中的单个分包信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技能广场异步导出分包")
public class SkillSquareBulkExportPartVO {

  @Schema(description = "分包序号，从 1 开始")
  private int partIndex;

  @Schema(description = "该分包包含的技能条数")
  private int recordCount;

  @Schema(description = "平台文件 ID，用于 file/download")
  private Long fileId;

  @Schema(description = "相对下载路径，形如 bote/file/download?fileId=xxx")
  private String downloadPath;
}
