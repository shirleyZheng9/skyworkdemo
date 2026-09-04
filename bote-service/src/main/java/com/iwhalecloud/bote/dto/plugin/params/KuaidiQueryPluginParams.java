package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 查快递插件参数
 *
 * @author lizuyin
 * @since 2025-11-21
 */
@Getter
@Setter
@ToString
public class KuaidiQueryPluginParams extends AbstractPluginParams {
  /** 快递100客户编码 */
  private String customer;
  /** 密钥 */
  private String key;
  /** 快递公司编码 */
  private String com;
  /** 快递单号 */
  private String num;
  /** 收件人或寄件人手机号（可选） */
  private String phone;

  public KuaidiQueryPluginParams() {
    super(PluginConsts.PLUGIN_CODE_KUAIDI_QUERY);
  }
}

