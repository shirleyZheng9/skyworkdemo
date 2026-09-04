package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 知识问答节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class KnowledgeChatNodeData {
  /** 知识库 ID（支持引用变量，支持逗号分隔的多个知识库 ID) */
  private String knowledgeId;
  /** 问题（模板字符串，支持引用变量） */
  private String question;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 是否流式输出，默认否 */
  private Boolean stream;
  /** 是否返回参考文档，默认否 */
  private Boolean withReferences;
  /** 是否返回追问问题，默认否 */
  private Boolean withQuestions;
}
