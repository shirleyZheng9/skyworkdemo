package com.iwhalecloud.bote.controller.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.util.MemoryContentUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.base.FeedbackMessageDTO;
import com.iwhalecloud.bote.dto.chat.AppendMessageMemoryRequest;
import com.iwhalecloud.bote.dto.chat.ChatMessageItemDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.SessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgFileDTO;
import com.iwhalecloud.bote.dto.chat.export.SessionSceneDTO;
import com.iwhalecloud.bote.dto.chat.SessionTitleUpdateDTO;
import com.iwhalecloud.bote.dto.chat.SimpleSessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.UpdateMessageMemoryRequestDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bote.dto.chat.query.ChatSessionQueryParams;
import com.iwhalecloud.bote.dto.chat.query.CreateSessionParams;
import com.iwhalecloud.bote.dto.chat.query.MessageGroupResponse;
import com.iwhalecloud.bote.dto.chat.vo.ChatTraceLogVO;
import com.iwhalecloud.bote.service.chat.IChatSessionService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话会话控制器
 *
 * @author Admin
 */
@RequiredArgsConstructor
@Tag(name = "对话：会话")
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "session")
public class ChatSessionController {

  private final IChatSessionService chatSessionService;

  @Operation(summary = "获取会话消息", description = "获取会话消息")
  @GetMapping("findSession")
  public ResultVO<SessionDTO> findSession(@RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话 ID 不能为空");
    return chatSessionService.findSession(sessionId);
  }

  @Operation(summary = "创建会话", description = "创建会话")
  @PostMapping("createSession")
  public ResultVO<SessionDTO> createSession(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(value = "botId", required = false) Long botId, @RequestParam(value = "botTenantId", required = false) Long botTenantId,
    @RequestParam(value = "extSystemId", required = false) Long extSystemId, @RequestParam(value = "platBotId", required = false) Long platBotId,
    @RequestParam(value = "spaceId", required = false) Long spaceId, @RequestParam(value = "isTest", required = false) Boolean isTest) {
    CreateSessionParams params = new CreateSessionParams();
    params.setSpaceId(spaceId);
    params.setTenantId(tenantId);
    params.setBotId(botId);
    params.setBotTenantId(botTenantId);
    params.setExtSystemId(extSystemId);
    params.setPlatBotId(platBotId);
    params.setIsTest(BooleanUtils.isTrue(isTest));
    return chatSessionService.createSession(params);
  }

  @Operation(summary = "删除会话", description = "删除会话")
  @PostMapping("deleteSession")
  public ResultVO<Void> deleteSession(@RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话 ID 不能为空");
    return chatSessionService.deleteSession(sessionId);
  }

  @Operation(summary = "根据应用 ID 删除会话")
  @PostMapping("deleteSessionByBotId")
  public ResultVO<Void> deleteSessionByBotId(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("botId") Long botId,
    @RequestParam(value = "botTenantId", required = false) Long botTenantId, @RequestParam(value = "spaceId", required = false) Long spaceId) {
    Assert.notNull(botId, "应用 ID 不能为空");
    return chatSessionService.clearSessions(spaceId, tenantId, botId, botTenantId);
  }

  @Operation(summary = "清空会话", description = "用于首页清空会话")
  @PostMapping("clearSessions")
  public ResultVO<Void> clearSessions(@RequestParam("tenantId") Long tenantId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return chatSessionService.clearSessions(null, tenantId, null, null);
  }

  @Operation(summary = "修改会话标题", description = "修改会话标题")
  @PostMapping("updateSessionTitle")
  public ResultVO<Void> updateSessionTitle(@RequestBody SessionTitleUpdateDTO req) {
    Assert.notNull(req.getSessionId(), "会话 ID 不能为空");
    Assert.hasText(req.getSessionTitle(), "会话标题不能为空");
    Long userId = SessionUtil.getLoginInfo().getUserId();
    return chatSessionService.updateSessionTitle(req.getSessionId(), req.getSessionTitle(), userId);
  }

