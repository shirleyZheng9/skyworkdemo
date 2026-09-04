package com.iwhalecloud.bote.dto.knowledge;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 知识问答响应
 *
 * @author bianjp
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeChatResponse {
  /** 对话日志 ID */
  private String chatLogId;
  /** 推理内容 */
  private String reasoning;
  /** 回复文本 */
  private String answer;
  /** 参考文档列表 */
  private List<ReferenceDocumentDTO> references;
  /** 相关问题列表。部分知识库类型支持返回相关问题，比如百应知识库 */
  private List<String> questions;
}
