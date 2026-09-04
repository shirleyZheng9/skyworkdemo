package com.iwhalecloud.bote.llm.client.ext;

/**
 * 模型请求头解析器
 *
 * @author bianjp
 * @since 2025-10-23
 */
public interface ModelRequestHeaderResolver {

  /**
   * 解析请求头的值
   *
   * <p>用于支持请求头的值引用变量（系统变量、登录信息），变量格式为 ${变量名}，一个值中可以引用多个变量。</p>
   *
   * <p>变量值需要在运行时实时解析，不能缓存解析结果。</p>
   *
   * @param value 值，支持引用变量
   * @return 解析后的值
   */
  String resolve(String value);

}
