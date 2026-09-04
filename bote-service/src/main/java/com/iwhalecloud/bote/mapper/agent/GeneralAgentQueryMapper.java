package com.iwhalecloud.bote.mapper.agent;

import com.iwhalecloud.bote.dto.agent.SimpleAiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 用户通用智能体配置查询 Mapper
 *
 * @author chen.linfa
 * @since 2026--03-10
 */
public interface GeneralAgentQueryMapper {

  /**
   * 查询用户启用的 模型 ID
   */
  Long getModelId(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 查询空间启用的模型 ID
   */
  Long getSpaceModelId(@Param("spaceId") Long spaceId);

  /**
   * 查询启用的模型 ID 归属的租户 ID
   */
  Long getModelTenantId(@Param("spaceId") Long spaceId, @Param("modelId") Long modelId);

  /**
   * 查询平台启用的模型 ID
   */
  Long getPlatformModelId();

  /**
   * 查询用户配置的环境变量
   */
  List<SimpleAiEnvVariableDTO> selectEnvVariableList(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 查询用户启用的 MCP ID 列表
   */
  List<Long> selectEnabledMcpIds(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId,
    @Param("platform") boolean platform);

  /**
   * 查询用户启用的 SKILLS ID 列表
   */
  List<Long> selectEnabledSkillIds(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId,
    @Param("platform") boolean platform);

  /**
   * 根据文件名称，获取用户自定义的提示词
   */
  String getCustomPrompt(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId,
    @Param("fileName") String fileName);

  /**
   * 查询用户自定义的提示词
   */
  List<SimpleAiWorkspaceDTO> selectCustomPromptList(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);
}
