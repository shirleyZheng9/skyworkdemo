package com.iwhalecloud.bote.dto.ontology;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体平台动作执行入参
 *
 * @author chen.linfa
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString
public class ActionExecuteRequest {
  /** 应用 ID */
  private String appId;
  /** 动作 ID */
  private String actionId;
  /** 请求 ID */
  private String requestId;
  /** 动作入参 */
  private Map<String, Object> inputParams;
}
