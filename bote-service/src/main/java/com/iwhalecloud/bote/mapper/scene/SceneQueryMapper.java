package com.iwhalecloud.bote.mapper.scene;

import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.SceneIntentDTO;
import com.iwhalecloud.bote.dto.bot.SimpleBotSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.scene.SimpleClawEnvVariableDTO;
import com.iwhalecloud.bote.dto.scene.SimpleDslInfoDTO;
import com.iwhalecloud.bote.dto.scene.SimpleScenePromptDTO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 场景查询相关数据库操作
 *
 * @author bianjp
 * @since 2024-08-05
 */
public interface SceneQueryMapper {
  /**
   * 根据场景 ID 查询提示词
   */
  @Nullable
  SimpleScenePromptDTO selectPromptBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询简单场景的技能列表
   */
  List<BotSceneSkillDTO> selectSkillsBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询知识库场景的知识库 ID 列表
   */
  List<Long> selectKnowledgeIdsBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询知识库场景的技能列表
   */
  List<BotSceneSkillDTO> selectKnowledgeSkillsBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询知识问答场景的配置
   */
  String selectKnowledgeSettingBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 根据场景 ID 查询场景类型和机器人 ID
   */
  @Nullable
  SimpleBotSceneDTO selectSceneTypeAndBotIdBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 根据场景 ID 列表查询场景类型
   */
  List<SimpleBotSceneDTO> selectSceneTypeBySceneIds(@Param("tenantId") Long tenantId, @Param("sceneIds") List<Long> sceneIds);

  /**
   * 根据场景 ID 查询 DSL 信息
   */
  @Nullable
  SimpleDslInfoDTO selectDslBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询机器人下的推荐场景
   *
   * <p>只查询已上架，且没有副驾标签的场景</p>
   */
  List<RecommendedSceneDTO> selectRecommendedScenes(@Param("query") BotSceneQueryParams params);

  /**
   * 根据场景 ID 查询场景基本信息
   */
  SimpleBotSceneDTO selectSceneBySceneId(@Param("tenantId") Long tenantId,
                                         @Param("sceneId") Long sceneId,
                                         @Param("sceneStatus") String sceneStatus,
                                         @Param("jumpLabelName") String jumpLabelName,
                                         @Param("confirmLabelName") String confirmLabelName,
                                         @Param("autoStartLabelName") String autoStartLabelName);

  /**
   * 根据场景 ID 批量查询场景基本信息
   */
  List<SimpleBotSceneDTO> selectScenesBySceneIds(@Param("tenantId") Long tenantId,
                                                 @Param("sceneIds") List<Long> sceneIds,
                                                 @Param("sceneStatus") String sceneStatus,
                                                 @Param("jumpLabelName") String jumpLabelName,
                                                 @Param("confirmLabelName") String confirmLabelName,
                                                 @Param("autoStartLabelName") String autoStartLabelName);

  /**
   * 根据租户 ID 查询意图识别使用的场景列表
   */
  List<SceneIntentDTO> selectSceneIntentListByTenantId(@Param("tenantId") Long tenantId, @Param("excludeLabels") List<String> excludeLabels);

  /**
   * 根据租户 ID 机器人 ID 查询关联的场景
   */
  List<SceneIntentDTO> selectSceneListByBotId(@Param("tenantId") Long tenantId, @Param("botId") Long botId, @Param("excludeLabels") List<String> excludeLabels);

  /**
   * 根据租户 ID 查询意图识别使用的场景列表
   */
  List<SimpleBotSceneDTO> selectSceneListByTenantId(@Param("tenantId") Long tenantId, @Param("excludeLabels") List<String> excludeLabels);

  /**
   * 查询智能体图标
   */
  BotSceneEntity selectSceneIcon(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 查询智能体名称
   */
  String selectSceneName(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 根据场景 ID 查询工作空间
   */
  List<SimpleAiWorkspaceDTO> selectWorkspaceBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId,
                                                      @Param("fileNames") List<String> fileNames);

  /**
   * 根据场景 ID 查询环境变量
   */
  List<SimpleClawEnvVariableDTO> selectEnvVariableBySceneId(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId, @Param("envCode") String envCode);
}
