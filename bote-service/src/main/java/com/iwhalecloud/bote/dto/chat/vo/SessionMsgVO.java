package com.iwhalecloud.bote.dto.chat.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import com.iwhalecloud.bote.dto.chat.agent.AgentReplyPart;
import com.iwhalecloud.bote.dto.knowledge.ReferenceDocumentDTO;
import com.iwhalecloud.bote.dto.planning.SimplePlanDTO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 会话消息
 *
 * @author Admin
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@Schema(description = "会话消息")
public class SessionMsgVO {
  @Schema(description = "消息 ID")
  private Long id;
  @Schema(description = "关联的消息 ID")
  private Long refId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "应用名称")
  private String botName;
  @Schema(description = "应用归属的租户 ID, 为空时表示当前租户")
  private Long ownerTenantId;
  @Schema(description = "事务 ID")
  private Long transactionId;
  @Schema(description = "上下文 ID")
  private String contextId;
  @Schema(description = "智能体 ID")
  private Long sceneId;
  @Schema(description = "计划 ID")
  private Long planId;
  @Schema(description = "消息类型")
  private String type;
  @Schema(description = "角色(system, user, assistant, tool)")
  private String role;
  @Schema(description = "消息内容")
  private String content;
  @Schema(description = "点赞类型，1: 点赞; 2: 点踩; 3: 不予置评;")
  private String likeType;
  @Schema(description = "文件下载类型")
  private String downloadType;
  @Schema(description = "输出内容格式")
  private String contentType;
  @Schema(description = "段落格式归属的文档")
  private String paragraphGroup;
  @Schema(description = "段落格式的序号")
  private Integer paragraphSortby;
  @Schema(description = "开始时间")
  private Date beginTime;
  @Schema(description = "结束时间")
  private Date endTime;
  @Schema(description = "排序")
  private Integer sort;
  @Schema(description = "反馈原因")
  private String feedbackReason;
  @Schema(description = "文件信息列表")
  private List<FileInfoDTO> fileInfos;

  @Schema(description = "页面类型工具函数回调 ID")
  private String toolCallId;
  @Schema(description = "附件 ID 列表")
  private List<Long> fileIds;
  @Schema(description = "附件信息，用于历史消息附件渲染")
  private List<FileInfoVO> files;
  @Schema(description = "参考文档")
  private List<ReferenceDocumentDTO> references;
  @Schema(description = "推荐场景列表")
  private List<RecommendedSceneDTO> recommendedScenes;
  @Schema(description = "页面数据")
  private Map<String, Object> page;
  @Schema(description = "agentReply 的回复片段列表")
  private List<AgentReplyPart> agentReplyParts;

  @Schema(description = "扩展参数，查询数据库使用", hidden = true)
  @JsonIgnore
  private String extParams;

  @Schema(description = "计划数据，用于渲染历史会话")
  private SimplePlanDTO plan;

  /**
   * 获取系统提醒
   */
  @Nullable
  @JsonIgnore
  public List<SystemReminder> getReminders() {
    SessionMsgExtParamsDTO params = JsonUtil.parseJson(extParams, SessionMsgExtParamsDTO.class);
    if (params != null) {
      return params.getReminders();
    }
    return null;
  }
}
