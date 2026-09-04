package com.iwhalecloud.bote.doc.module.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库类型信息
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@Schema(description = "知识库类型信息")
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTypeDTO {
  @Schema(description = "类型名称")
  private String name;
  @Schema(description = "类型编码")
  private String code;
  @Schema(description = "类型描述")
  private String description;
  @Schema(description = "配置属性列表")
  private List<Object> attrs;
}
