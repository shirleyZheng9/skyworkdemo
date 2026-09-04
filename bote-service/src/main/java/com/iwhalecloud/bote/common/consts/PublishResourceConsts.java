package com.iwhalecloud.bote.common.consts;

import java.util.Collections;
import java.util.List;

/**
 * 发布资源常量类
 *
 * @author lizuyin
 * @since 2025-07-24
 */
public final class PublishResourceConsts {
  /** 系统编码 - 百应 */
  public static final String SYSTEM_CODE_BYAI = "BOT";
  /** 服务模式 - 远程 */
  public static final String HOST_TYPE_HOSTED = "hosted";
  /** 默认标签 */
  public static final List<String> DEFAULT_TAGS = Collections.singletonList("实用工具");
  /** 默认超时时间(毫秒) */
  public static final Integer DEFAULT_MCP_TIMEOUT = 60;
  /** 默认传输类型 */
  public static final String TRANSFER_TYPE = "RESTfull";
  /** 资源类型 - 智能应用 */
  public static final String RESOURCE_TYPE_BOT = "BOT";
  /** 资源类型 - 智能体 */
  public static final String RESOURCE_TYPE_SCENE = "SCENE";

  /** boteclaw 类型的发布资源 */
  public static final String BOTECLAW_EXT_RESOURCE_ID = "-2";

  private PublishResourceConsts() {
      // 私有构造函数，防止实例化
  }
}
