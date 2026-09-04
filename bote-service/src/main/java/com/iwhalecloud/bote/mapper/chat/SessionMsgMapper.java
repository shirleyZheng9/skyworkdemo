package com.iwhalecloud.bote.mapper.chat;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.chat.ChatMessageItemDTO;
import com.iwhalecloud.bote.dto.chat.HistoryMessageDTO;
import com.iwhalecloud.bote.dto.chat.SessionMsgDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import com.iwhalecloud.bote.dto.chat.vo.SessionMsgVO;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 会话消息管理
 *
 * @author auto
 * @since 2024-10-28
 */
public interface SessionMsgMapper {

  /**
   * 根据 ID 查询消息
   *
   * <p>用于更新记忆内容接口，只查出用到的字段</p>
   */
  SessionMsgDTO selectMsgById(@Param("msgId") Long msgId, @Param("selectMemoryContent") boolean selectMemoryContent);

  /**
   * 更新点赞类型
   *
   * @param msgId 消息 ID
   * @param likeType 点赞类型
   * @return 结果
   */
  int updateSessionMsgLikeType(@Param("msgId") Long msgId, @Param("likeType") String likeType, @Param("updatorId") Long updatorId);

  /**
   * 批量新增会话消息
   *
   * @param sessionMsgs 会话消息列表
   * @return 结果
   */
  int batchInsertSessionMsg(@Param("list") List<SessionMsgDTO> sessionMsgs);

  /**
   * 获取会话消息列表
   *
   * @param sessionId 会话 ID
   * @return 会话消息列表
   */
  List<SessionMsgVO> listSessionMsgs(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 和上下文 ID 查询会话消息列表
   */
  List<SessionMsgVO> selectMessagesBySessionId(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 分页查询会话的事务 ID 列表
   */
  Page<Long> selectTransactionIdsPage(@Param("query") ChatMessageQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据事务 ID 列表查询会话消息列表
   */
  List<SessionMsgVO> selectMessagesByTransactionIds(@Param("query") ChatMessageQueryParams queryParams, @Param("transactionIds") List<Long> transactionIds);

  /**
   * 查询关联的消息列表
   */
  List<SessionMsgVO> selectRelatedSessionMessage(@Param("sessionId") Long sessionId, @Param("msgIds") Collection<Long> msgIds);

  /**
   * 查询关联的段落消息列表
   */
  List<SessionMsgVO> selectParagraphSessionMessage(@Param("sessionId") Long sessionId, @Param("transactionIds") Collection<Long> transactionIds);

  /**
   * 查询计划相关的会话消息列表
   */
  List<SessionMsgVO> selectSessionMessageForPlan(@Param("sessionId") Long sessionId, @Param("planIds") Collection<Long> planIds);

  /**
   * 分页查询会话消息
   */
  Page<ChatMessageItemDTO> selectMessagesPage(@Param("query") ChatMessageQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据会话 ID 获取最大排序号
   *
   * @param sessionId 会话 ID
   * @return 最大排序号
   */
  Integer getMaxSort(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 和上下文 ID 获取最大排序号
   */
  Integer getMaxSortByContext(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 查询历史消息列表，用于联网搜索的大模型记忆
   *
   * <p>只查询用户消息和文本回复</p>
   */
  List<HistoryMessageDTO> selectHistoryTextMessagesByContextId(@Param("sessionId") Long sessionId,
    @Param("contextId") String contextId,
    @Param("date") Date date);

  /**
   * 根据会话 ID 和上下文 ID 查询历史消息列表，用于工作流中的大模型记忆
   */
  List<HistoryMessageDTO> selectHistoryMessagesByContextId(@Param("sessionId") Long sessionId,
    @Param("contextId") String contextId,
    @Param("date") Date date);

  /**
   * 根据会话 ID 和上下文 ID 查询最后一条历史消息
   *
   * @param sessionId 会话 ID
   * @param contextId 上下文 ID
   * @return 最后一条消息
   */
  SessionMsgVO selectLastMessage(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 根据会话 ID 和上下文 ID 查询场景 ID
   */
  Long selectSceneIdByContextId(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 根据会话 ID 和上下文 ID 判断是否存在消息
   */
  boolean existsMessage(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 根据会话 ID 查询最后一条历史消息，用于会话列表展示最后一条消息内容
   *
   * @param sessionId 会话 ID
   * @param msgTypes 消息类型
   * @return 最后一条消息
   */
  SessionMsgVO selectLastMessageForShow(@Param("sessionId") Long sessionId, @Param("msgTypes") List<String> msgTypes);

  /**
   * 批量更新反馈信息
   *
   * @param feedbackReason 反馈信息
   * @param updatorId 更新人
   * @return 影响行数
   */
  int updateFeedbackMessage(@Param("msgId") Long msgId, @Param("feedbackReason") String feedbackReason, @Param("updatorId") Long updatorId);

  /**
   * 更新消息状态
   */
  int updateMsgStatus(@Param("msgId") Long msgId, @Param("msgStatus") String msgStatus);

  /**
   * 批量设置消息为已压缩状态
   */
  int updateMsgCompressed(@Param("msgIds") List<Long> msgIds);
}
