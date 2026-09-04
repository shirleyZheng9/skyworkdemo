package com.iwhalecloud.bote.config.properties;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * knowledgeGraph 对接配置（服务地址、SSO 加密密钥等不进租户设置缓存）
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Component
@ConfigurationProperties("knowledge.knowledge-graph")
@ConditionalOnBooleanProperty("knowledge.knowledgeGraph.enabled")
@Validated
@Setter
@Getter
public class KnowledgeGraphProperties {

  /** 是否启用 */
  private Boolean enabled;
  /** 服务根地址（不含末尾 /） */
  @URL
  @NotBlank
  private String apiUrl;
  /** SSO ticket 的 AES 密钥（可与平台 AES 配合存储为密文，见业务侧解密逻辑） */
  @NotBlank
  private String secret;
  /** SSO cookie 名称 */
  private String cookieName = "token";
  /** cookie失效时间 （秒） */
  private int cookieMaxAge = 24 * 60 * 60;

  /** 知识库列表 API 地址 */
  private String knowledgeBaseApiUrl = "/api/list_task";
  /** 知识检索 API 地址 */
  private String knowledgeRetrievalApiUrl = "/api/retrieve";
  /** 创建会话 API 地址 */
  private String createSessionApiUrl = "/api/create_session";
  /** 知识问答 API 地址 */
  private String knowledgeChatApiUrl = "/ws/complete";
  /** 登录接口 API 地址 */
  private String loginApiUrl = "/api/auth/sso/login";

  @PostConstruct
  void normalizeUrl() {
    if (StringUtils.isBlank(this.apiUrl)) {
      return;
    }
    this.apiUrl = StringUtils.stripEnd(this.apiUrl.trim(), "/");
    this.knowledgeBaseApiUrl = this.apiUrl + knowledgeBaseApiUrl;
    this.knowledgeRetrievalApiUrl = this.apiUrl + knowledgeRetrievalApiUrl;
    this.createSessionApiUrl = this.apiUrl + createSessionApiUrl;
    this.knowledgeChatApiUrl = this.apiUrl + knowledgeChatApiUrl;
    this.loginApiUrl = this.apiUrl + loginApiUrl;
  }
}