  @Operation(summary = "获取应用下的会话列表", description = "获取当前用户会话列表")
  @GetMapping("listSessions")
  public ResultVO<SessionGroupDTO> listSessions(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(value = "botId", required = false) Long botId, @RequestParam(value = "extSystemId", required = false) Long extSystemId,
    @RequestParam(value = "searchContent", required = false) String searchContent, @RequestParam(value = "spaceId", required = false) Long spaceId,
    @RequestParam(value = "isTest", required = false) Boolean isTest) {
    ChatSessionQueryParams params = new ChatSessionQueryParams();
    params.setSpaceId(spaceId);
    params.setTenantId(tenantId);
    params.setBotId(botId);
    params.setExtSystemId(extSystemId);
    params.setSearchContent(searchContent);
    params.setIsTest(BooleanUtils.isTrue(isTest));
    return ResultVO.success(chatSessionService.listSessions(params));
  }

  @Operation(summary = "获取会话列表", description = "用于应用广场")
  @GetMapping("querySessionList")
  public ResultVO<List<SimpleSessionGroupDTO>> querySessionList(@RequestParam(value = "tenantId", required = false) Long tenantId) {
    if (tenantId == null) {
      // TODO 待前端调整后，统一移除
      tenantId = TenantIdUtil.getTenantId();
    }
    return ResultVO.success(chatSessionService.querySessionList(tenantId));
  }

  @Operation(summary = "获取会话列表", description = "用于运行态")
  @PostMapping("querySessionForRuntime")
  public ResultVO<List<SimpleSessionGroupDTO>> querySessionForRuntime(@RequestBody ChatSessionQueryParams params) {
    Assert.notNull(params.getSpaceId(), "空间 ID 不能为空");
    return ResultVO.success(chatSessionService.querySessionForRuntime(params));
  }

  @Operation(summary = "保存消息，用于对话过程，模拟消息入库")
  @PostMapping("saveMockMessage")
  public ResultVO<Void> saveMockMessage(@RequestBody ChatRequestDTO request) {
    Assert.notNull(request.getSessionId(), "会话 ID 不能为空");
    Assert.notNull(request.getMessage(), "模拟消息不能为空");
    Assert.hasText(request.getMessage().getContent(), "模拟消息不能为空");
    return chatSessionService.saveMockMessage(request);
  }

  @Operation(summary = "获取会话消息", description = "供第三方系统使用")
  @GetMapping("/listMessages")
  public ResultVO<MessageGroupResponse> listMessages(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话 ID 不能为空");
    if (tenantId == null) {
      // TODO 待前端调整后，统一移除
      tenantId = TenantIdUtil.getTenantId();
    }
    return chatSessionService.listMessages(tenantId, sessionId);
  }

  @Operation(summary = "分页查询会话消息")
  @PostMapping("queryMessagePage")
  public ResultVO<MessageGroupResponse> queryMessagePage(@RequestBody ChatMessageQueryParams queryParams) {
    Assert.notNull(queryParams.getSessionId(), "会话 ID 不能为空");
    return chatSessionService.queryMessagePage(queryParams);
  }

  @Operation(summary = "分页查询消息")
  @PostMapping("qryMessagesPage")
  public ResultVO<PageInfo<ChatMessageItemDTO>> qryMessagesPage(@RequestBody ChatMessageQueryParams queryParams) {
    Assert.notNull(queryParams.getTenantId(), "tenantId 不能为空");
    return ResultVO.success(chatSessionService.qryMessagesPage(queryParams));
  }

  @Operation(summary = "导出会话列表和会话详情")
  @PostMapping("exportSessionMessages")
  public void exportSessionMessages(@RequestBody ChatMessageQueryParams queryParams, HttpServletResponse response) {
    Assert.notNull(queryParams.getTenantId(), "tenantId 不能为空");
    chatSessionService.exportSessionMessages(queryParams, response);
  }

  @Operation(summary = "导出会话会话详情")
  @PostMapping("exportSessionDetails")
  public void exportSessionDetails(@RequestBody ChatMessageQueryParams queryParams, HttpServletResponse response) {
    Assert.notNull(queryParams.getTenantId(), "tenantId 不能为空");
    Assert.notNull(queryParams.getSessionId(), "sessionId 不能为空");
    chatSessionService.exportSessionDetails(queryParams, response);
  }

  @Operation(summary = "查询会话日志")
  @GetMapping("qryTraceLogs")
  public ResultVO<List<ChatTraceLogVO>> qryTraceLogs(@RequestParam("msgId") Long msgId) {
    Assert.notNull(msgId, "msgId 不能为空");
    return ResultVO.success(chatSessionService.qryTraceLogs(msgId));
  }

  @Operation(summary = "点赞消息", description = "点赞消息")
  @PostMapping("likeMessage")
  public ResultVO<Void> likeMessage(@RequestParam("msgId") Long msgId) {
    Assert.notNull(msgId, "会话消息 ID 不能为空");
    return chatSessionService.updateLikeType(msgId, ChatConsts.LIKE_TYPE_1);
  }

