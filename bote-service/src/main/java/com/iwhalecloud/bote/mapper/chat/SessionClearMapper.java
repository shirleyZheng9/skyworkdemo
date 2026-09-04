package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.entity.chat.SessionEntity;
import com.iwhalecloud.bote.entity.chat.SessionMsgEntity;
import com.iwhalecloud.bote.entity.chat.SessionMsgFileEntity;
import com.iwhalecloud.bote.entity.chat.SessionMsgTextEntity;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会话归档、清理相关数据库操作
 *
 * @author bianjp
 * @since 2025-08-23
 */
public interface SessionClearMapper {
  /**
   * 查询待归档的会话 ID
   */
  List<Long> selectSessionIdsForArchive(@Param("maxUpdatedTime") Date maxUpdatedTime, @Param("limit") int limit);

  /**
   * 查询待清理的历史会话 ID
   */
  List<Long> selectHistorySessionIdsForClear(@Param("maxUpdatedTime") Date maxUpdatedTime, @Param("limit") int limit);

  /**
   * 归档会话
   */
  int archiveSession(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 归档消息
   */
  int archiveMsg(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 归档消息文本
   */
  int archiveMsgText(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 归档消息文件
   */
  int archiveMsgFile(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 查询会话列表
   */
  List<SessionEntity> selectSessionList(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 查询消息列表
   */
  List<SessionMsgEntity> selectMsgList(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 查询消息文本列表
   */
  List<SessionMsgTextEntity> selectMsgTextList(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 查询消息文件列表
   */
  List<SessionMsgFileEntity> selectMsgFileList(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 批量插入历史会话
   */
  int batchInsertSessionHistory(@Param("sessionList") List<SessionEntity> sessionList);

  /**
   * 批量插入历史消息
   */
  int batchInsertMsgHistory(@Param("msgList") List<SessionMsgEntity> msgList);

  /**
   * 批量插入历史消息文本
   */
  int batchInsertMsgTextHistory(@Param("msgTextList") List<SessionMsgTextEntity> msgTextList);

  /**
   * 批量插入历史消息文件
   */
  int batchInsertMsgFileHistory(@Param("msgFileList") List<SessionMsgFileEntity> msgFileList);

  /**
   * 根据机器人 ID 查询会话 ID 列表
   *
   * @param spaceId 空间 ID
   * @param tenantId 当前租户 ID
   * @param botId 应用 ID
   * @param botTenantId 应用归属的租户 ID
   * @param creatorId 创建人
   */
  List<Long> selectSessionIdsByBotId(@Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId, @Param("botId") Long botId,
    @Param("botTenantId") Long botTenantId, @Param("creatorId") Long creatorId);

  /**
   * 删除会话
   */
  int deleteSessionBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除消息
   */
  int deleteMsgBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除消息文本
   */
  int deleteMsgTextBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除消息文件
   */
  int deleteMsgFileBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除历史会话
   */
  int deleteHistorySessionBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除历史消息
   */
  int deleteHistoryMsgBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除历史消息文本
   */
  int deleteHistoryMsgTextBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除历史消息文件
   */
  int deleteHistoryMsgFileBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 根据历史会话 ID 失效关联的文件
   */
  int disableFilesByHistorySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除归档失败的会话
   */
  int deleteArchiveFailingSession();

  /**
   * 删除归档失败的消息
   */
  int deleteArchiveFailingMsg();

  /**
   * 删除归档失败的消息文本
   */
  int deleteArchiveFailingMsgText();

  /**
   * 删除归档失败的消息文件
   */
  int deleteArchiveFailingMsgFile();

  /**
   * 删除自主规划模式、知识问答模式智能体的会话记录
   */
  int deleteChatMessagesBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 删除自主规划模式、知识问答模式智能体的会话记录，用于删除调试记录
   */
  int deleteChatMessagesBySessionId(@Param("sessionId") Long sessionId, @Param("maxCreateTime") Date maxCreateTime);

  /**
   * 删除自主规划模式、知识问答模式智能体的会话记录
   */
  int deleteChatMessages(@Param("maxCreateTime") Date maxCreateTime);
}
