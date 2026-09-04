package com.iwhalecloud.bote.sandbox.runtime.agentpool;

import com.alibaba.opensandbox.sandbox.domain.services.Commands;
import com.alibaba.opensandbox.sandbox.domain.services.Filesystem;
import com.alibaba.opensandbox.sandbox.domain.services.Health;
import com.iwhalecloud.bote.sandbox.dto.agentpool.SandboxDetailInfo;
import com.iwhalecloud.bote.sandbox.runtime.AbstractOpenSandboxRuntime;
import java.time.Duration;
import java.time.Instant;
import org.apache.commons.lang3.Strings;

/**
 * 基于 AgentPool 的沙箱运行时
 *
 * <p>文件操作、命令执行使用了 OpenSandbox 的 execd 组件，因此可以跟 OpenSandbox 共用部分代码</p>
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public class AgentPoolSandboxRuntime extends AbstractOpenSandboxRuntime {
  private final Health healthService;
  private final AgentPoolManagementClient managementClient;

  public AgentPoolSandboxRuntime(String sandboxId, Commands commandService, Filesystem filesystemService, Health healthService, AgentPoolManagementClient managementClient) {
    super(sandboxId, commandService, filesystemService);
    this.healthService = healthService;
    this.managementClient = managementClient;
  }

  @Override
  public boolean isHealthy() {
    try {
      return healthService.ping(sandboxId);
    }
    catch (Exception e) {
      logger.warn("AgentPool ping sandbox failed: sandboxId={}", sandboxId, e);
      return false;
    }
  }

  @Override
  public void renew(Duration duration) {
    managementClient.renewSandbox(sandboxId, Instant.now().plus(duration));
  }

  @Override
  public void destroy() {
    try {
      SandboxDetailInfo detail = managementClient.getSandboxDetail(sandboxId);
      if (detail == null) {
        return;
      }
      managementClient.removeSandbox(sandboxId);
    }
    catch (Exception e) {
      if (Strings.CI.contains(e.getMessage(), "not found")) {
        logger.warn("Destroy sandbox failed, sandbox not found: sandboxId={}", sandboxId);
      }
      else {
        logger.error("Destroy sandbox failed: sandboxId={}", sandboxId, e);
      }
    }
  }
}
