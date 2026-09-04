package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 光标变化的事件消息
 *
 * @author Aiqing
 * @since 2025/9/4
 */
@Getter
@Setter
@ToString
public class UpdateCursorEventDTO {

  /**
   * 事件ID：update_cursor
   */
  private String eventID = "update_cursor";

  /**
   * 事件数据
   */
  private EventData updateCursorEvent;


  @Getter
  @Setter
  @ToString
  public static class EventData {
    /**
     * unitID
     */
    private String unitID;
    /**
     * memberID
     */
    private String memberID;
    /**
     * selection
     */
    private String selection;
  }
}
