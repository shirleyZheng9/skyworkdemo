package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GenericCmdType {

  /**
   * 心跳
   */
  HEART_BEAT(1, "心跳"),
  /**
   * 系统消息
   */
  SYSTEM_MESSAGE(2, "系统消息"),

  TIPTAP_COLLAB_SUB(1001, "文档协作-订阅"),
  TIPTAP_COLLAB_PUB(1002, "文档协作-发布");


  private final Integer code;

  private final String desc;


  public static GenericCmdType fromCode(Integer code) {
    for (GenericCmdType typeEnum : values()) {
      if (typeEnum.code.equals(code)) {
        return typeEnum;
      }
    }
    return null;
  }
}

