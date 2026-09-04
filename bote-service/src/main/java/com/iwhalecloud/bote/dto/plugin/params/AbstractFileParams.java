package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件参数抽象类
 * 定义插件参数类必须提供的文件相关字段
 *
 * @author zhao.xu104
 * @since 2025-11-28
 */
@Getter
@Setter
public abstract class AbstractFileParams extends AbstractPluginParams {
  /** 文件ID */
  private Long fileId;
  /** 文件地址 */
  private String fileUrl;

  public AbstractFileParams(String pluginCode) {
    super(pluginCode);
  }
}
