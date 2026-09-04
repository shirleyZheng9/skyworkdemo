package com.iwhalecloud.bote.common.enums;

/**
 * 沙箱模式
 *
 * @author bianjp
 * @since 2026-04-16
 */
public enum SandboxMode {
  /** 使用远程沙箱，需要单独部署沙箱服务 */
  REMOTE,
  /** 使用本地目录，适用于未部署沙箱的环境 */
  LOCAL,
  /** 使用客户端（适用于桌面客户端） */
  CLIENT
}
