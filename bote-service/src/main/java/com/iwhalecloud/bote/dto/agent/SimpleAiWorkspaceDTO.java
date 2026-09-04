package com.iwhalecloud.bote.dto.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单用户级的提示词
 *
 * @author chen.linfa
 * @since 2026-03-19
 */
@Getter
@Setter
@ToString
public class SimpleAiWorkspaceDTO {
  /** 文件名称 */
  private String fileName;
  /** 文件内容 */
  private String fileContent;
}
