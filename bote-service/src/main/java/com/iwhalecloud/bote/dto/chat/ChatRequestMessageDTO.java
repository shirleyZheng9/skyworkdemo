package com.iwhalecloud.bote.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 对话请求消息
 *
 * @author bianjp
 * @since 2025-03-07
 */
@Getter
@Setter
@ToString
public class ChatRequestMessageDTO {
  @Schema(description = "消息类型(input: 用户输入; point: 副驾指令, a2ui: A2UI 页面交互)", allowableValues = {"input", "point", "a2ui"}, requiredMode = RequiredMode.REQUIRED)
  private String type;
  @Schema(description = "角色(user: 用户输入, tool: 页面交互)", allowableValues = {"user", "tool"})
  private String role;
  @Schema(description = "消息内容")
  private String content;
  @Schema(description = "附件 ID 列表")
  private List<Long> fileIds;
  @Schema(description = "参数（指令表单数据、副驾指令参数），对应到场景入参")
  private Map<String, Object> params;
  @Schema(description = "关联消息 ID，可选，表示用户消息关联的助手消息（点击追问问题时为追问问题的消息 ID, 页面表单提交时未页面消息 ID）")
  private Long refId;
  @Schema(description = "工具调用 ID, 仅简单场景中页面、页面函数回调使用，用于将回调结果作为大模型的工具调用结果")
  private String toolCallId;

  /**
   * 判断是否空消息
   */
  @JsonIgnore
  public boolean isEmpty() {
    return StringUtils.isEmpty(content) && CollectionUtils.isEmpty(fileIds) && MapUtils.isEmpty(params);
  }
}
