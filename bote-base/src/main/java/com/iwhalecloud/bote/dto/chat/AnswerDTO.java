package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 流式回复内容
 *
 * @author bianjp
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
public class AnswerDTO {
  /** 消息 ID, 目前仅用于处理回复节点的文件下载，不需要传给别的地方 */
  @JsonIgnore
  private String msgId;
  /** 步骤编码 */
  private String code;
  /** 步骤名称 */
  private String name;
  /** 文本内容 */
  private String text;
  /** 自定义的记忆内容 */
  private String memoryContent;
  /** 参考文档列表 */
  private List<ReferenceDocumentDTO> references;
  /** 追问问题列表 */
  private List<String> questions;
  /** 知识问答的对话日志 ID */
  private String chatLogId;
  /** 推理内容 */
  private String reasoning;

  public AnswerDTO() {
  }

  public AnswerDTO(String msgId, String code, String name) {
    this.msgId = msgId;
    this.code = code;
    this.name = name;
  }

  /**
   * 转为 Map
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    if (StringUtils.isNotEmpty(name)) {
      map.put("name", name);
    }
    if (StringUtils.isNotEmpty(code)) {
      map.put("code", code);
    }
    if (StringUtils.isNotEmpty(reasoning)) {
      map.put("reasoning", reasoning);
    }
    map.put("text", text);
    if (CollectionUtils.isNotEmpty(references)) {
      map.put("references", references);
    }
    if (CollectionUtils.isNotEmpty(questions)) {
      map.put("questions", questions);
    }
    if (StringUtils.isNotEmpty(chatLogId)) {
      map.put("chatLogId", chatLogId);
    }
    return map;
  }
}
