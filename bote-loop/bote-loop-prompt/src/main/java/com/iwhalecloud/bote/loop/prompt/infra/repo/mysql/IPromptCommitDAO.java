package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql;


import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDCommitVersionPair;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Prompt提交版本数据访问对象接口
 * 迁移对应关系: Go语言IPromptCommitDAO
 * - 功能: Prompt提交版本的数据库操作接口
 * - 方法定义:
 * * create: 创建提交版本
 * * get: 根据PromptID和版本获取提交版本
 * * mGet: 批量获取提交版本
 * * list: 分页查询提交版本
 * <p>
 * Java实现说明:
 * - 对应Go的IPromptCommitDAO接口
 * - 使用MyBatis进行数据库操作
 * - 支持读写分离和缓存
 * - 参数验证和错误处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java 方法参数
 * - Go db.Option -> Java DBOption
 * - Go error -> Java Exception
 * - Go *model.PromptCommit -> Java PromptCommitPO
 */
public interface IPromptCommitDAO {

  /**
   * 创建提交版本
   * 迁移对应关系: Go语言Create方法
   * - 功能: 创建新的提交版本记录
   * - 参数: promptCommitPO - 提交版本数据对象, timeNow - 当前时间
   * - 返回: 无异常表示成功
   *
   * @param promptCommitPO 提交版本数据对象
   * @param timeNow 当前时间
   */
  void create(PromptCommitEntity promptCommitPO, Date timeNow);

  /**
   * 获取提交版本
   * 迁移对应关系: Go语言Get方法
   * - 功能: 根据PromptID和版本获取提交版本
   * - 参数: promptId - Prompt ID, commitVersion - 提交版本
   * - 返回: 提交版本对象，不存在返回null
   *
   * @param promptId Prompt ID
   * @param commitVersion 提交版本
   * @return 提交版本对象，不存在返回null
   */
  PromptCommitEntity get(Long promptId, String commitVersion);

  /**
   * 批量获取提交版本
   * 迁移对应关系: Go语言MGet方法
   * - 功能: 批量获取多个提交版本
   * - 参数: pairs - PromptID和提交版本对列表
   * - 返回: 提交版本映射表
   *
   * @param pairs PromptID和提交版本对列表
   * @return 提交版本映射表
   */
  Map<PromptIDCommitVersionPair, PromptCommitEntity> mGet(List<PromptIDCommitVersionPair> pairs);

  /**
   * 分页查询提交版本
   * 迁移对应关系: Go语言List方法
   * - 功能: 根据条件分页查询提交版本
   * - 参数: param - 查询参数
   * - 返回: 提交版本列表
   *
   * @param param 查询参数
   * @return 提交版本列表
   */
  List<PromptCommitEntity> list(ListCommitParam param);
}
