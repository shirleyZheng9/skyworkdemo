package com.iwhalecloud.bote.config.properties;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 百应对接配置
 *
 * @author bianjp
 * @since 2025-07-24
 */
@Component
@ConfigurationProperties("beyond")
@ConditionalOnBooleanProperty("beyond.enabled")
@Validated
@Getter
@Setter
@ToString
public class BeyondProperties {
  /** jwt 密钥，由百应提供，鉴权使用 */
  @NotEmpty
  private String jwtKey;
  /** 接口地址 */
  @NotEmpty
  @URL
  private String apiUrl;
  /** 接口地址(百应知识库相关接口地址,不配置时默认取百应接口地址) */
  @URL
  private String knowledgeApiUrl;
  // 自动构造
  /** 知识问答接口地址 */
  private HttpUrl knowledgeChatApiUrl;
  /** 知识检索接口地址 */
  private String knowledgeRecallApiUrl;
  /** 查询知识库列表接口地址 */
  private String queryKnowledgeListApiUrl;
  /** 目录检索接口地址 */
  private String catalogTreeApiUrl;
  /** 组织检索接口地址 */
  private String orgTreeApiUrl;
  /** 组织管理员检索接口地址 */
  private String orgAdminApiUrl;
  /** 资源发布接口地址 */
  private String resourcePublishApiUrl;
  /** 数字员工-BOT发布接口地址 */
  private String publishEmployeeApiUrl;
  /** 获取组织详情接口地址 */
  private String orgDetailApiUrl;


  /**
   * 规范接口地址
   */
  @PostConstruct
  public void normalizeUrl() {
    this.apiUrl = StringUtils.stripEnd(this.apiUrl, "/");
    if (StringUtils.isEmpty(this.knowledgeApiUrl)) {
      this.knowledgeApiUrl = this.apiUrl;
    }
    else {
      this.knowledgeApiUrl = StringUtils.stripEnd(this.knowledgeApiUrl, "/");
    }
    this.knowledgeChatApiUrl = HttpUrl.parse(this.knowledgeApiUrl + "/conversationService/openapi/chat/qa");
    this.knowledgeRecallApiUrl = this.knowledgeApiUrl + "/conversationService/open/api/knowledgeRetrieve";
    this.queryKnowledgeListApiUrl = this.knowledgeApiUrl + "/aiFactoryServer/api/v1/getUserAuthResource";
    this.catalogTreeApiUrl = this.apiUrl + "/aiFactoryServer/api/v1/queryCatalogTree";
    this.orgTreeApiUrl = this.apiUrl + "/aiFactoryServer/api/v1/getOrgTree";
    this.orgAdminApiUrl = this.apiUrl + "/aiFactoryServer/api/v1/getPublishByOrgId";
    this.orgDetailApiUrl = this.apiUrl + "/aiFactoryServer/open/api/qryOrgById";
    this.resourcePublishApiUrl = this.apiUrl + "/aiFactoryServer/api/v1/resource/publishResource";
    this.publishEmployeeApiUrl = this.apiUrl + "/aiFactoryServer/open/api/v1/resource/publishEmployee";
  }
}
