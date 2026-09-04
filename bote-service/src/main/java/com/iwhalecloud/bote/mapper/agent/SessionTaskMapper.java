package com.iwhalecloud.bote.mapper.agent;

import com.iwhalecloud.bote.entity.agent.SessionTaskEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 会话任务 Mapper
 *
 * @author bianjp
 * @since 2026-04-10
 */
public interface SessionTaskMapper {

  /**
   * 插入任务
   */
  int insertTask(@Param("entity") SessionTaskEntity entity);

  /**
   * 更新任务
   */
  int updateTaskById(@Param("entity") SessionTaskEntity entity);

  /**
   * 更新任务依赖
   */
  int updateTaskBlocks(@Param("id") Long id, @Param("blocksJson") String blocksJson);

  /**
   * 更新任务依赖
   */
  int updateTaskBlockedBy(@Param("id") Long id, @Param("blockedByJson") String blockedByJson);

  /**
   * 删除任务
   */
  int deleteTaskById(@Param("id") Long id);

  /**
   * 根据会话 ID 列表批量删除任务
   */
  int deleteTasksBySessionIds(@Param("sessionIds") List<Long> sessionIds);

  /**
   * 根据 ID 查询任务
   */
  @Nullable
  SessionTaskEntity selectTaskById(@Param("id") Long id);

  /**
   * 根据会话 ID 和任务 ID 查询任务
   */
  @Nullable
  SessionTaskEntity selectTaskBySessionIdAndTaskId(@Param("sessionId") Long sessionId, @Param("taskId") Integer taskId);

  /**
   * 根据会话 ID 查询所有任务（未删除的）
   */
  List<SessionTaskEntity> selectTasksBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 查询最大任务 ID（包括已删除的）
   */
  @Nullable
  Integer selectMaxTaskId(@Param("sessionId") Long sessionId);

}
