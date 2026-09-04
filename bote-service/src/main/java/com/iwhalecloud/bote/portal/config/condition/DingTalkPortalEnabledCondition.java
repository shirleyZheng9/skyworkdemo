package com.iwhalecloud.bote.portal.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Aiqing
 * @since 2025/6/4
 */
public class DingTalkPortalEnabledCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String dingTalkEnabled = context.getEnvironment().getProperty("bote.portal.type");
    String multiPortalEnabled = context.getEnvironment().getProperty("bote.portal.multi.enabled");

    return "true".equalsIgnoreCase(dingTalkEnabled) && "true".equalsIgnoreCase(multiPortalEnabled);
  }
}
