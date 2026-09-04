package com.iwhalecloud.bote.dto.plugin.params;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.dto.plugin.AbstractPluginParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * SMTP邮件发送参数
 *
 * @author zhao.xu104
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class SmtpEmailPluginParams extends AbstractPluginParams {
  /** SMTP服务器地址 */
  private String host;
  /** SMTP端口 */
  private Integer port;
  /** 发件人邮箱 */
  private String username;
  /** 发件人密码 */
  private String password;
  /** 协议，默认smtp */
  private String protocol;
  /** 编码，默认UTF-8 */
  private String encoding;
  /** 收件人邮箱列表 */
  private List<String> to;
  /** 邮件主题 */
  private String subject;
  /** 邮件内容（支持HTML） */
  private String content;
  /** 附件文件ID（可选），如果HTML内容中使用 cid:文件ID 引用，则作为内嵌图片 */
  private List<Long> fileIds;

  public SmtpEmailPluginParams() {
    super(PluginConsts.PLUGIN_CODE_SMTP_EMAIL);
  }
}

