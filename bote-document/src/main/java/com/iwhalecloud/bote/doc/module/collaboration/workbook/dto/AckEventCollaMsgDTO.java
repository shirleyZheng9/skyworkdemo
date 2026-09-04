package com.iwhalecloud.bote.doc.module.collaboration.workbook.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 协作消息数据
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Getter
@Setter
@ToString
public class AckEventCollaMsgDTO {

  /**
   * 变更事件ID， 如changeset_ack
   */
  private String eventID = "changeset_ack";
  /**
   * 表格变更
   */
  private AckEvent csAckEvent;

  @Getter
  @Setter
  @ToString
  public static class AckEvent {
    private ChangesetDTO cs;
  }
}
