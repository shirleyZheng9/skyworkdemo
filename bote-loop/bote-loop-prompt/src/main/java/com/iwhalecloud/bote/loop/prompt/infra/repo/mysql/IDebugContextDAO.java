package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugContextEntity;

/**
 * 调试上下文DAO接口
 * 迁移对应关系: Go语言mysql.IDebugContextDAO
 * - 功能: 调试上下文的数据访问接口
 * - 主要方法:
 * * save - 保存调试上下文
 * * get - 获取调试上下文
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.IDebugContextDAO接口
 * - 使用Java接口定义，包含调试上下文数据访问方法
 * - 方法签名适配Java类型系统
 * - 使用Spring的依赖注入机制
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
public interface IDebugContextDAO {

  /**
   * 保存调试上下文
   * 迁移对应关系: Go语言mysql.IDebugContextDAO.Save
   * - 功能: 保存调试上下文
   * - 参数: 调试上下文对象
   */
  void save(PromptDebugContextEntity debugContext);

  /**
   * 获取调试上下文
   * 迁移对应关系: Go语言mysql.IDebugContextDAO.Get
   * - 功能: 根据PromptID和UserID获取调试上下文
   * - 参数: PromptID、UserID
   * - 返回: 调试上下文对象
   */
  PromptDebugContextEntity get(Long promptId, String userId);
}
