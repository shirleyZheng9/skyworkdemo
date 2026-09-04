package com.iwhalecloud.bote.doc.module.collaboration.doc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档角色
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRoleVO {

  @Schema(description = "角色编码")
  private String role;
}
