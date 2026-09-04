package com.iwhalecloud.bote.license.condition;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 需要 license 的条件：默认开启；仅当 {@code bote.license.enabled=false} 时关闭（便于本地/Docker 无 bote.lic 时启动）。
 *
 * @author cheng.xu
 */
class OnLicenseCondition extends SpringBootCondition {

  private static final String ENABLED_PROPERTY = "bote.license.enabled";

  @Override
  public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
    boolean enabled = context.getEnvironment().getProperty(ENABLED_PROPERTY, Boolean.class, Boolean.TRUE);
    if (!enabled) {
      return ConditionOutcome.noMatch(ENABLED_PROPERTY + " is false");
    }
    return ConditionOutcome.match();
  }
}
