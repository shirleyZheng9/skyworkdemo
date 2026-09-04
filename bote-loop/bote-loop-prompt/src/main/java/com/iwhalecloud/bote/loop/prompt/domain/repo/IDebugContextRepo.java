package com.iwhalecloud.bote.loop.prompt.domain.repo;

import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugContext;

/**
 * 调试上下文仓库接口
 * 迁移对应关系: Go语言repo.IDebugContextRepo
 * - 功能: 调试上下文的数据访问接口
 * - 主要方法:
 * * saveDebugContext - 保存调试上下文
 * * getDebugContext - 获取调试上下文
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IDebugContextRepo接口
 * - 使用Java接口定义，包含调试上下文数据访问方法
 * - 方法签名适配Java类型系统
 * - 使用Spring的依赖注入机制
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go指针类型 -> Java对象引用
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
public interface IDebugContextRepo {

  /**
   * 保存调试上下文
   * 迁移对应关系: Go语言repo.IDebugContextRepo.SaveDebugContext
   * - 功能: 保存调试上下文信息
   * - 参数: 调试上下文对象
   * - 返回: 无返回值，异常通过异常机制处理
   * <p>
   * 处理逻辑:
   * - 输入验证: 检查调试上下文对象是否有效
   * - 数据持久化: 将调试上下文保存到数据库
   * - 异常处理: 处理保存失败的情况
   */
  void saveDebugContext(DebugContext debugContext);

  /**
   * 获取调试上下文
   * 迁移对应关系: Go语言repo.IDebugContextRepo.GetDebugContext
   * - 功能: 根据PromptID和用户ID获取调试上下文
   * - 参数: PromptID、用户ID
   * - 返回: 调试上下文对象，如果不存在则返回null
   * <p>
   * 处理逻辑:
   * - 参数验证: 检查PromptID和用户ID是否有效
   * - 数据查询: 从数据库查询调试上下文
   * - 结果处理: 返回查询结果或null
   * - 异常处理: 处理查询失败的情况
   */
  DebugContext getDebugContext(Long promptId, String userId);
}
