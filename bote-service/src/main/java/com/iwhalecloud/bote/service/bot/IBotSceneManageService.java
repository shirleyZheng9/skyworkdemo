package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneBatchOperDTO;
import com.iwhalecloud.bote.dto.bot.RecommendedSceneDTO;
import com.iwhalecloud.bote.dto.bot.query.BotApplyParams;
import com.iwhalecloud.bote.dto.bot.query.BotSceneQueryParams;
import com.iwhalecloud.bote.dto.skill.StandardServiceDiffViewDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * 智能体管理服务
 *
 * @author chen.linfa
 * @since 2024-08-02
 */
public interface IBotSceneManageService {

  /**
   * 保存智能体基本信息
   *
   * @param scene 智能体
   * @return 结果
   */
  ResultVO<BotSceneDTO> saveSceneInfo(BotSceneDTO scene);

  /**
   * 一句话生成提示词模式智能体（智能体）
   *
   * @param scene 智能体
   * @return 结果
   */
  ResultVO<BotSceneDTO> aiGenPromptScene(BotSceneDTO scene);

  /**
   * 查询智能生成智能体进度
   *
   * @param processId 进度ID
   * @return 进度信息
   */
  ResultVO<Map<String, Object>> getAiGenProcess(String processId);

  /**
   * 上下架智能体
   *
   * @param sceneStatus 智能体状态
   * @param sceneId 智能体ID
   * @param tenantId 租户 ID
   * @return 结果
   */
  ResultVO<Void> publishScene(String sceneStatus, Long sceneId, Long tenantId);

  /**
   * 批量上下架智能体
   *
   * @param sceneOperDTO 智能体操作DTO
   * @return 结果
   */
  List<BotSceneDTO> publishSceneBatch(BotSceneBatchOperDTO sceneOperDTO);

  /**
   * 保存智能体，用于修改智能体
   *
   * @param scene 智能体
   * @return 结果
   */
  ResultVO<BotSceneDTO> saveScene(BotSceneDTO scene);

  /**
   * 删除智能体
   *
   * @param tenantId 租户 ID
   * @param sceneId 智能体 ID
   * @return 结果
   */
  ResultVO<Void> removeScene(Long tenantId, Long sceneId);

  /**
   * 批量删除智能体
   *
   * @param sceneOperDTO 智能体操作DTO
   * @return 结果
   */
  List<BotSceneDTO> removeSceneBatch(BotSceneBatchOperDTO sceneOperDTO);

  /**
   * 批量移动智能体
   *
   * @param sceneOperDTO 智能体操作DTO
   * @return 结果
   */
  List<BotSceneDTO> moveSceneBatch(BotSceneBatchOperDTO sceneOperDTO);

  /**
   * 获取智能体基本信息
   *
   * @param tenantId 租户 ID
   * @param sceneId 智能体 ID
   * @return 智能体基本信息
   */
  @Nullable
  BotSceneDTO getSceneInfo(Long tenantId, Long sceneId);

  /**
   * 获取智能体
   *
   * @param tenantId 租户 ID
   * @param sceneId 智能体 ID
   * @param withChildren 是否查询子节点
   * @return 智能体
   */
  BotSceneDTO getScene(Long tenantId, Long sceneId, boolean withChildren);

  /**
   * 查询智能体的基本信息
   */
  BotSceneDTO getSimpleScene(Long tenantId, Long sceneId);

  /**
   * 查询智能体列表（分页）
   *
   * @param queryParams 查询条件
   * @return 智能体列表
   */
  PageInfo<BotSceneDTO> queryScenePage(BotSceneQueryParams queryParams);

  /**
   * 查询智能体列表
   *
   * @param queryParams 查询条件
   * @return 智能体列表
   */
  List<BotSceneDTO> querySceneList(BotSceneQueryParams queryParams);

  /**
   * 查询智能体列表，用于智能体加入应用
   *
   * @param queryParams 查询条件
   * @return 智能体列表
   */
  PageInfo<BotSceneDTO> queryScenePageForBot(BotSceneQueryParams queryParams);

  /**
   * 查询智能体列表（分页），用于百应平台
   *
   * @param queryParams 查询条件
   * @return 智能体列表
   */
  PageInfo<BotSceneDTO> beyondQueryScenePage(BotSceneQueryParams queryParams);

  /**
   * 根据智能体配置的技能、大纲树，生成智能体提示词
   *
   * @param scene 智能体
   * @return 提示词
   */
  String generatePrompt(BotSceneDTO scene);

  /**
   * 根据用户ID，查询机器人下推荐的智能体列表
   *
   * @param params 查询条件
   * @return 智能体列表
   */
  List<RecommendedSceneDTO> queryRecommendedScenes(BotSceneQueryParams params);

  /**
   * 查找智能体历史版本详情
   *
   * @param logId 日志 ID
   * @param tenantId 租户 ID
   * @return 智能体
   */
  ResultVO<StandardServiceDiffViewDTO> findSceneVersion(Long logId, Long tenantId);

  /**
   * 比较两次修改记录的差异
   *
   * @param tenantId 租户ID
   * @param oldLogId 旧版本日志ID
   * @param logId 日志ID
   * @return 结果
   */
  ResultVO<StandardServiceDiffViewDTO> diffSceneOperLog(Long tenantId, Long oldLogId, Long logId);

  /**
   * 发起申请，生成新的智能体应用，或加入已有应用
   *
   * @param apply 申请信息
   * @return 结果
   */
  ResultVO<Long> publish(BotApplyParams apply);

  /**
   * 自动生成流程步骤
   *
   * @param scene 智能体
   * @return 结果
   */
  ResultVO<List<SimpleFlowStepDTO>> generateFlowStep(BotSceneDTO scene);

  /**
   * 根据日志ID，查询智能体历史版本详情
   *
   * @param logId 日志ID
   * @param tenantId 租户ID
   * @return 智能体
   */
  BotSceneDTO getBotSceneveisonDetail(Long logId, Long tenantId);
}
