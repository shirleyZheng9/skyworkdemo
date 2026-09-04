package com.iwhalecloud.bote.mapper.cutover;

import com.iwhalecloud.bote.doc.module.document.entity.DocumentContributorEntity;
import com.iwhalecloud.bote.dto.base.AppPublishDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.BotUserExperienceDTO;
import com.iwhalecloud.bote.dto.convert.BtDcDocumentDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationMemberDTO;
import com.iwhalecloud.bote.dto.portal.SimpleTenantDTO;
import com.iwhalecloud.bote.dto.skill.SkillFlowWithParamDTO;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.entity.portal.ExternalPortalEntity;
import com.iwhalecloud.bote.entity.portal.UserEntity;
import com.iwhalecloud.bote.entity.skill.SkillFlowEntity;
import com.iwhalecloud.bote.entity.skill.SkillFunctionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 割接功能相关的数据库操作
 *
 * @author bianjp
 * @since 2025-03-03
 */
public interface CutOverMapper {

  /**
   * 分页查询包含问题分类节点的工作流
   */
  List<SkillFlowWithParamDTO> selectFlowsWithQuestionClassifierNode();

  /**
   * 更新工作流的流程图和 DSL
   */
  int updateFlowGraphAndDsl(@Param("flow") SkillFlowWithParamDTO flow);

  /**
   * 收集需要割接的机器人数据
   */
  List<BotDTO> selectBot();

  /**
   * 收集机器人下有效的场景数据
   */
  List<BotSceneDTO> selectBotScene(@Param("tenantId") Long tenantId, @Param("botId") Long botId);

  /**
   * 分页查询需要加解密的用户数据
   */
  List<UserEntity> selectUsersForEncryption();

  /**
   * 更新用户加密字段
   */
  int updateUserEncryptFields(@Param("user") UserEntity user);

  /**
   * 收集常见问题指令数据
   */
  List<BotUserExperienceDTO> selectBotUserExperience();

  /**
   * 查询所有租户
   */
  List<SimpleTenantDTO> selectAllTenants(@Param("ignoreSpaceId") boolean ignoreSpaceId);

  /**
   * 查询包含 Groovy 脚本的智能体
   */
  List<BotSceneEntity> selectScenesWithGroovy();

  /**
   * 查询包含 Groovy 脚本的工作流
   */
  List<SkillFlowEntity> selectFlowsWithGroovy();

  /**
   * 查询包含 Groovy 脚本的服务函数
   */
  List<SkillFunctionEntity> selectFunctionsWithGroovy();

  /**
   * 查询包含 Groovy 脚本的门户配置
   */
  List<ExternalPortalEntity> selectExternalPortalsWithGroovy();

  int updateTenantSpaceId();

  /**
   * 收集需要割接的助手成员
   */
  List<OrganizationMemberDTO> selectAllOrgMember();

  /**
   * 收集需要割接的组织成员角色数据
   */
  List<OrganizationMemberDTO> selectAllOrgMemberRole();

  /**
   * 收集需要割接的应用发布免登录链接数据
   */
  List<AppPublishDTO> selectAppPublishUrls();

  /**
   * 更新应用发布免登录链接的URL
   */
  int updateAppPublishUrl(@Param("publishId") Long publishId, @Param("newUrl") String newUrl);

  /**
   * 收集需要割接的文档ID数据
   */
  List<String> selectDocumentIds();


  /**
   * 收集需要割接的文档数据
   */
  List<BtDcDocumentDTO> selectDocumentByIds(@Param("ids") List<String> ids);

  /**
   * 批量插入文档贡献者数据
   */
  int batchInsertDocumentContributor(@Param("contributors") List<DocumentContributorEntity> documentContributors);

}
