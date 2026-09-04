package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.module.knowledge.entity.DocumentContentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料内容
 *
 * @author qian.sisheng
 * @since 2025-1-15
 */

@Getter
@Setter
@ToString(callSuper = true)
public class DocumentContentDTO extends DocumentContentEntity {
  @Schema(description = "操作类型")
  private String actionType;
}
