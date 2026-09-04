package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 判断是否登录对象
 *
 * @author chen.linfa
 * @since 2024-07-31
 **/
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "判断是否登录对象")
public class LoggedDTO {
  @Schema(description = "是否已登录")
  private Boolean login;
  @Schema(description = "登录地址")
  private String loginUrl;
  @Schema(description = "重定向地址")
  private String redirectUrl;
  @Schema(description = "登录信息")
  private LoginInfo loginInfo;
  @Schema(description = "是否是超级管理员")
  private Boolean admin;
  @Schema(description = "角色和权限映射，key 为角色编码")
  private Map<String, SimpleRolePrivInfoDTO> rolePrivMap;
  @Schema(description = "生成签名的用户ID的Key")
  private String secretKey;
  @Schema(description = "生成签名的用户ID值")
  private Long secretValue;
  @Schema(description = "签名服务时间")
  private Long serverTime;
  @Schema(description = "签名模式")
  private String securityMode;
}
