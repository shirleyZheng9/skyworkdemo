package com.iwhalecloud.bote.mapper.chat;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.chat.SessionDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatSessionQueryParams;
import com.iwhalecloud.bote.dto.chat.export.SessionSceneDTO;
import com.iwhalecloud.bote.dto.chat.query.ChatMessageQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 会话管理
 *
 * @author auto
 * @since 2024-10-28
 */
public interface SessionMapper {

  /**
   * 根据主键获取会话
   *
   * @param sessionId 会话 ID
   * @return 会话
   */
  SessionDTO getSession(@Param("id") Long sessionId);

  /**
   * 根据会话 ID 查询机器人 ID
   */
  Long selectBotIdBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 查询空间 ID
   */
  Long selectSpaceIdBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 新增会话
   *
   * @param session 会话
   * @return 结果
   */
  int insertSession(@Param("dto") SessionDTO session);

  /**
   * 修改会话
   *
   * @param session 会话
   * @return 结果
   */
  int updateSession(@Param("dto") SessionDTO session);

  /**
   * 获取用户最近的一次会话的 ID
   */
  Long selectLatestSessionId(@Param("botId") Long botId, @Param("userId") Long userId, @Param("extSystemId") Long extSystemId);

  /**
   * 根据用户 ID 获取会话列表
   *
   * @return 会话列表
   */
  List<SessionDTO> listSessions(@Param("query") ChatSessionQueryParams params);

  /**
   * 根据用户 ID 获取会话列表
   *
   * @return 会话列表
   */
  List<SessionDTO> selectSessionList(@Param("query") ChatSessionQueryParams params);

  /**
   * 根据广场应用 ID 获取会话
   *
   * @param spaceId 空间 ID
   * @param platBotId 广场应用 ID
   * @param userId 用户 ID
   * @return 会话
   */
  SessionDTO getSessionByPlatBotId(@Param("spaceId") Long spaceId, @Param("platBotId") Long platBotId, @Param("userId") Long userId);

  /**
   * 根据群组 ID 获取会话
   *
   * @param groupId 群组 ID (字符串格式，存储在 remark 中)
   * @param spaceId 空间 ID
   * @param userId 用户 ID
   * @return 会话
   */
  SessionDTO getSessionByGroupId(@Param("groupId") String groupId, @Param("spaceId") Long spaceId, @Param("userId") Long userId);

  /**
   * 查询会话及其关联的智能体列表
   *
   * @param query 查询参数
   * @param rowBounds 分页参数
   * @return 会话列表
   */
  Page<SessionSceneDTO> selectSessionScenePage(@Param("query") ChatMessageQueryParams query, RowBounds rowBounds);

  /**
   * 根据会话 ID 获取关联的智能体列表
   *
   * @param sessionId 会话 ID
   * @return 智能体列表
   */
  List<SessionSceneDTO> selectSceneBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 获取会话摘要
   */
  String selectCompressedSummary(@Param("sessionId") Long sessionId);

  /**
   * 更新会话摘要
   */
  int updateCompressedSummary(@Param("sessionId") Long sessionId, @Param("compressedSummary") String compressedSummary);
}
