package com.iwhalecloud.bote.doc.module.collaboration.doc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * nodejs 协作服务订阅事件消息内容
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Getter
@Setter
@ToString
public class NodeSubEvent {

  /**
   * 文档ID
   */
  private String documentId;

  /**
   * subscribe/unsubscribe
   */
  private String action;

  /**
   * 用户ID
   */
  private Long userId;
}
