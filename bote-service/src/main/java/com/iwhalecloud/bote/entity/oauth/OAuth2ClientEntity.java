package com.iwhalecloud.bote.entity.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * OAuth2客户端实体
 *
 * @author zhao.xu104
 * @since 2025-07-04
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_OAUTH2_CLIENT")
@Schema(description = "OAuth2客户端")
public class OAuth2ClientEntity extends BaseEntity {

  @DiffId
  @Schema(description = "客户端ID")
  private Long clientId;

  @NotBlank(message = "客户端标识不能为空")
  @DiffField(name = "CLIENT_CODE")
  @Schema(description = "客户端标识")
  private String clientCode;

  @NotBlank(message = "客户端密钥不能为空")
  @DiffField(name = "CLIENT_SECRET")
  @Schema(description = "客户端密钥")
  private String clientSecret;

  @DiffField(name = "CLIENT_NAME")
  @Schema(description = "客户端名称")
  private String clientName;

  @DiffField(name = "DESCRIPTION_INFO")
  @Schema(description = "客户端描述")
  private String descriptionInfo;

  @DiffField(name = "GRANT_TYPES")
  @Schema(description = "授权类型列表，JSON格式")
  private String grantTypes;

  @DiffField(name = "SCOPES")
  @Schema(description = "权限范围列表，JSON格式")
  private String scopes;

  @NotNull(message = "是否启用不能为空")
  @DiffField(name = "ENABLED")
  @Schema(description = "是否启用", example = "T/F")
  private String enabled;

  @DiffField(name = "ACCESS_TOKEN_EXPIRE_SECONDS")
  @Schema(description = "访问令牌过期时间(秒)")
  private Integer accessTokenExpireSeconds;

  @DiffField(name = "REFRESH_TOKEN_EXPIRE_SECONDS")
  @Schema(description = "刷新令牌过期时间(秒)")
  private Integer refreshTokenExpireSeconds;

  @DiffField(name = "AUTH_CODE_EXPIRE_SECONDS")
  @Schema(description = "授权码过期时间(秒)")
  private Integer authCodeExpireSeconds;

  /**
   * 获取授权类型列表
   */
  @JsonIgnore
  public List<String> getGrantTypeList() {
    if (grantTypes == null || grantTypes.trim().isEmpty()) {
      return Arrays.asList("authorization_code", "refresh_token");
    }
    try {
      return JsonUtil.parseJson(grantTypes, new TypeReference<List<String>>() {
      });
    }
    catch (Exception e) {
      return Arrays.asList("authorization_code", "refresh_token");
    }
  }

  /**
   * 设置授权类型列表
   */
  public void setGrantTypeList(List<String> grantTypeList) {
    if (grantTypeList == null || grantTypeList.isEmpty()) {
      this.grantTypes = null;
    }
    else {
      this.grantTypes = JsonUtil.toJsonString(grantTypeList);
    }
  }

  /**
   * 获取权限范围列表
   */
  @JsonIgnore
  public List<String> getScopeList() {
    if (scopes == null || scopes.trim().isEmpty()) {
      return Arrays.asList("read", "write");
    }
    try {
      return JsonUtil.convert(scopes, new TypeReference<List<String>>() {
      });
    }
    catch (Exception e) {
      return Arrays.asList("read", "write");
    }
  }

  /**
   * 设置权限范围列表
   */
  public void setScopeList(List<String> scopeList) {
    if (scopeList == null || scopeList.isEmpty()) {
      this.scopes = null;
    }
    else {
      this.scopes = JsonUtil.toJsonString(scopeList);
    }
  }

  /**
   * 验证授权类型是否支持
   */
  @JsonIgnore
  public boolean isSupportedGrantType(String grantType) {
    return getGrantTypeList().contains(grantType);
  }

  /**
   * 验证权限范围是否支持
   */
  @JsonIgnore
  public boolean isSupportedScope(String scope) {
    if (scope == null || scope.trim().isEmpty()) {
      return true; // 空范围总是允许的
    }
    List<String> requestedScopes = Arrays.asList(scope.split("\\s+"));
    List<String> allowedScopes = getScopeList();
    return new HashSet<>(allowedScopes).containsAll(requestedScopes);
  }
}
