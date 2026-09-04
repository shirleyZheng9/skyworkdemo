package com.iwhalecloud.bote.sandbox.pool;

import com.iwhalecloud.bote.sandbox.runtime.SandboxRuntime;
import org.springframework.lang.Nullable;

/**
 * 沙箱后端工厂
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public interface SandboxBackendFactory {

  /**
   * 创建新沙箱
   *
   * @param image 镜像地址，为空时使用默认镜像
   * @return 沙箱运行时
   */
  SandboxRuntime createSandbox(@Nullable String image);

  /**
   * 连接已有沙箱
   *
   * @param remoteSandboxId 远端沙箱实例 ID
   * @return 连接成功时返回沙箱运行时，失败时返回 null
   */
  @Nullable
  SandboxRuntime attachSandbox(String remoteSandboxId);

}
