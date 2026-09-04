package com.iwhalecloud.bote.sandbox.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/*
 * 沙箱引擎配置属性，对应前缀 bote.sandbox.engine
 */
@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "bote.sandbox.engine")
public class SandboxEngineProperties {
  /**
   * 沙箱后端：opensandbox（默认）使用 OpenSandbox SDK；agentpool 使用 AgentPool HTTP API（管理面 + execd）。
   */
  private String provider = "opensandbox";
  /* OpenSandbox / AgentPool 共用连接配置（domain、api-key、protocol 等） */
  @Valid
  private Connection connection = new Connection();
  /* AgentPool 创建沙箱时的资源与选项 */
  private AgentPool agentPool = new AgentPool();
  /* 全局池化配置（削峰填谷默认值） */
  private Pool pool = new Pool();
  /* 会话级沙箱配置 */
  private Session session = new Session();
  /* 按镜像覆盖池配置，key 为镜像名 */
  private Map<String, Pool> poolByImage = new HashMap<>();

  @Getter
  @Setter
  @ToString
  public static class Connection {
    /* OpenSandbox API 域名 */
    @NotEmpty
    @Pattern(regexp = "(?!https?://).*(?<!/)", message = "domain must not starts with http:// or https:// and must not end with /")
    private String domain;
    /* API Key */
    private String apiKey;
    /* 协议 http/https */
    @NotEmpty
    @Pattern(regexp = "http|https", message = "protocol must be http or https")
    private String protocol = "http";
    /* 请求超时时间 */
    @NotNull
    private Duration requestTimeout = Duration.ofMinutes(30);
    /* 是否开启 HTTP 调试日志 */
    private boolean debug = false;

    /**
     * 获取基础地址
     */
    public String getBaseUrl() {
      return protocol + "://" + domain;
    }
  }

  @Getter
  @Setter
  @ToString
  public static class Pool {
    /* 池内最大沙箱总数（削峰：限制峰值并发） */
    private Integer maxTotal = 20;
    /* 最小空闲数量（填谷：低峰时保持就绪） */
    private Integer minIdle = 0;
    /* 空闲超时时间，超过则回收沙箱 */
    private Duration idleTimeout = Duration.ofMinutes(30);
    /* 获取沙箱等待超时 */
    private Duration acquireTimeout = Duration.ofSeconds(60);
    /* 单沙箱最大存活时间 */
    private Duration maxLiveTime = Duration.ofHours(12);
    /* 默认镜像 */
    private String defaultImage;
    /* 池维护任务执行间隔（毫秒）：谷期保持 ensureMinIdle + 空闲回收 evictIdle + 会话空闲回收，默认 60000 */
    private Long maintainIntervalMs = 60_000L;
  }

  @Getter
  @Setter
  @ToString
  public static class Session {
    /** 沙箱过期时间（过期后自动释放） */
    private Duration idleTimeout = Duration.ofMinutes(60);
  }

  @Getter
  @Setter
  @ToString
  public static class AgentPool {
    /* 管理 API 路径前缀，默认 /api/agent_pool*/
    private String managementApiPrefix = "/api/agent_pool";
    /* 创建沙箱时 CPU 限制（毫核，1000=1 核） */
    private Integer cpuMilli = 2000;
    /* 创建沙箱时内存上限（MB） 0为不限制 */
    private Integer memoryMb = 4096;
  }
}
