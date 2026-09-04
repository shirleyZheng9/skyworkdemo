package com.iwhalecloud.bote.mapper.agent;

import com.iwhalecloud.bote.entity.agent.SessionStateEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 会话状态 Mapper
 *
 * @author bianjp
 * @since 2026-04-13
 */
public interface SessionStateMapper {

  /**
   * 插入会话状态
   */
  int insert(@Param("entity") SessionStateEntity entity);

  /**
   * 更新会话状态
   */
  int update(@Param("entity") SessionStateEntity entity);

  /**
   * 根据会话 ID 查询所有会话状态
   */
  List<SessionStateEntity> selectBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 根据会话 ID 和作用域查询会话状态
   */
  @Nullable
  SessionStateEntity selectBySessionIdAndScope(@Param("sessionId") Long sessionId, @Param("scope") String scope);

  /**
   * 根据会话 ID 和作用域删除会话状态
   */
  int deleteBySessionIdAndScope(@Param("sessionId") Long sessionId, @Param("scope") String scope);

  /**
   * 根据会话 ID 列表批量删除会话状态
   */
  int deleteBySessionIds(@Param("sessionIds") List<Long> sessionIds);
}
