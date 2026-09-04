package com.iwhalecloud.bote.portal.config.properties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

/**
 * 博特门户配置
 *
 * @author bianjp
 * @since 2024-08-22
 */
@ConfigurationProperties("bote.portal")
@Getter
@Setter
@ToString
public class DefaultPortalProperties {
  /** cookie 名称 */
  @NotEmpty
  private String cookieName = "BOTE_SESSION";
  /** 超时时间 */
  @DurationUnit(ChronoUnit.SECONDS)
  private Duration timeout = Duration.ofHours(24);
}
