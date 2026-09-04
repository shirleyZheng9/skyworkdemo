package com.iwhalecloud.bote.loop.prompt.domain.repo;

import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListDebugHistoryParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListDebugHistoryResult;

/**
 * 调试日志仓库接口
 * 迁移对应关系: Go语言repo.IDebugLogRepo
 * - 功能: 调试日志的数据访问接口
 * - 主要方法:
 * * saveDebugLog - 保存调试日志
 * * listDebugHistory - 列表查询调试历史
 * <p>
 * Java实现说明:
 * - 对应Go的repo.IDebugLogRepo接口
 * - 使用Java接口定义，包含调试日志数据访问方法
 * - 方法签名适配Java类型系统
 * - 使用Spring的依赖注入机制
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go指针类型 -> Java对象引用
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go bool -> Java Boolean
 */
public interface IDebugLogRepo {

  /**
   * 保存调试日志
   * 迁移对应关系: Go语言repo.IDebugLogRepo.SaveDebugLog
   * - 功能: 保存调试日志信息
   * - 参数: 调试日志对象
   * - 返回: 无返回值，异常通过异常机制处理
   * <p>
   * 处理逻辑:
   * - 输入验证: 检查调试日志对象是否有效
   * - 数据持久化: 将调试日志保存到数据库
   * - 异常处理: 处理保存失败的情况
   */
  void saveDebugLog(DebugLog debugLog);

  /**
   * 列表查询调试历史
   * 迁移对应关系: Go语言repo.IDebugLogRepo.ListDebugHistory
   * - 功能: 分页查询调试历史列表
   * - 参数: 查询参数
   * - 返回: 查询结果
   * <p>
   * 处理逻辑:
   * - 参数验证: 检查查询参数是否有效
   * - 数据查询: 从数据库分页查询调试历史
   * - 结果处理: 返回查询结果
   * - 异常处理: 处理查询失败的情况
   */
  ListDebugHistoryResult listDebugHistory(ListDebugHistoryParam param);
}
