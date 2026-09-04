package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 飞书插件参数抽象类
 *
 * @author qian.sisheng
 * @since 2025-08-22
 */
@Getter
@Setter
@ToString
public class AbstractLarkPluginParams extends AbstractPluginParams {
  /** 应用ID */
  private String appId;
  /** 应用密钥 */
  private String appSecret;
  /** 回调地址，用于getAuthUrl操作 */
  private String redirectUrl;
  /** 表ID */
  private String tableId;
  /** 多维表格 App 的唯一标识 */
  private String appToken;
  /** 多维表格的url */
  private String url;
  /** 插件名称 */
  private String pluginName;

  public AbstractLarkPluginParams(String pluginCode, String pluginName) {
    super(pluginCode);
    this.pluginName = pluginName;
  }
}
