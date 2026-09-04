package com.iwhalecloud.bote.beyond;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.BeyondConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.SwaggerApiUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.beyond.BeyondMqMessageDTO;
import com.iwhalecloud.bote.dto.beyond.BeyondMqMessageDTO.PluginMachineInfo;
import com.iwhalecloud.bote.dto.beyond.BeyondMqMessageDTO.ResourceDTO;
import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.dto.mq.MqConfig;
import com.iwhalecloud.bote.dto.skill.ServiceGatewayDTO;
import com.iwhalecloud.bote.dto.skill.ServicePlatformDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.mq.client.LitchiMqConsumer;
import com.iwhalecloud.bote.service.mcp.IMcpServerManageService;
import com.iwhalecloud.bote.service.skill.ISkillServiceManageService;
import com.iwhalecloud.bss.litchi.mq.MQClientFactory;
import com.iwhalecloud.bss.litchi.mq.config.properties.LitchiMQProperties;
import com.iwhalecloud.bss.litchi.mq.consts.MQProcessStatus;
import com.iwhalecloud.bss.litchi.mq.consumer.MQConsumer;
import com.iwhalecloud.bss.litchi.mq.dto.ConsumerMessage;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 百应 MQ 消费者管理器
 *
 * @author chen.linfa
 * @since 2025-12-04
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
public class BeyondMqConsumerManager implements ApplicationListener<ApplicationReadyEvent>, DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(BeyondMqConsumerManager.class);

  private final Environment environment;

  private final IMcpServerManageService mcpServerManageService;

  private final ISkillServiceManageService serviceManageService;

  /** MQ 客户端实例映射。key 为 mqInstId, value 为客户端实例 */
  private final Map<Long, LitchiMqConsumer> clientMap = new ConcurrentHashMap<>();

  public BeyondMqConsumerManager(Environment environment, IMcpServerManageService mcpServerManageService,
    ISkillServiceManageService serviceManageService) {
    this.environment = environment;
    this.mcpServerManageService = mcpServerManageService;
    this.serviceManageService = serviceManageService;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // 应用启动后加载消费者
    String server = environment.getProperty("beyond.kafka.server");
    if (StringUtils.isEmpty(server)) {
      return;
    }
    registerConsumer(server);
  }

  /**
   * 关闭 MQ 客户端
   */
  @Override
  public void destroy() {
    removeClient();
  }

  /**
   * 加载消费者
   */
  private void registerConsumer(String server) {
    try {
      Long key = 1L;
      LitchiMqConsumer client = clientMap.get(key);
      // 已有客户端实例时，检查客户端状态是否正常，必要时删除客户端实例以触发重新创建
      if (isClientHealthy(client)) {
        logger.debug("The beyond mq consumer client has not been changed and continues to use");
        return;
      }

      // 关闭client链接，重新注册消费者
      if (client != null) {
        client.close();
        clientMap.remove(key);
      }

      // 重新获取客户端
      client = clientMap.computeIfAbsent(key, k -> createConsumerClient(server));
      // 订阅主题
      String topic = environment.getProperty("beyond.kafka.topic", "resource-events");
      client.subscribe(topic, this::handleMessage);
    }
    catch (RuntimeException e) {
      logger.error("Failed to register beyond mq consumers", e);
    }
  }

  /**
   * 创建消费者客户端实例
   */
  @SuppressWarnings("java:S2139")
  private LitchiMqConsumer createConsumerClient(String server) {
    // 构造消费者配置
    MqConfig mqConfig = new MqConfig();
    mqConfig.setServer(server);
    mqConfig.setConsumerGroup(environment.getProperty("beyond.kafka.consumer.group", "boteConsumer"));
    LitchiMQProperties properties = mqConfig.buildProperties(BaseConsts.MQ_TYPE_KAFKA, false);
    // 创建消费者
    MQConsumer consumer = null;
    try {
      consumer = MQClientFactory.createConsumer(properties);
      consumer.afterPropertiesSet();
      return new LitchiMqConsumer(1L, mqConfig, consumer);
    }
    catch (Exception e) {
      // 启动失败时销毁实例，避免残留线程
      if (consumer != null) {
        consumer.close();
      }
      logger.error("Failed to create MQ consumer: properties={}", properties, e);
      String msg = StringUtils.isNotEmpty(e.getMessage()) ? e.getMessage() : ExceptionUtils.getRootCauseMessage(e);
      throw new IllegalStateException("创建 MQ 消费者实例出现异常：" + msg, e);
    }
  }

  /**
   * 清理未用到的客户端
   */
  private void removeClient() {
    if (MapUtils.isNotEmpty(clientMap)) {
      clientMap.get(1L).close();
      clientMap.remove(1L);
    }
  }

  /**
   * 检查client是否健康
   *
   * @param client client客户端
   * @return client 是否检查
   */
  private boolean isClientHealthy(@Nullable LitchiMqConsumer client) {
    if (client == null) {
      logger.warn("The beyond mq consumer client is null");
      return false;
    }
    if (!client.isHealthy()) {
      logger.warn("The beyond mq consumer client is not healthy");
      return false;
    }
    return true;
  }

  /**
   * 处理 MQ 消息
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private MQProcessStatus handleMessage(ConsumerMessage message) {
    String content = new String(message.getBody(), StandardCharsets.UTF_8);
    BeyondMqMessageDTO dto = JsonUtil.parseJson(content, BeyondMqMessageDTO.class);
    Assert.notNull(dto, "Failed to parse body");
    ResourceDTO resource = dto.getPayload().getResource();
    if (BeyondConsts.RESOURCE_TYPE_MCP.equals(resource.getResourceBizType())) {
      McpServerDTO server = McpServerDTO.from(resource);
      mcpServerManageService.syncMcpServerFromBeyond(server);
    }
    else if (BeyondConsts.RESOURCE_TYPE_TOOL.equals(resource.getResourceBizType())) {
      List<SkillServiceDTO> services = convert(resource);
      Long catalogId = null;
      if (Objects.equals(resource.getResourceStatus(), BeyondConsts.STATUS_INVALID)) {
        catalogId = resource.getResourceId();
      }
      serviceManageService.syncServiceFromBeyond(services, catalogId);
    }
    return MQProcessStatus.DONE;
  }

  /**
   * 转换 API 对象
   */
  private List<SkillServiceDTO> convert(ResourceDTO resource) {
    List<SkillServiceDTO> list = new ArrayList<>();

    for (PluginMachineInfo info : CollectionUtils.emptyIfNull(resource.getPluginMachineInfo())) {
      SkillServiceDTO dto = new SkillServiceDTO();
      dto.setServiceId(info.getPluginMachine().getPluginMachineId());
      dto.setServiceCode(info.getPluginMachine().getMachineCode());
      dto.setServiceName(info.getPluginMachine().getMachineName());
      dto.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dto.setRemark(BaseConsts.SYSTEM_TYPE_BEYOND);
      dto.setIsMock(BaseConsts.FALSE);
      dto.setIsSse(BaseConsts.FALSE);
      // 工具集 ID 作为目录 ID
      dto.setCatalogItemId(resource.getResourceId());

      // 提取服务出入参定义
      extractApiInfo(info.getPluginMachineOpenAPI(), dto);
      // 补充鉴权类的 header 参数
      if (MapUtils.isNotEmpty(resource.getHeaders())) {
        List<ParameterSpec> specs = new ArrayList<>();
        for (Entry<String, String> entry : resource.getHeaders().entrySet()) {
          ParameterSpec spec = new ParameterSpec();
          spec.setDefaultValue(entry.getKey());
          spec.setName(entry.getKey());
          spec.setType(AttrDataType.STRING);
          spec.setRequired(true);
          spec.setValue(entry.getValue());
          spec.setKey(IDUtils.nextId22());
          spec.setParentKey("-1");
          specs.add(spec);
        }
        String headerJson = dto.getHeaderJson();
        if (StringUtils.isEmpty(headerJson)) {
          dto.setHeaderJson(JsonUtil.toJsonString(ParameterSpec.newRoot(specs)));
        }
        else {
          ParameterSpec root = JsonUtil.parseJsonRequired(headerJson, ParameterSpec.class);
          List<ParameterSpec> children = root.getChildren();
          if (CollectionUtils.isEmpty(children)) {
            children = new ArrayList<>();
          }
          children.addAll(specs);
          root.setChildren(children);
          dto.setHeaderJson(JsonUtil.toJsonString(root));
        }
      }

      // 提取网关信息
      String serviceUrl = info.getPluginMachineOpenAPI().getServers().getFirst().getUrl();
      ServiceGatewayDTO gateway = new ServiceGatewayDTO();
      gateway.setEnvCode(BaseConsts.ENV_CODE_DEV);
      gateway.setUrl(serviceUrl);
      gateway.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
      gateway.setStatusCd(BaseConsts.STATUS_CD_VALID);

      ServicePlatformDTO platform = new ServicePlatformDTO();
      platform.setPlatformCode(serviceUrl);
      platform.setPlatformName(serviceUrl);
      platform.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
      platform.setStatusCd(BaseConsts.STATUS_CD_VALID);
      platform.setGateways(Collections.singletonList(gateway));
      dto.setPlatform(platform);

      list.add(dto);
    }
    return list;
  }

  /**
   * 从 OpenAPI 中提取 API 信息
   *
   * @param openAPI OpenAPI 对象
   * @param dto 技能服务 DTO
   */
  private void extractApiInfo(OpenAPI openAPI, SkillServiceDTO dto) {
    if (MapUtils.isEmpty(openAPI.getPaths())) {
      return;
    }
    // 获取 schemas 定义
    @SuppressWarnings("rawtypes")
    Map<String, Schema> schemas =
      openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null ? openAPI.getComponents().getSchemas() : Collections.emptyMap();
    // 遍历 paths，找到第一个路径和对应的 HTTP 方法
    for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
      String relativePath = pathEntry.getKey();
      PathItem pathItem = pathEntry.getValue();
      // 尝试获取各种 HTTP 方法对应的 Operation
      Operation operation = null;
      String reqMethod = null;
      if (pathItem.getGet() != null) {
        operation = pathItem.getGet();
        reqMethod = "GET";
      }
      else if (pathItem.getPost() != null) {
        operation = pathItem.getPost();
        reqMethod = "POST";
      }
      if (operation != null && StringUtils.isNotBlank(reqMethod)) {
        // 设置相对路径和请求方法
        dto.setRelativePath(relativePath);
        dto.setReqMethod(reqMethod.toUpperCase());
        // 提取 header 参数
        dto.setHeaderJson(SwaggerApiUtil.getParameterJson(operation, schemas, "header"));
        // 提取 path 参数
        dto.setPathJson(SwaggerApiUtil.getParameterJson(operation, schemas, "path"));
        // 提取 query 参数
        dto.setQueryJson(SwaggerApiUtil.getParameterJson(operation, schemas, "query"));
        // 提取 body 参数
        dto.setBodyJson(SwaggerApiUtil.getBodyJson(operation, schemas));
        // 提取响应参数
        dto.setResponseJson(SwaggerApiUtil.getResponseJson(operation, schemas));
        // 只处理第一个路径
        break;
      }
    }
  }
}
