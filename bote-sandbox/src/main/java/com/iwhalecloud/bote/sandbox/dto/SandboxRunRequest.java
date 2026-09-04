package com.iwhalecloud.bote.sandbox.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 执行命令请求
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxRunRequest {
  /** 命令 **/
  private String command;
  /** 超时时间(ms) */
  private Long timeout;
  /** 工作目录 */
  private String workDir;
  /** 环境变量 */
  private Map<String, String> env;
}
