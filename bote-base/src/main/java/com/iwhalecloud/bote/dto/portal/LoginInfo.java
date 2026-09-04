package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 登录信息
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录信息")
public class LoginInfo {
  @Schema(description = "鉴权 token")
  private String token;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "用户名称")
  private String userName;
  @Schema(description = "手机号码")
  private String phoneNo;
  @Schema(description = "邮箱")
  private String email;
  @Schema(description = "真实姓名")
  private String realName;
  @Schema(description = "租户 ID")
  private Long defaultTenantId;
  @Schema(description = "用户类型 10平台管理员 20平台开发者")
  private String userType;
  @Schema(description = "系统编码")
  private String systemCode;
  @Schema(description = "外系统用户 ID")
  private String extUserId;
  @Schema(description = "自动创建租户场景，支持指定租户编码")
  private String tenantCode;
  @Schema(description = "自动创建租户场景，支持指定租户名称")
  private String tenantName;
  @Schema(description = "外系统的 session ID")
  private String extSessionId;
  @Schema(description = "自定义属性(值只能使用基本数据类型及 map, list, 不要使用自定义对象，否则序列化/反序列化时可能会丢失类型信息)")
  private Map<String, Object> attributes;
  @Schema(description = "系统类型, lcdp: 灵犀低代码")
  private String systemType;
  @Schema(description = "灵犀租户ID")
  private Long extTenantId;
  @Schema(description = "重定向地址")
  private String redirectUrl;
}
