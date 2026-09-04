package com.iwhalecloud.bote.entity.app;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用 Entity
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_web_app")
public class WebAppEntity extends BaseEntity {

  @DiffId
  @Schema(description = "应用ID")
  private Long webAppId;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "工作空间ID")
  private Long spaceId;

  @DiffField(name = "APP_NAME")
  @Schema(description = "应用名称")
  @Size(max = 20, message = "应用名称超过限定长度20")
  private String appName;

  @DiffField(name = "APP_DESC")
  @Schema(description = "应用描述")
  @Size(max = 500, message = "应用描述超过限定长度500")
  private String appDesc;

  @DiffField(name = "APP_ICON")
  @Schema(description = "应用图标")
  private String appIcon;

  @DiffField(name = "AUTH_TYPE")
  @Schema(description = "授权方式(sso:单点登录, oauth2:OAuth2, custom:自定义登录)")
  private String authType;

  @DiffField(name = "OPEN_TYPE")
  @Schema(description = "打开方式(portal:门户内打开, browser:浏览器打开)")
  private String openType;

  @DiffField(name = "ACCESS_URL")
  @Schema(description = "访问网址")
  private String accessUrl;

}
