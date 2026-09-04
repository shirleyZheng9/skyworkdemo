package com.iwhalecloud.bote.doc.module.collaboration.socket.ws.univer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UniverCmdType {

  /**
   * 心跳
   */
  HEART_BEAT(1, "心跳"),
  /**
   * 系统消息
   */
  JOIN_ROOM(2, "加入协作房间"),

  CURSOR_CHANGE(4, "光标变更"),

  SYNC_MEMBER(5, "同步房间成员"),
  CHANGESET(6, "数据变更");


  private final Integer code;
  private final String desc;

  public static UniverCmdType fromCode(Integer code) {
    for (UniverCmdType typeEnum : values()) {
      if (typeEnum.code.equals(code)) {
        return typeEnum;
      }
    }
    return null;
  }
}

