package com.iwhalecloud.bote.sandbox.pool;

import com.alibaba.opensandbox.sandbox.Sandbox;
import com.alibaba.opensandbox.sandbox.config.ConnectionConfig;
import com.alibaba.opensandbox.sandbox.config.ConnectionConfig.Builder;
import com.iwhalecloud.bote.sandbox.config.SandboxEngineProperties;
import com.iwhalecloud.bote.sandbox.runtime.SandboxRuntime;
import com.iwhalecloud.bote.sandbox.runtime.opensandbox.OpenSandboxRuntime;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 基于 OpenSandbox 的沙箱实现
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class OpenSandboxBackendFactory implements SandboxBackendFactory {
  private static final Logger logger = LoggerFactory.getLogger(OpenSandboxBackendFactory.class);

  private final SandboxEngineProperties properties;

  @Override
  public SandboxRuntime createSandbox(@Nullable String image) {
    String img = StringUtils.isNotEmpty(image) ? image : properties.getPool().getDefaultImage();
    Sandbox sandbox = Sandbox.builder()
      .connectionConfig(buildConfig())
      .image(img)
      .timeout(properties.getSession().getIdleTimeout())
      .build();
    logger.debug("OpenSandbox sandbox created: id={}", sandbox.getId());
    return new OpenSandboxRuntime(sandbox);
  }

  @Override
  @Nullable
  public SandboxRuntime attachSandbox(String remoteSandboxId) {
    if (StringUtils.isEmpty(remoteSandboxId)) {
      return null;
    }
    Sandbox sandbox = Sandbox.connector().sandboxId(remoteSandboxId).connectionConfig(buildConfig()).connect();
    if (sandbox.isHealthy()) {
      return new OpenSandboxRuntime(sandbox);
    }
    return null;
  }

  /**
   * 构造连接配置
   */
  private ConnectionConfig buildConfig() {
    SandboxEngineProperties.Connection connection = properties.getConnection(); //NOPMD - suppressed CloseResource - 误报
    Builder builder = ConnectionConfig.builder();
    if (StringUtils.isNotEmpty(connection.getApiKey())) {
      builder.apiKey(connection.getApiKey());
    }
    return builder
      .domain(connection.getDomain())
      .protocol(connection.getProtocol())
      .requestTimeout(connection.getRequestTimeout())
      .debug(connection.isDebug())
      .build();
  }
}
