package com.iwhalecloud.bote.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 天工项目成员同步配置
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * syncUserId 与 syncUserSystemCode 限制内部接口调用方；businessUserSystemCode 固定新业务用户的归属门户。
 */
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("integration.tiangong")
public class TiangongProjectSyncProperties {
  /** 唯一允许调用内部同步接口的技术账号 ID。 */
  @NotNull
  private Long syncUserId = 1L;
  /** 技术账号所属门户，与 Session 的 systemCode 一起校验。 */
  @NotBlank
  private String syncUserSystemCode = "DEFAULT";
  /** 天工实际业务用户所属门户，不能从技术账号 Session 继承。 */
  @NotBlank
  private String businessUserSystemCode = "tiangong-noheader";
  /** 单条 MQ 消息允许写入的最大用户数，限制事务和锁的持续时间。 */
  @Min(1)
  private int maxBatchSize = 200;
}
