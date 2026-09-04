package com.iwhalecloud.bote.doc.module.document.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档历史记录清理统计信息DTO
 *
 * @author lizuyin
 * @since 2025-10-16
 */
@Getter
@Setter
@ToString
public class DocHistCleanupStatsDTO {

  /** 删除的文档内容历史记录数 */
  private Integer docContentDeleted = 0;
  /** 删除的工作簿内容历史记录数 */
  private Integer workbookContentDeleted = 0;
  /** 删除的导出快照记录数 */
  private Integer exportSnapshotsDeleted = 0;
  /** 删除的文件数 */
  private Integer filesDeleted = 0;

  /** 累加统计信息 *
   * @param other 另一个统计信息对象
   */
  public void add(DocHistCleanupStatsDTO other) {
    this.docContentDeleted += other.docContentDeleted;
    this.workbookContentDeleted += other.workbookContentDeleted;
    this.exportSnapshotsDeleted += other.exportSnapshotsDeleted;
    this.filesDeleted += other.filesDeleted;
  }
}
