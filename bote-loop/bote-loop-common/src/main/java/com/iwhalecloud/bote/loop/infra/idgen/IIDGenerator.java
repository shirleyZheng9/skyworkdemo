package com.iwhalecloud.bote.loop.infra.idgen;

import java.util.List;

/**
 * ID生成器接口
 * 迁移对应关系: Go语言idgen.IIDGenerator
 * - 功能: 生成唯一ID的核心接口
 * - 主要方法:
 * * genID - 生成单个ID
 * * genMultiIDs - 批量生成ID
 * <p>
 * Java实现说明:
 * - 对应Go的idgen.IIDGenerator接口
 * - 使用Java接口定义，包含ID生成方法
 * - 方法签名适配Java类型系统
 * - 支持传统Spring MVC模式
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go int64 -> Java Long
 * - Go切片类型 -> Java List
 */
public interface IIDGenerator {

  /**
   * 生成单个ID
   * 迁移对应关系: Go语言idgen.IIDGenerator.GenID
   * - 功能: 生成一个唯一ID
   * - 返回: 生成的ID
   * - 异常: 生成失败时抛出异常
   */
  Long genId();

  List<Long> genMultiIds(int count);
}
