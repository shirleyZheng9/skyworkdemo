package com.iwhalecloud.bote.dto.skill;

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * agent skill 安全扫描 内容规则
 *
 * @author chen.linfa
 * @since 2026-05-09
 */
@Getter
@Setter
@ToString
@Builder
public class SecuritySourceRuleDTO {
  private String ruleId;
  private String message;
  private Pattern pattern;
  @Nullable
  private Pattern requiresContext;
  @Nullable
  private Integer requiresContextWindowLines;
}
