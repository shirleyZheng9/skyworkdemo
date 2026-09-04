package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能广场大批量异步导出任务状态（轮询）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "技能广场异步导出任务状态")
public class SkillSquareBulkExportJobStatusVO {

  @Schema(description = "任务 ID")
  private Long jobId;
  @Schema(description = "任务开始时间（毫秒时间戳，创建任务时写入）")
  private Long startTimeMillis;
  @Schema(description = "阶段")
  private Phase phase;
  @Schema(description = "总技能条数")
  private int totalRecords;
  @Schema(description = "分包总数")
  private int totalParts;
  @Schema(description = "已完成分包数（含上传）")
  private int completedParts;
  @Schema(description = "已写入导出包中的技能条数（随打包过程递增")
  private int processedRecords;
  @Schema(description = "失败原因，仅 FAILED 时有值")
  private String errorMessage;
  @Builder.Default
  @Schema(description = "已就绪的分包下载信息，RUNNING/SUCCESS 时逐步填充")
  private List<SkillSquareBulkExportPartVO> parts = new ArrayList<>();

  public enum Phase {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
  }
}