  @Operation(summary = "点踩消息", description = "点踩消息")
  @PostMapping("dislikeMessage")
  public ResultVO<Void> dislikeMessage(@RequestParam("msgId") Long msgId) {
    Assert.notNull(msgId, "会话消息 ID 不能为空");
    return chatSessionService.updateLikeType(msgId, ChatConsts.LIKE_TYPE_2);
  }

  @Operation(summary = "取消点赞/踩消息", description = "点踩消息")
  @PostMapping("unlikeMessage")
  public ResultVO<Void> unlikeMessage(@RequestParam("msgId") Long msgId) {
    Assert.notNull(msgId, "会话消息 ID 不能为空");
    return chatSessionService.updateLikeType(msgId, null);
  }

  @Operation(summary = "更新消息的记忆内容")
  @PostMapping("updateMessageMemory")
  public ResultVO<Void> updateMessageMemory(@RequestBody UpdateMessageMemoryRequestDTO request) {
    Assert.notNull(request.getMsgId(), "消息 ID 不能为空");
    chatSessionService.updateMessageMemory(request);
    return ResultVO.success();
  }

  @Operation(summary = "追加记忆内容", description = "提供给场景/工作流调试功能使用，以确保调试和聊天窗口中处理追加的逻辑一致")
  @PostMapping("appendMessageMemory")
  public ResultVO<Object> appendMessageMemory(@RequestBody AppendMessageMemoryRequest request) {
    Object memoryContent = MemoryContentUtil.appendMemoryContent(request.getOldContent(), request.getContent());
    return ResultVO.success(memoryContent);
  }

  @Operation(summary = "查看会话消息文件列表")
  @GetMapping("listMessageFiles")
  public ResultVO<List<SessionMsgFileDTO>> listMessageFiles(
    @Parameter(description = "会话 ID", required = true) @RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话 ID 不能为空");
    return ResultVO.success(chatSessionService.listMessageFiles(sessionId));
  }

  @Operation(summary = "获取会话未完成的前5个场景")
  @GetMapping("listRunningScenes")
  public ResultVO<List<SceneProcessDTO>> listRunningScenes(@RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话 ID 不能为空");
    return ResultVO.success(chatSessionService.listRunningScenes(sessionId));
  }

  @Operation(summary = "挂起执行中的智能体")
  @PostMapping("suspendRunningScene")
  public ResultVO<Void> suspendRunningScene(@RequestBody SceneProcessDTO scene) {
    Assert.notNull(scene.getSessionId(), "会话 ID 不能为空");
    Assert.notNull(scene.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(scene.getBotId(), "应用 ID 不能为空");
    Assert.notNull(scene.getSceneId(), "智能体 ID 不能为空");
    Assert.hasText(scene.getContextId(), "上下文 ID 不能为空");
    Assert.hasText(scene.getSceneName(), "智能体名称不能为空");
    chatSessionService.suspendRunningScene(scene);
    return ResultVO.success();
  }

  @Operation(summary = "分页查询对话记录")
  @PostMapping("querySessionRecordPage")
  public ResultVO<PageInfo<SessionSceneDTO>> querySessionRecordPage(@RequestBody ChatMessageQueryParams queryParams) {
    return ResultVO.success(chatSessionService.querySessionRecordPage(queryParams));
  }

  @Operation(summary = "查询对话相关的智能体")
  @GetMapping("querySessionSceneList")
  public ResultVO<List<SessionSceneDTO>> querySessionSceneList(@RequestParam("sessionId") Long sessionId) {
    return ResultVO.success(chatSessionService.querySessionSceneList(sessionId));
  }

  @Operation(summary = "反馈会话消息")
  @PostMapping("feedbackMessage")
  public ResultVO<Void> feedbackMessage(@RequestBody FeedbackMessageDTO feedbackMessage) {
    return chatSessionService.feedbackMessage(feedbackMessage);
  }

  @Operation(summary = "根据sessionId获取用户最近发送的一条消息")
  @GetMapping("queryLastMsgText")
  public ResultVO<String> queryLastMsgText(@RequestParam("sessionId") Long sessionId) {
    Assert.notNull(sessionId, "会话记录不存在");
    return ResultVO.success(chatSessionService.getLastMsgText(sessionId));
  }
}
