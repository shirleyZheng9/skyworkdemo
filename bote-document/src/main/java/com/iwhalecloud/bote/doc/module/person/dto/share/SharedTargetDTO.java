package com.iwhalecloud.bote.doc.module.person.dto.share;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 我共享的文档 DTO
 *
 * @author lizuyin
 * @since 2025-08-20
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "我共享的文档")
public class SharedTargetDTO {
  /** 对象ID（用户/组织/角色/分组） */
  private Long itemId;
  /** 对象名称 */
  private String itemName;
  /** 对象所属组织 */
  private String itemOrg;
  /** 授权的权限 */
  private String permission;
  /** 对象类型：USER/ORG/ROLE/GROUP */
  private String type;
}


