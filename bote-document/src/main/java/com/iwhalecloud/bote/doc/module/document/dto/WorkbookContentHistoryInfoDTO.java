package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工作簿内容历史表
 *
 * @author system
 * @since 2025-09-26
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(hidden = true)
public class WorkbookContentHistoryInfoDTO {

  @Schema(description = "分组名称：当天为「今天」，其余为 uuuu-MM-dd")
  private String revisionName;

  @Schema(description = "该分组下的工作簿内容历史明细，组内按版本号倒序")
  private List<WorkbookContentHistoryDTO> data;
}
