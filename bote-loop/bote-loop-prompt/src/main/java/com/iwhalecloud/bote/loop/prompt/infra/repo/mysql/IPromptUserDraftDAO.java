package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql;


import com.iwhalecloud.bote.entity.loop.prompt.PromptUserDraftEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDUserIDPair;
import java.util.List;
import java.util.Map;

/**
 * Prompt用户草稿数据访问对象接口
 * 迁移对应关系: Go语言IPromptUserDraftDAO
 * - 功能: 用户草稿的数据库操作接口
 * - 方法定义:
 * * create: 创建草稿
 * * delete: 删除草稿
 * * get: 根据promptID和userID获取草稿
 * * getById: 根据草稿ID获取草稿
 * * mGet: 批量获取草稿
 * * update: 更新草稿
 * <p>
 * Java实现说明:
 * - 对应Go的IPromptUserDraftDAO接口
 * - 使用MyBatis进行数据库操作
 * - 支持读写分离和缓存
 * - 参数验证和错误处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java 方法参数
 * - Go db.Option -> Java DBOption
 * - Go error -> Java Exception
 * - Go *model.PromptUserDraft -> Java PromptUserDraftPO
 */
public interface IPromptUserDraftDAO {

  /**
   * 创建用户草稿
   * 迁移对应关系: Go语言Create方法
   * - 功能: 创建新的用户草稿记录
   * - 参数: promptDraftPO - 草稿数据对象
   * - 返回: 无异常表示成功
   *
   * @param promptDraftPO 草稿数据对象
   */
  void create(PromptUserDraftEntity promptDraftPO);

  /**
   * 删除用户草稿
   * 迁移对应关系: Go语言Delete方法
   * - 功能: 根据草稿ID删除草稿
   * - 参数: draftId - 草稿ID
   * - 返回: 无异常表示成功
   *
   * @param draftId 草稿ID
   */
  void delete(Long draftId);

  /**
   * 获取用户草稿
   * 迁移对应关系: Go语言Get方法
   * - 功能: 根据空间ID、promptID和userID获取草稿
   * - 参数: spaceId - 空间ID, promptId - Prompt ID, userId - 用户ID
   * - 返回: 草稿对象，不存在返回null
   *
   * @param spaceId 空间ID
   * @param promptId Prompt ID
   * @param userId 用户ID
   * @return 草稿对象，不存在返回null
   */
  PromptUserDraftEntity get(Long spaceId, Long promptId, String userId);

  /**
   * 根据ID获取用户草稿
   * 迁移对应关系: Go语言GetByID方法
   * - 功能: 根据草稿ID获取草稿
   * - 参数: draftId - 草稿ID
   * - 返回: 草稿对象，不存在返回null
   *
   * @param draftId 草稿ID
   * @return 草稿对象，不存在返回null
   */
  PromptUserDraftEntity getById(Long draftId);

  /**
   * 批量获取用户草稿
   * 迁移对应关系: Go语言MGet方法
   * - 功能: 批量获取多个草稿
   * - 参数: pairs - PromptID和UserID对列表
   * - 返回: 草稿映射表
   *
   * @param pairs PromptID和UserID对列表
   * @return 草稿映射表
   */
  Map<PromptIDUserIDPair, PromptUserDraftEntity> mGet(List<PromptIDUserIDPair> pairs);

  /**
   * 更新用户草稿
   * 迁移对应关系: Go语言Update方法
   * - 功能: 更新草稿内容
   * - 参数: promptDraftPO - 草稿数据对象
   * - 返回: 无异常表示成功
   *
   * @param promptDraftPO 草稿数据对象
   */
  void update(PromptUserDraftEntity promptDraftPO);
}
