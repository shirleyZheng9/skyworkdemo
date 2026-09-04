package com.iwhalecloud.bote.doc.module.knowledge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档贡献者 DTO
 *
 * @author qian.sisheng
 * @since 2026/03/02
 */
@Setter
@Getter
@ToString
public class DocumentContributorDTO {
  /** 用户ID */
  private Long userId;
  /** 用户名称 */
  private String realName;
}
