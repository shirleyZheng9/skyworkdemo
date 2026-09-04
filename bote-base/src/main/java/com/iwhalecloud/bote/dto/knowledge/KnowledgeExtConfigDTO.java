package com.iwhalecloud.bote.dto.knowledge;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识库扩展配置
 *
 * @author bianjp
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString
public class KnowledgeExtConfigDTO {
  /** 知识库类型扩展配置（具体内容取决于知识库类型） */
  private Map<String, Object> knowledgeTypeExt;
}
