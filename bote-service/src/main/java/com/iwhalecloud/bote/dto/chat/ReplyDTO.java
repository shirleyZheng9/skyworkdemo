package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.agent.tools.A2uiTools.A2uiEvent;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.orchestration.step.ReplyStep.ContentTypeConfig;
import com.iwhalecloud.bote.dto.search.response.BochaSearchResponse.WebPageInfoGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回复对象
 *
 * @author bianjp
 * @since 2025-03-10
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "回复对象")
public final class ReplyDTO {
  @Schema(description = "步骤编码")
  private String code;
  @Schema(description = "步骤名称")
  private String name;
  @Schema(description = "回复类型", allowableValues = {"text", "page", "pageFunc"})
  private ChatMessageType type;
  @Schema(description = "文本回复")
  private String text;
  @Schema(description = "页面回复")
  private Map<String, Object> page;
  @Schema(description = "页面函数回复")
  private Map<String, Object> pageFunc;
  @Schema(description = "参考文档列表")
  private List<ReferenceDocumentDTO> references;
  @Schema(description = "追问问题列表")
  private List<String> questions;
  @Schema(description = "网页")
  private List<WebPageInfoGroup> pageInfos;
  @Schema(description = "附件")
  private List<FileInfoDTO> fileInfos;
  @Schema(description = "场景切换")
  private Map<String, Object> agentSwitch;
  @Schema(description = "推理内容")
  private String reasoning;
  @Schema(description = "输出内容格式")
  private ContentTypeConfig contentType;
  @Schema(description = "流程步骤")
  private Map<String, Object> flowStep;
  @Schema(description = "推荐信息")
  private Map<String, Object> recommendation;
  @Schema(description = "A2UI 事件列表")
  private List<A2uiEvent> a2uiEvents;

  public ReplyDTO() {
  }

  /**
   * 构造回复对象
   *
   * @param type 消息类型
   * @param content 消息内容
   * @param code 步骤编码
   * @param name 步骤名称
   */
  @SuppressWarnings("unchecked")
  public ReplyDTO(ChatMessageType type, Object content, String code, String name) {
    this.type = type;
    this.code = code;
    this.name = name;
    List<ChatMessageType> types = Arrays.asList(ChatMessageType.TEXT, ChatMessageType.REASONING);
    if (types.contains(type)) {
      this.text = (String) content;
    }
    else if (type == ChatMessageType.PAGE) {
      this.page = (Map<String, Object>) content;
    }
    else if (type == ChatMessageType.PAGE_FUNC) {
      this.pageFunc = (Map<String, Object>) content;
    }
    else if (type == ChatMessageType.AGENT_SWITCH) {
      this.agentSwitch = (Map<String, Object>) content;
    }
    else if (type == ChatMessageType.FLOW_STEP) {
      this.flowStep = (Map<String, Object>) content;
    }
    else if (type == ChatMessageType.TEXT_CONTENT_TYPE) {
      this.contentType = (ContentTypeConfig) content;
    }
    else if (type == ChatMessageType.RECOMMENDATION) {
      this.recommendation = (Map<String, Object>) content;
    }
    else if (type == ChatMessageType.A2UI) {
      this.a2uiEvents = (List<A2uiEvent>) content;
    }
    else {
      throw new IllegalStateException("不支持的回复类型: " + type.getCode());
    }
  }

  /**
   * 构造流式输出的回复对象
   *
   * @param answer 流式回复内容
   */
  public ReplyDTO(AnswerDTO answer) {
    this.type = ChatMessageType.TEXT;
    this.text = answer.getText();
    this.code = answer.getCode();
    this.name = answer.getName();
    this.references = answer.getReferences();
    this.questions = answer.getQuestions();
    this.reasoning = answer.getReasoning();
  }
}
