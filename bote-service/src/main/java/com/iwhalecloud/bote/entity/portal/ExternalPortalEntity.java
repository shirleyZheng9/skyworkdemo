package com.iwhalecloud.bote.entity.portal;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 外部门户
 *
 * @author bianjp
 * @since 2025-02-24
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_external_portal")
@Schema(hidden = true)
public class ExternalPortalEntity extends BaseEntity {
  @Id
  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "portal_name")
  @Schema(description = "门户名称")
  private String portalName;
  @DiffField(name = "portal_code")
  @Schema(description = "门户编码（平台内唯一，用作副驾 sdk 的 systemCode 参数）")
  private String portalCode;
  @DiffField(name = "portal_type")
  @Schema(description = "门户类型", allowableValues = {BaseConsts.PORTAL_TYPE_UPORTAL, BaseConsts.PORTAL_TYPE_NGPORTAL, BaseConsts.PORTAL_TYPE_BASIC_CENTER, BaseConsts.PORTAL_TYPE_SSO, BaseConsts.PORTAL_TYPE_NONE})
  private String portalType;
  @DiffField(name = "default_tenant_id")
  @Schema(description = "新用户默认绑定租户 ID")
  private Long defaultTenantId;
  @DiffField(name = "default_role")
  @Schema(description = "新用户默认角色")
  private String defaultRole;
  @DiffField(name = "login_url")
  @Schema(description = "登录地址")
  private String loginUrl;
  @DiffField(name = "logged_url")
  @Schema(description = "登录状态检查接口地址")
  private String loggedUrl;
  @DiffField(name = "REDIRECT_URL")
  @Schema(description = "跳转博特地址")
  private String redirectUrl;
  @DiffField(name = "cookie_name")
  @Schema(description = "sessionId cookie 名称")
  private String cookieName;
  @DiffField(name = "url_param_name")
  @Schema(description = "URL 中的 sessionId 参数名称")
  private String urlParamName;
  @DiffField(name = "AUTO_CREATE_TENANT")
  @Schema(description = "首次登录是否自动创建租户")
  private String autoCreateTenant;
  @DiffField(name = "SECRET_KEY")
  @Schema(description = "JWT 签名密钥")
  private String secretKey;
  @DiffField(name = "SSO_SCRIPT")
  @Schema(description = "单点登录自定义 groovy 脚本")
  private String ssoScript;
  @DiffField(name = "HEADER_JSON")
  @Schema(description = "请求头")
  private String headerJson;
}
