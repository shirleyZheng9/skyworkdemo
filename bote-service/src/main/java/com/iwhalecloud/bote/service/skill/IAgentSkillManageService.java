package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.SquareBotSkillRowDTO;
import com.iwhalecloud.bote.dto.ontology.OntoSceneDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyActionDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyAppDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyObjectDTO;
import com.iwhalecloud.bote.dto.ontology.OntologyRuleDTO;
import com.iwhalecloud.bote.dto.ontology.query.OntologyQueryParams;
import com.iwhalecloud.bote.dto.skill.AgentSkillDirDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillFileDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillPublishResultDTO;
import com.iwhalecloud.bote.dto.skill.AgentSkillTreeNodeDTO;
import com.iwhalecloud.bote.dto.skill.query.AgentSkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Agent Skill 管理服务
 *
 * @author bianjp
 * @since 2026-02-03
 */
public interface IAgentSkillManageService {
  /**
   * 查询 Agent Skill
   */
  AgentSkillDTO findAgentSkill(Long tenantId, Long skillId);

  /**
   * 保存 Agent Skill
   */
  ResultVO<AgentSkillDTO> saveAgentSkill(AgentSkillDTO skill);

  /**
   * 删除 Agent Skill
   */
  ResultVO<Void> deleteAgentSkill(Long tenantId, Long skillId);

  /**
   * 分页查询 Agent Skill
   */
  PageInfo<AgentSkillDTO> queryAgentSkillPage(AgentSkillQueryParams queryParams);

  /**
   * 上传 Agent Skill 文件
   */
  ResultVO<FileInfoVO> uploadAgentSkillFile(MultipartFile file);

  /**
   * 上传 Agent Skill 文件
   */
  ResultVO<FileInfoVO> uploadAgentSkillFile(File file, String originalFilename);

  /**
   * 批量查询：租户下多个 bot 对已上架广场技能（data_from=10A）的安装版本
   *
   * @param botIds botId 列表，空则返回空列表
   */
  List<SquareBotSkillRowDTO> listInstalledSkillVersionForSquareBots(Long tenantId, Long skillSquareId, List<Long> botIds);

  /**
   * 查询指定租户、Bot 已安装的技能广场技能 ID（agent_skill_square.skill_id）列表
   */
  List<Long> listInstalledSkillSquareIds(Long tenantId, Long botId);

  /**
   * 查询当前空间下用户的技能
   *
   * @param params 查询参数
   * @return 技能列表
   */
  PageInfo<AgentSkillDTO> getUserSpaceSkillPage(AgentSkillQueryParams params);

  /**
   * 保存目录
   *
   * @param dir 目录
   * @return 目录
   */
  ResultVO<AgentSkillDirDTO> saveAgentSkillDir(AgentSkillDirDTO dir);

  /**
   * 删除目录（级联删除目录下文件）
   *
   * @param dirId 目录ID
   * @param skillId 技能ID
   * @param tenantId 租户ID
   */
  ResultVO<Void> deleteAgentSkillDir(Long tenantId, Long skillId, Long dirId);

  /**
   * 保存文本文件（保存后刷新槽位）
   *
   * @param file 文件
   */
  ResultVO<AgentSkillFileDTO> saveAgentSkillFile(AgentSkillFileDTO file);

  /**
   * 上传文件并落库
   *
   * @param file 文件
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @param dirId 目录ID
   */
  ResultVO<AgentSkillFileDTO> uploadAgentSkillFile(MultipartFile file, Long tenantId, Long skillId, Long dirId);

  /**
   * 删除文件
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   * @param skillFileId 文件ID
   */
  ResultVO<Void> deleteAgentSkillFile(Long tenantId, Long skillId, Long skillFileId);

  /**
   * 查询文件详情（文本含 file_content）
   *
   * @param tenantId 租户ID
   * @param skillFileId 文件ID
   */
  ResultVO<AgentSkillFileDTO> getAgentSkillFileContent(Long tenantId, Long skillFileId);

  /**
   * 查询目录+文件树
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   */
  ResultVO<List<AgentSkillTreeNodeDTO>> queryAgentSkillTree(Long tenantId, Long skillId);

  /**
   * 检测存量数据：无目录文件记录时从模板或已有 zip 初始化
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   */
  ResultVO<Void> checkAndInitAgentSkill(Long tenantId, Long skillId);

  /**
   * 一键发布：打包 DB 目录文件数据为 zip 并回写 bt_agent_skill.file_info_id
   *
   * @param tenantId 租户ID
   * @param skillId 技能ID
   */
  ResultVO<AgentSkillPublishResultDTO> publishAgentSkill(Long tenantId, Long skillId);

  /**
   * 分页查询本体应用
   *
   * @param queryParams 查询参数
   * @return 本体应用分页列表
   */
  PageInfo<OntologyAppDTO> queryOntologyAppPage(OntologyQueryParams queryParams);

  /**
   * 分页查询本体场景
   *
   * @param queryParams 查询参数
   * @return 本体场景分页列表
   */
  PageInfo<OntoSceneDTO> queryOntologyScenePage(OntologyQueryParams queryParams);

  /**
   * 获取本体场景详情
   *
   * @param queryParams 查询参数
   * @return 本体场景详情
   */
  OntoSceneDTO getOntologySceneDetail(OntologyQueryParams queryParams);

  /**
   * 分页查询本体规则
   *
   * @param queryParams 查询参数
   * @return 本体规则分页列表
   */
  PageInfo<OntologyRuleDTO> queryOntologyRulePage(OntologyQueryParams queryParams);

  /**
   * 获取本体对象分页
   *
   * @param queryParams 查询参数
   * @return 本体对象分页列表
   */
  PageInfo<OntologyObjectDTO> queryOntologyObjectPage(OntologyQueryParams queryParams);

  /**
   * 获取本体动作分页
   *
   * @param queryParams 查询参数
   * @return 本体动作分页列表
   */
  PageInfo<OntologyActionDTO> queryOntologyActionPage(OntologyQueryParams queryParams);
}

