package com.iwhalecloud.bote.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信对接配置
 *
 * @author lizuyin
 * @since 2025-08-12
 */
@Component
@ConfigurationProperties("bote.publish")
@Validated
@Getter
@Setter
@ToString
public class PublishProperties {

  /** 微信API基础地址 */
  @NotBlank
  private String apiUrl = "https://api.weixin.qq.com";
  /** 发送客服消息接口地址 */
  private String sendCustomMessageApiUrl;
  /** 获取微信AccessToken接口地址 */
  private String getAccessTokenApiUrl;
  /** MP相关配置 - 微信 */
  private MpConfig mp = new MpConfig();

  /**
   * 初始化接口地址
   */
  @PostConstruct
  public void initApiUrls() {
    this.sendCustomMessageApiUrl = this.apiUrl + "/cgi-bin/message/custom/send";
    // 优先使用发布配置中的MP服务器地址，其次使用默认地址
    if (this.getAccessTokenApiUrl == null) {
      this.getAccessTokenApiUrl = getMp().getServerUrl() != null ? getMp().getServerUrl() :
        this.apiUrl + "/cgi-bin/token";
    }
  }

  /**
   * MP配置 - 微信
   */
  @Getter
  @Setter
  @ToString
  public static class MpConfig {
    /** MP服务器地址 */
    private String serverUrl;
  }
}
