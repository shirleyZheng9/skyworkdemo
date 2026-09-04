package com.iwhalecloud.bote.sms.bill.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 河南电信短信推送客户端配置
 *
 * @author wangtingyun
 * @since 2025-10-15
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "sms.client.bill")
public class BillClientProperties {

  /** 用户名 */
  private String username;
  /** 密码 */
  private String password;
  /** 所属系统 */
  private String sysCode;
  /** 产品ID */
  private String productId;
  /** 本地网id */
  private String latnId;
  /** 业务场境ID */
  private Long businessId;
  /** WebService 服务地址 */
  private String serviceUrl;
  /** wsdl资源位置 */
  private String wsdlLocation = "wsdl/BillDataServlet.wsdl";
  /** webservice连接超时时间：毫秒 */
  private long connectionTimeout = 30000;
  /** webservice接收回复超时时间：毫秒 */
  private long receiveTimeout = 60000;
  /** 应用ID  */
  private String appId;
  /** 应用密钥 */
  private String appKey;

}
