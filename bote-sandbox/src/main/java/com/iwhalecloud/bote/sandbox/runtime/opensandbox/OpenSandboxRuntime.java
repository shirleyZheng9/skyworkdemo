package com.iwhalecloud.bote.sandbox.runtime.opensandbox;

import com.alibaba.opensandbox.sandbox.Sandbox;
import com.iwhalecloud.bote.sandbox.runtime.AbstractOpenSandboxRuntime;
import java.time.Duration;

/**
 * 基于 OpenSandbox 的沙箱运行时
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public class OpenSandboxRuntime extends AbstractOpenSandboxRuntime {
  private final Sandbox sandbox;

  public OpenSandboxRuntime(Sandbox sandbox) {
    super(sandbox.getId(), sandbox.commands(), sandbox.files());
    this.sandbox = sandbox;
  }

  @Override
  public boolean isHealthy() {
    try {
      return sandbox.isHealthy();
    }
    catch (Exception e) {
      logger.warn("OpenSandbox ping sandbox failed: sandboxId={}", sandboxId, e);
      return false;
    }
  }

  @Override
  public void renew(Duration duration) {
    try {
      sandbox.renew(duration);
    }
    catch (Exception e) {
      logger.error("Failed to renew sandbox: sandboxId={}", sandboxId, e);
    }
  }

  @Override
  public void destroy() {
    try {
      sandbox.kill();
    }
    catch (Exception e) {
      logger.error("Failed to remove sandbox: sandboxId={}", sandboxId, e);
    }
  }
}
