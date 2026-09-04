package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.module.document.entity.WorkbookContentHistoryEntity;

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
public class WorkbookContentHistoryDTO extends WorkbookContentHistoryEntity {

  @Schema(description = "创建人名称（bt_user.real_name）")
  private String userName;

  @Schema(description = "内容")
  private String content;
}
