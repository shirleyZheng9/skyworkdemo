package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.BotSceneParamDTO;
import com.iwhalecloud.bote.dto.bot.BotScenePromptDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneSkillDTO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 场景关联配置 Mapper
 *
 * @author chen.linfa
 * @since 2024-09-14
 */
public interface BotSceneRelaManageMapper {

  /**
   * 批量新增场景变量
   *
   * @param param 场景变量
   * @return 结果
   */
  int insertSceneParam(@Param("dto") BotSceneParamDTO param);

  /**
   * 修改场景变量
   *
   * @param param 场景变量
   * @return 结果
   */
  int updateSceneParam(@Param("dto") BotSceneParamDTO param);

  /**
   * 根据场景 ID 查询场景变量
   */
  BotSceneParamDTO getSceneParam(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 根据ID列表查询场景变量
   */
  List<BotSceneParamDTO> selectSceneParamsByIds(@Param("ids") Collection<Long> ids, @Param("tenantId") Long tenantId);

  /**
   * 新增场景提示词
   *
   * @param scenePrompt 场景提示词
   * @return 结果
   */
  int insertScenePrompt(@Param("dto") BotScenePromptDTO scenePrompt);

  /**
   * 修改场景提示词
   *
   * @param scenePrompt 场景提示词
   * @return 结果
   */
  int updateScenePrompt(@Param("dto") BotScenePromptDTO scenePrompt);

  /**
   * 根据场景 ID 查询提示词
   */
  BotScenePromptDTO getScenePrompt(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);

  /**
   * 批量新增场景技能
   *
   * @param sceneSkills 场景技能
   * @return 结果
   */
  int batchInsertSceneSkill(@Param("list") List<BotSceneSkillDTO> sceneSkills);

  /**
   * 修改场景技能
   *
   * @param sceneSkill 场景技能
   * @return 结果
   */
  int updateSceneSkill(@Param("dto") BotSceneSkillDTO sceneSkill);

  /**
   * 根据场景 ID 查询技能列表
   */
  List<BotSceneSkillDTO> selectSceneSkillList(@Param("tenantId") Long tenantId, @Param("sceneId") Long sceneId);
}
