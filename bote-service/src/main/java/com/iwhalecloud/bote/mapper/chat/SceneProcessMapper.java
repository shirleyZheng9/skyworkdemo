package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.SceneProcessDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会话场景进度管理
 *
 * @author chen.linfa
 * @since 2024-12-17
 */
public interface SceneProcessMapper {
  /**
   * 新增记录
   *
   * @param dto 记录
   * @return 结果
   */
  int insertSceneProcess(@Param("dto") SceneProcessDTO dto);

  /**
   * 更新对话场景状态
   *
   * @param dto 记录
   * @return 结果
   */
  int updateChatSceneStatus(@Param("dto") SceneProcessDTO dto);

  /**
   * 更新智能体执行进度
   */
  int updateSceneProcess(@Param("sceneId") Long sceneId, @Param("contextId") String contextId, @Param("chatStatus") String chatStatus);

  /**
   * 获取最近一个未完成的场景
   *
   * @param sessionId 会话 ID
   * @return 最新用户消息
   */
  SceneProcessDTO selectLastRunningScene(@Param("sessionId") Long sessionId);

  /**
   * 通过会话 ID 查询归属的机器人配置的默认场景
   *
   * @param sessionId 会话 ID
   * @return 默认场景
   */
  SceneProcessDTO selectDefaultSceneBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 通过会话 ID 查询归属应用的智能体列表
   *
   * @param sessionId 会话 ID
   */
  List<SceneProcessDTO> selectSceneListBySessionId(@Param("sessionId") Long sessionId);

  /**
   * 获取最近未完成的 5 个场景
   *
   * @param sessionId 会话 ID
   * @return 未完成的场景
   */
  List<SceneProcessDTO> selectRunningScene(@Param("sessionId") Long sessionId);

  /**
   * 根据条件获取最近一个场景
   *
   * @param sessionId 会话 ID
   * @param contextId 上下文 ID
   * @return 场景
   */
  SceneProcessDTO selectLastScene(@Param("sessionId") Long sessionId, @Param("contextId") String contextId);

  /**
   * 根据条件查询历史前 3 个场景
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @param userId 用户 ID
   * @return 前 3 场景
   */
  List<SceneProcessDTO> selectTop3Scene(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 检查是否存在执行中的智能体
   */
  boolean existsRunningScene(@Param("dto") SceneProcessDTO sceneProcess);
}
