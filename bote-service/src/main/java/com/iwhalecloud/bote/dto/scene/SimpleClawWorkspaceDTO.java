package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单 claw 工作空间
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString
public class SimpleClawWorkspaceDTO {
  /** 主键 */
  private Long id;
  /** 文件名称 */
  private String fileName;
  /** 文件内容 */
  private String fileContent;
}
