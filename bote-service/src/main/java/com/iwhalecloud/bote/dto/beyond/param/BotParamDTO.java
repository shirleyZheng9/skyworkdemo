package com.iwhalecloud.bote.dto.beyond.param;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 数字员工参数DTO
 *
 * @author lizuyin
 * @since 2025-01-15
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class BotParamDTO {
  /** 主页类型，default:默认模板，custom:自定义模板 */
  private String homeType;
  /** 数字员工主页URL */
  private String agentHomeUrl;
  /** 数字员工SSE接口URL */
  private String agentSseUrlOri;
  /** 数字员工Web接口URL */
  private String agentWebUrlOri;
  /** 认证类型，session:共享session，oauth2:oauth2认证 */
  private String authType;
  /** 创建类型，FROM_THIRD:来自第三方 */
  private String createType;
  /** 数字员工开发类型，如：byai */
  private String agentDevType;
  /** 数字员工类型，如：001 */
  private String agentType;
  /** 集成方式 */
  private String integrationType;
  /** 核心能力 */
  private String ability;
  /** 能力边界 */
  private String constraints;
  /** 示例问法 */
  private String faqs;
}
