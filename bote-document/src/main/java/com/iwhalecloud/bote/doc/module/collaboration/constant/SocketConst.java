package com.iwhalecloud.bote.doc.module.collaboration.constant;

/**
 * socket涉及的常量
 */
public final class SocketConst {

  /**
   * socket server最大id,从0开始递增
   */
  public static final String CACHE_MAX_SERVER_ID = "socket:max_server_id";

  public static final String WEBSOCKET_PATH = "websocket.path";

  /**
   * Redis队列Key前缀
   */
  public static final String CHANGESET_QUEUE_KEY = "workbook:changeset:queue:";

}
