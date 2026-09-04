package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc;

import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.GetPromptParams;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.LoopPrompt;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.MGetPromptQuery;

import java.util.List;

/**
 * Prompt RPC适配器接口
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/component/rpc/prompt.go
 * - 功能: Prompt相关的RPC调用接口
 * - 主要方法:
 * * getPrompt - 获取Prompt
 * * mGetPrompt - 批量获取Prompt
 * * listPrompt - 分页查询Prompt列表
 * * listPromptVersion - 分页查询Prompt版本列表
 * * executePrompt - 执行Prompt
 * <p>
 * Java实现说明:
 * - 对应Go的IPromptRPCAdapter接口
 * - 使用Spring组件注解
 * - 支持Prompt的完整CRUD操作
 * - 支持版本管理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go *entity.LoopPrompt -> Java LoopPrompt
 * - Go []*entity.LoopPrompt -> Java List<LoopPrompt>
 */
public interface IPromptRPCAdapter {

  /**
   * 获取Prompt
   * 迁移对应关系: Go语言GetPrompt方法
   * - 功能: 根据ID和参数获取Prompt
   * - 参数: spaceId - 空间ID
   * - 参数: promptId - Prompt ID
   * - 参数: params - 查询参数
   * - 返回: Prompt对象
   * - 异常: 查询失败时抛出BssException
   */
  LoopPrompt getPrompt(Long spaceId, Long promptId, GetPromptParams params);

  /**
   * 批量获取Prompt
   * 迁移对应关系: Go语言MGetPrompt方法
   * - 功能: 根据查询条件批量获取Prompt
   * - 参数: spaceId - 空间ID
   * - 参数: promptQueries - 查询条件列表
   * - 返回: Prompt列表
   * - 异常: 查询失败时抛出BssException
   */
  List<LoopPrompt> mGetPrompt(Long spaceId, List<MGetPromptQuery> promptQueries);

  /**
   * 分页查询Prompt列表
   * 迁移对应关系: Go语言ListPrompt方法
   * - 功能: 根据条件分页查询Prompt列表
   * - 参数: param - 查询参数
   * - 返回: Prompt列表
   * - 异常: 查询失败时抛出BssException
   */
  List<LoopPrompt> listPrompt(ListPromptParam param);

  /**
   * 分页查询Prompt版本列表
   * 迁移对应关系: Go语言ListPromptVersion方法
   * - 功能: 根据条件分页查询Prompt版本列表
   * - 参数: param - 查询参数
   * - 返回: 版本信息列表和下一页游标
   * - 异常: 查询失败时抛出BssException
   */
  ListPromptVersionResult listPromptVersion(ListPromptVersionParam param);

  /**
   * 执行Prompt
   * 迁移对应关系: Go语言ExecutePrompt方法
   * - 功能: 执行Prompt并返回结果
   * - 参数: spaceId - 空间ID
   * - 参数: param - 执行参数
   * - 返回: 执行结果
   * - 异常: 执行失败时抛出BssException
   */
  ExecutePromptResult executePrompt(Long spaceId, ExecutePromptParam param);
}
