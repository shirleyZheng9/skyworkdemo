package com.iwhalecloud.bote.mapper.scene;

import com.iwhalecloud.bote.dto.scene.FlowSceneProcessDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatMessageDTO;
import com.iwhalecloud.bote.entity.scene.SceneChatMessageEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 场景会话消息相关数据库操作
 *
 * @author bianjp
 * @since 2024-08-06
 */
public interface SceneChatMessageMapper {

  /**
   * 批量插入消息
   */
  int batchInsertMessage(@Param("messages") List<SceneChatMessageEntity> messages);

  /**
   * 根据对话标识查询历史消息
   */
  List<SceneChatMessageDTO> selectSimpleMessagesByContextId(@Param("contextId") String contextId);

  /**
   * 新增智能体执行进度
   */
  int insertProcess(@Param("dto") FlowSceneProcessDTO process);

  /**
   * 查询智能体执行进度
   */
  FlowSceneProcessDTO getProcess(@Param("contextId") String contextId);

  /**
   * 更新智能体执行进度
   */
  int updateProcess(@Param("sceneId") Long sceneId, @Param("contextId") String contextId, @Param("flowStatus") String flowStatus);
}
