package com.iwhalecloud.bote.loop.domain.component.rpc;

import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;

import java.util.List;

/**
 * 用户信息提供者接口
 * 迁移对应关系: Go语言modules/prompt/domain/component/rpc.IUserProvider
 * - 功能: 提供用户信息查询服务
 * - 主要方法:
 * * mGetUserInfo - 批量获取用户信息
 * <p>
 * Java实现说明:
 * - 对应Go的IUserProvider接口
 * - 使用Java接口定义
 * - 处理批量用户信息查询
 * - 支持异常处理
 * <p>
 * 技术栈迁移:
 * - Go接口 -> Java接口
 * - Go context.Context -> Java方法参数(可扩展)
 * - Go []string -> Java List<String>
 * - Go []*UserInfo -> Java List<UserInfo>
 * - Go error返回 -> Java异常处理
 */
public interface IUserProvider {

  /**
   * 批量获取用户信息
   * 迁移对应关系: Go语言MGetUserInfo(ctx context.Context, userIDs []string) (userInfos []*UserInfo, err error)
   * - 功能: 根据用户ID列表批量获取用户信息
   * - 参数: userIDs - 用户ID列表
   * - 返回: 用户信息列表
   * - 异常: 可能抛出业务异常或系统异常
   * <p>
   * 处理逻辑:
   * - 输入验证: 检查用户ID列表是否为空
   * - 批量查询: 调用用户服务获取用户信息
   * - 结果处理: 返回用户信息列表
   * - 异常处理: 处理查询失败的情况
   */
  List<RpcUserInfo> mGetUserInfo(List<String> userIds);
}
