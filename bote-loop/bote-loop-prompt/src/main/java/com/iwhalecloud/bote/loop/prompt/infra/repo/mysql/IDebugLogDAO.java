package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugLogEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListParam;
import java.util.List;

/**
 * 调试日志DAO接口
 * 迁移对应关系: Go语言mysql.IDebugLogDAO
 * - 功能: 调试日志的数据访问接口
 * - 主要方法:
 * * list - 列表查询调试日志
 * * save - 保存调试日志
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.IDebugLogDAO接口
 * - 使用Java接口定义，包含调试日志数据访问方法
 * - 方法签名适配Java类型系统
 * - 使用Spring的依赖注入机制
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go切片类型 -> Java List
 * - Go指针类型 -> Java对象引用
 */
public interface IDebugLogDAO {

  /**
   * 列表查询调试日志
   * 迁移对应关系: Go语言mysql.IDebugLogDAO.List
   * - 功能: 根据参数查询调试日志列表
   * - 参数: 查询参数
   * - 返回: 调试日志列表
   */
  List<PromptDebugLogEntity> list(ListParam param);

  /**
   * 保存调试日志
   * 迁移对应关系: Go语言mysql.IDebugLogDAO.Save
   * - 功能: 保存调试日志
   * - 参数: 调试日志对象
   */
  void save(PromptDebugLogEntity debugLog);
}
