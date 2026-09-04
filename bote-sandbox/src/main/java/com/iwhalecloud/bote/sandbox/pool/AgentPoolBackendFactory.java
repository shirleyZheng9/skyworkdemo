package com.iwhalecloud.bote.sandbox.pool;

import com.alibaba.opensandbox.sandbox.HttpClientProvider;
import com.alibaba.opensandbox.sandbox.config.ConnectionConfig;
import com.alibaba.opensandbox.sandbox.config.ConnectionConfig.Builder;
import com.alibaba.opensandbox.sandbox.domain.models.sandboxes.SandboxEndpoint;
import com.alibaba.opensandbox.sandbox.domain.services.Commands;
import com.alibaba.opensandbox.sandbox.domain.services.Filesystem;
import com.alibaba.opensandbox.sandbox.domain.services.Health;
import com.alibaba.opensandbox.sandbox.infrastructure.factory.AdapterFactory;
import com.iwhalecloud.bote.sandbox.config.SandboxEngineProperties;
import com.iwhalecloud.bote.sandbox.dto.agentpool.CreateSandboxRequest;
import com.iwhalecloud.bote.sandbox.dto.agentpool.CreateSandboxRequest.ResourceLimit;
import com.iwhalecloud.bote.sandbox.dto.agentpool.CreateSandboxResponse;
import com.iwhalecloud.bote.sandbox.dto.agentpool.SandboxDetailInfo;
import com.iwhalecloud.bote.sandbox.runtime.SandboxRuntime;
import com.iwhalecloud.bote.sandbox.runtime.agentpool.AgentPoolManagementClient;
import com.iwhalecloud.bote.sandbox.runtime.agentpool.AgentPoolSandboxRuntime;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 基于 AgentPool 的沙箱实现
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class AgentPoolBackendFactory implements SandboxBackendFactory {
  private static final Logger logger = LoggerFactory.getLogger(AgentPoolBackendFactory.class);

  private final SandboxEngineProperties properties;
  private final AgentPoolManagementClient managementClient;

  public AgentPoolBackendFactory(SandboxEngineProperties properties) {
    this.properties = properties;
    String baseUrl = properties.getConnection().getBaseUrl() + "/" + StringUtils.strip(properties.getAgentPool().getManagementApiPrefix(), "/");
    this.managementClient = new AgentPoolManagementClient(baseUrl, properties.getConnection().getApiKey());
  }

  @Override
  public SandboxRuntime createSandbox(String image) {
    String img = StringUtils.isNotEmpty(image) ? image : properties.getPool().getDefaultImage();
    CreateSandboxResponse response = managementClient.createSandbox(buildCreateSandboxRequest(img));
    int sandboxId = response.getSandboxId();
    String execdEndpoint = response.getExecdEndpoint();
    logger.debug("AgentPool sandbox created: id={}, execdEndpoint={}", sandboxId, execdEndpoint);
    return createRuntime(String.valueOf(sandboxId), execdEndpoint);
  }

  /**
   * 构造创建沙箱请求
   */
  private CreateSandboxRequest buildCreateSandboxRequest(String image) {
    CreateSandboxRequest request = new CreateSandboxRequest();
    request.setImageUri(image);
    request.setTimeout(properties.getSession().getIdleTimeout().toSeconds());
    SandboxEngineProperties.AgentPool ap = properties.getAgentPool();
    if (ap.getCpuMilli() != null || ap.getMemoryMb() != null) {
      ResourceLimit resourceLimit = new ResourceLimit();
      resourceLimit.setCpuMilli(ap.getCpuMilli());
      resourceLimit.setMemoryMb(ap.getMemoryMb());
      request.setResourceLimit(resourceLimit);
    }
    request.setEntrypoint(List.of("sh", "/start.sh"));
    return request;
  }

  @Override
  @Nullable
  public SandboxRuntime attachSandbox(String sandboxId) {
    if (StringUtils.isEmpty(sandboxId)) {
      return null;
    }
    try {
      SandboxDetailInfo detail = managementClient.getSandboxDetail(sandboxId);
      if (detail == null || !detail.isHealthy()) {
        return null;
      }
      String execdEndpoint = detail.getEndpointUrl();
      if (StringUtils.isEmpty(execdEndpoint)) {
        logger.warn("AgentPool attachSandbox: detail missing execd_endpoint for id={}", sandboxId);
        return null;
      }
      return createRuntime(sandboxId, execdEndpoint);
    }
    catch (Exception e) {
      logger.warn("AgentPool attachSandbox failed: id={}, error={}", sandboxId, e.getMessage());
    }
    return null;
  }

  /**
   * 构造沙箱运行时实例
   */
  private AgentPoolSandboxRuntime createRuntime(String sandboxId, String execdEndpoint) {
    String protocol;
    String baseUrl;
    if (execdEndpoint.startsWith("https://")) {
      protocol = "https";
      baseUrl = Strings.CS.removeStart(execdEndpoint, "https://");
    }
    else {
      protocol = "http";
      //noinspection HttpUrlsUsage
      baseUrl = Strings.CS.removeStart(execdEndpoint, "http://");
    }
    Builder connectionConfigBuilder = ConnectionConfig.builder()
      .protocol(protocol)
      .requestTimeout(properties.getConnection().getRequestTimeout())
      .debug(properties.getConnection().isDebug());
    String apiKey = properties.getConnection().getApiKey();
    if (StringUtils.isNotEmpty(apiKey)) {
      connectionConfigBuilder.apiKey(apiKey);
    }
    ConnectionConfig connectionConfig = connectionConfigBuilder.build();
    SandboxEndpoint endpoint = new SandboxEndpoint(baseUrl, Map.of());
    HttpClientProvider httpClientProvider = new HttpClientProvider(connectionConfig);
    AdapterFactory factory = new AdapterFactory(httpClientProvider);
    Commands commandService = factory.createCommands(endpoint);
    Filesystem filesystemService = factory.createFilesystem(endpoint);
    Health healthService = factory.createHealth(endpoint);
    return new AgentPoolSandboxRuntime(sandboxId, commandService, filesystemService, healthService, managementClient);
  }
}
