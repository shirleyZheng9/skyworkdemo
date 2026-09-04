package com.iwhalecloud.bote.service.chat;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.FeedbackMessageDTO;
import com.iwhalecloud.bote.dto.chat.ChatMessageItemDTO;
import com.iwhalecloud.bote.dto.chat.ChatRequestDTO;
import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO;
import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.SessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.export.SessionSceneDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgFileDTO;
import com.iwhalecloud.bote.dto.chat.SimpleSessionGroupDTO;
import com.iwhalecloud.bote.dto.chat.UpdateMessageMemoryRequestDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bote.dto.chat.query.ChatSessionQueryParams;
import com.iwhalecloud.bote.dto.chat.query.CreateSessionParams;
import com.iwhalecloud.bote.dto.chat.query.MessageGroupResponse;
import com.iwhalecloud.bote.dto.chat.vo.ChatTraceLogVO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 对话会话应用服务
 *
 * @author Admin
 */
public interface IChatSessionService {

  /**
   * 查询单个会话消息
   *
   * @param sessionId 会话 ID
   * @return 会话
   */
  ResultVO<SessionDTO> findSession(Long sessionId);

  /**
   * 创建会话
   *
   * @param params 入参
   * @return 结果
   */
  ResultVO<SessionDTO> createSession(CreateSessionParams params);

  /**
   * 删除会话
   *
   * @param sessionId 会话 ID
   * @return 结果
   */
  ResultVO<Void> deleteSession(Long sessionId);

  /**
   * 清空会话
   *
   * @param spaceId 空间 ID
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param botTenantId 应用归属租户 ID
   * @return 结果
   */
  ResultVO<Void> clearSessions(@Nullable Long spaceId, Long tenantId, @Nullable Long botId, @Nullable Long botTenantId);

  /**
   * 修改会话标题
   *
   * @param sessionId 会话 ID
   * @param title 标题
   * @param userId 用户 ID
   * @return 结果
   */
  ResultVO<Void> updateSessionTitle(Long sessionId, String title, Long userId);

  /**
   * 获取应用下的会话列表
   *
   * @param params 查询条件
   * @return 会话分组
   */
  SessionGroupDTO listSessions(ChatSessionQueryParams params);

  /**
   * 获取会话列表，用于应用广场
   *
   * @param tenantId 租户 ID
   * @return 会话分组
   */
  List<SimpleSessionGroupDTO> querySessionList(Long tenantId);

  /**
   * 获取会话列表，用于运行态
   *
   * @param params 入参
   * @return 会话分组
   */
  List<SimpleSessionGroupDTO> querySessionForRuntime(ChatSessionQueryParams params);

  /**
   * 保存消息，用于对话过程，模拟消息入库
   *
   * @param request 对话入参
   * @return 结果
   */
  ResultVO<Void> saveMockMessage(ChatRequestDTO request);

  /**
   * 获取会话消息
   *
   * @param tenantId 租户 ID
   * @param sessionId 回话 ID
   * @return 会话消息
   */
  ResultVO<MessageGroupResponse> listMessages(Long tenantId, Long sessionId);

  /**
   * 分页查询会话消息
   *
   * @param queryParams 查询条件
   * @return 会话消息
   */
  ResultVO<MessageGroupResponse> queryMessagePage(ChatMessageQueryParams queryParams);

  /**
   * 分页查询消息
   */
  PageInfo<ChatMessageItemDTO> qryMessagesPage(ChatMessageQueryParams queryParams);

  /**
   * 导出会话消息为Excel文件
   *
   * @param queryParams 查询条件（支持tenantId、sessionId、时间范围等）
   * @param response HTTP响应对象，用于写出Excel文件
   */
  void exportSessionMessages(ChatMessageQueryParams queryParams, HttpServletResponse response);

  /**
   * 导单个会话详情为Excel文件
   *
   * @param queryParams 搜索条件
   * @param response HTTP响应对象，用于写出Excel文件
   */
  void exportSessionDetails(ChatMessageQueryParams queryParams, HttpServletResponse response);

  /**
   * 查询会话日志
   *
   * @param msgId 消息 ID
   * @return 会话日志列表
   */
  List<ChatTraceLogVO> qryTraceLogs(Long msgId);

  /**
   * 修改点赞类型
   *
   * @param msgId 消息 ID
   * @param likeType 点赞类型
   * @return 结果
   */
  ResultVO<Void> updateLikeType(Long msgId, @Nullable String likeType);

  /**
   * 更新消息的记忆内容
   */
  void updateMessageMemory(UpdateMessageMemoryRequestDTO request);

  /**
   * 保存会话消息
   *
   * @param msgs 消息列表
   * @param userId 用户 ID
   */
  void saveSessionMsgs(List<SessionMsgDTO> msgs, Long userId);

  /**
   * 更新会话结束时间
   *
   * @param sessionId 会话 ID
   * @param userId 用户 ID
   * @param endTime 结束时间
   */
  void updateSessionEndTime(Long sessionId, Long userId, Date endTime);

  /**
   * 保存会话日志
   *
   * @param msgId 用户消息 ID
   * @param logs 会话日志列表
   */
  void saveChatTraceLogs(Long msgId, List<ChatTraceLogDTO> logs);

  /**
   * 查看会话消息文件列表
   *
   * @param sessionId 会话 ID
   * @return 会话消息文件列表
   */
  List<SessionMsgFileDTO> listMessageFiles(Long sessionId);

  /**
   * 获取会话未完成的前5个场景
   *
   * @param sessionId 会话 ID
   * @return 完成的场景
   */
  List<SceneProcessDTO> listRunningScenes(Long sessionId);

  /**
   * 挂起执行中的智能体
   */
  void suspendRunningScene(SceneProcessDTO scene);

  /**
   * 查询对话记录
   *
   * @param queryParams 查询参数
   * @return 会话场景列表
   */
  PageInfo<SessionSceneDTO> querySessionRecordPage(ChatMessageQueryParams queryParams);

  /**
   * 查询会话关联的场景列表
   *
   * @param sessionId 会话 ID
   * @return 会话场景列表
   */
  List<SessionSceneDTO> querySessionSceneList(Long sessionId);

  /**
   * 反馈会话消息
   *
   * @param feedbackMessage 反馈信息
   * @return 结果
   */
  ResultVO<Void> feedbackMessage(FeedbackMessageDTO feedbackMessage);

  /**
   * 根据sessionId获取用户最近发送的一条消息
   * @param sessionId 会话 ID
   * @return 用户发送的消息内容
   */
  String getLastMsgText(Long sessionId);
}
