package com.iwhalecloud.bote.config.properties;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WeKnora 知识库配置属性
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Component
@ConfigurationProperties("knowledge.weknora")
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@Validated
@Getter(AccessLevel.NONE)
@Setter
@ToString
public class WeKnoraProperties {

  /** WeKnora API 基础地址 */
  @URL
  private String apiUrl;
  /** 是否启用 WeKnora */
  private Boolean enabled;

  /** 租户配置，key 为租户 ID。可选，用于给个别租户配置不同的 WeKnora 接口地址 */
  @Valid
  private Map<Long, WeKnoraProperties> tenant = new HashMap<>();

  /** 登录接口地址 */
  private String loginApiUrl;
  /** 注册接口地址 */
  private String registerApiUrl;
  /** 知识库列表接口地址 */
  private String listKnowledgeBasesApiUrl;
  /** 创建会话接口地址 */
  private String createSessionApiUrl;
  /** 知识检索接口地址 */
  private String knowledgeSearchApiUrl;
  /** 模型列表接口地址 */
  private String listModelsApiUrl;
  /** 修改密码接口地址 */
  private String changePasswordApiUrl;
  /** 知识库详情接口地址 */
  private String knowledgeBaseApiUrl;

  /**
   * 获取租户的 WeKnora 配置
   */
  private WeKnoraProperties getProperties(@Nullable Long tenantId) {
    if (tenantId != null && tenant.containsKey(tenantId)) {
      WeKnoraProperties props = tenant.get(tenantId);
      Assert.hasLength(props.apiUrl, () -> "未配置 WeKnora 接口地址: tenantId=" + tenantId);
      return props;
    }
    Assert.hasLength(apiUrl, "未配置 WeKnora 接口地址");
    return this;
  }

  public String getApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).apiUrl;
  }

  public String getLoginApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).loginApiUrl;
  }

  public String getRegisterApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).registerApiUrl;
  }

  public String getListKnowledgeBasesApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).listKnowledgeBasesApiUrl;
  }

  public String getCreateSessionApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).createSessionApiUrl;
  }

  public String getKnowledgeSearchApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).knowledgeSearchApiUrl;
  }

  public String getListModelsApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).listModelsApiUrl;
  }

  /**
   * 构建动态 API URL（用于含路径变量的接口，如 /v1/knowledge-chat/{sessionId}）
   *
   * @param path      路径（以 "/" 开头）
   * @param tenantId  租户 ID，可为 null
   * @return 完整 URL
   */
  public String buildUrl(String path, @Nullable Long tenantId) {
    return getProperties(tenantId).apiUrl + path;
  }

  public String getChangePasswordApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).changePasswordApiUrl;
  }

  public String getKnowledgeBaseApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).knowledgeBaseApiUrl;
  }

  /**
   * 规范接口地址，并预先组装各子路径 URL
   */
  @PostConstruct
  public void normalizeUrl() {
    if (!tenant.isEmpty()) {
      for (WeKnoraProperties props : tenant.values()) {
        props.normalizeUrl();
      }
    }

    apiUrl = StringUtils.stripEnd(apiUrl, "/");
    if (StringUtils.isEmpty(apiUrl)) {
      return;
    }

    loginApiUrl = apiUrl + "/auth/login";
    registerApiUrl = apiUrl + "/auth/register";
    listKnowledgeBasesApiUrl = apiUrl + "/knowledge-bases";
    createSessionApiUrl = apiUrl + "/sessions";
    knowledgeSearchApiUrl = apiUrl + "/knowledge-search";
    listModelsApiUrl = apiUrl + "/models";
    changePasswordApiUrl = apiUrl + "/auth/change-password";
    knowledgeBaseApiUrl = apiUrl + "/knowledge-bases";
  }
}
