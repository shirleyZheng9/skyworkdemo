package com.iwhalecloud.bote.common.consts;

import lombok.Getter;

/**
 * 日志事件常量
 *
 * @author tingyun.wang
 * @since 2025-07-18
 */
@Getter
public enum AccountEventTypeEnum {

  ADD_ACCOUNT("account", "ADD_ACCOUNT", "新增账号"),
  MOD_ACCOUNT("account", "MOD_ACCOUNT", "编辑账号"),
  DEL_ACCOUNT("account", "DEL_ACCOUNT", "删除账号"),
  ADD_PERMISSION("permission", "ADD_PERMISSION", "添加账号权限"),
  MOD_PERMISSION("permission", "MOD_PERMISSION", "修改账号权限"),
  DEL_PERMISSION("permission", "DEL_PERMISSION", "删除账号权限");

  private final String eventType;

  private final String eventCode;

  private final String eventDesc;

  AccountEventTypeEnum(String eventType, String eventCode, String eventDesc) {
    this.eventType = eventType;
    this.eventCode = eventCode;
    this.eventDesc = eventDesc;
  }

}
