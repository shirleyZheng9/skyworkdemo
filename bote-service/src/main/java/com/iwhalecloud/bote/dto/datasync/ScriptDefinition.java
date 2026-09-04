package com.iwhalecloud.bote.dto.datasync;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chen.linfa
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString
@Builder
public class ScriptDefinition {
  /** sql 脚本 */
  private String sql;
  /** sql 脚本入参 */
  private List<String> args;
}
