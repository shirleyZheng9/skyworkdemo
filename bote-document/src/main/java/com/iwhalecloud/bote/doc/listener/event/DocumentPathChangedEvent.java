package com.iwhalecloud.bote.doc.listener.event;

import com.iwhalecloud.bss.litchi.disruptor.DisruptorObject;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档路径变更事件
 *
 * <p>当文档库名称修改、文档移动位置、文档重命名等操作发生时触发此事件，
 * 用于清除相关的路径缓存。</p>
 *
 * @author Aiqing
 * @since 2025/1/15
 */
@Getter
@Setter
@ToString
public class DocumentPathChangedEvent implements DisruptorObject {

  /**
   * 事件类型
   */
  private PathChangeType changeType;

  /**
   * 文档库ID
   */
  private String libraryId;

  /**
   * 受影响的文档ID列表
   * 当文档库名称修改时，此列表包含该文档库下的所有文档ID
   * 当文档移动时，此列表包含移动的文档及其子文档的ID
   */
  private List<String> affectedDocumentIds;

  /**
   * 变更原因描述
   */
  private String reason;

  /**
   * 操作人ID
   */
  private Long operatorId;

  /**
   * 租户ID
   */
  private Long tenantId;

  /**
   * 空间ID
   */
  private Long spaceId;


  /**
   * 路径变更类型枚举
   */
  public enum PathChangeType {
    /**
     * 文档库名称修改
     */
    LIBRARY_NAME_CHANGED,

    /**
     * 文档库删除
     */
    LIBRARY_DELETED,


    /**
     * 文档移动位置
     */
    DOCUMENT_MOVED,

    /**
     * 文档重命名
     */
    DOCUMENT_RENAMED,

    /**
     * 文档删除
     */
    DOCUMENT_DELETED,

  }
}
