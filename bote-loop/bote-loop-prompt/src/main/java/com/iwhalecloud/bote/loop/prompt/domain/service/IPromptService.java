package com.iwhalecloud.bote.loop.prompt.domain.service;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Message;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.entity.VariableVal;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ExecuteParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptKeyVersionPair;
import java.util.List;
import java.util.Map;

/**
 * Prompt领域服务接口
 * 迁移对应关系: Go语言modules/prompt/domain/service.IPromptService
 * - 功能: 核心Prompt业务逻辑服务接口
 * - 主要方法:
 * * formatPrompt - 格式化Prompt消息
 * * executeStreaming - 流式执行Prompt
 * * execute - 执行Prompt
 * * mCompleteMultiModalFileURL - 完成多模态文件URL
 * * mGetPromptIDs - 根据prompt key获取prompt id
 * * mParseCommitVersionByPromptKey - 解析提交版本
 * <p>
 * Java实现说明:
 * - 对应Go的service.IPromptService接口
 * - 使用Java接口定义，包含所有核心业务方法
 * - 方法签名适配Java类型系统
 * - 使用Spring的依赖注入机制
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针类型 -> Java对象引用
 */
public interface IPromptService {

  /**
   * 格式化Prompt消息
   * 迁移对应关系: Go语言service.IPromptService.FormatPrompt
   * - 功能: 根据Prompt模板和变量值格式化消息
   * - 参数: Prompt对象、消息列表、变量值列表
   * - 返回: 格式化后的消息列表
   */
  List<Message> formatPrompt(Prompt prompt, List<Message> messages, List<VariableVal> variableVals);

  /**
   * 执行Prompt
   * 迁移对应关系: Go语言service.IPromptService.Execute
   * - 功能: 执行Prompt并返回结果
   * - 参数: 执行参数
   * - 返回: 执行结果
   */
  Reply execute(ExecuteParam param);

  /**
   * 完成多模态文件URL
   * 迁移对应关系: Go语言service.IPromptService.MCompleteMultiModalFileURL
   * - 功能: 处理多模态文件URL
   * - 参数: 消息列表
   */
  void mCompleteMultiModalFileURL(List<Message> messages);

  /**
   * 根据prompt key获取prompt id
   * 迁移对应关系: Go语言service.IPromptService.MGetPromptIDs
   * - 功能: 批量获取Prompt ID映射
   * - 参数: 空间ID、Prompt Key列表
   * - 返回: Prompt Key到ID的映射
   */
  Map<String, Long> mGetPromptIDs(Long spaceId, List<String> promptKeys);

  /**
   * 根据prompt key解析提交版本
   * 迁移对应关系: Go语言service.IPromptService.MParseCommitVersionByPromptKey
   * - 功能: 解析提交版本，如果为空则使用最新版本
   * - 参数: 空间ID、Prompt Key版本对列表
   * - 返回: Prompt Key版本对到提交版本的映射
   */
  Map<PromptKeyVersionPair, String> mParseCommitVersionByPromptKey(Long spaceId, List<PromptKeyVersionPair> pairs);
}
