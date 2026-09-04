package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.module.knowledge.entity.DocumentParameterEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档参数
 *
 * @author qian.sisheng
 * @since 2025-1-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DocumentParameterDTO extends DocumentParameterEntity {

  @Schema(description = "旧映射字段")
  private String oldMappingCode;
  @Schema(description = "最大长度")
  private Integer maxLength;
  @Schema(description = "是否已修改")
  private Boolean isChanged;
}
