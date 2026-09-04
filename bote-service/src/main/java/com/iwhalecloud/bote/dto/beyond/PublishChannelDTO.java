package com.iwhalecloud.bote.dto.beyond;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布渠道DTO
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class PublishChannelDTO {
  /** 目录ID */
  private Long catalogId;
  /** 组织ID */
  private Long manOrgId;
  /** 管理员用户ID */
  private Long manUserId;
  /** 备注 */
  private String remark;
  /** 核心能力 */
  private String ability;
  /** 能力边界 */
  private String constraints;
  /** 示例问法 */
  private String faqs;

  /** 发布名称 */
  private String name;

  /** appId */
  private String appId;
  /** secret */
  private String secret;
  /** token */
  private String token;
  /** aesKey */
  private String aesKey;

  // A2A 配置
  /** 智能体名称 */
  private String agentName;
  /** 智能体描述 */
  private String agentDesc;

  // 飞书 配置
  /** 验证令牌 */
  private String verificationToken;
  /** 加密密钥 */
  private String encryptKey;
  // 钉钉 配置
  /** 机器人Code */
  private String robotCode;

}
