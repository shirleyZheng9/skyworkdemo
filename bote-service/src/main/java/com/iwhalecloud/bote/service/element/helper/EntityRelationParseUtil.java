package com.iwhalecloud.bote.service.element.helper;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.mapper.base.EnvVariableManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.Nullable;

/**
 * 实体关联关系解析工具类
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
public final class EntityRelationParseUtil {

  private static final EnvVariableManageMapper mapper = SpringUtil.getBean(EnvVariableManageMapper.class);
  private static final TenantSettingInfoCache tenantSettingInfoCache = SpringUtil.getBean(TenantSettingInfoCache.class);

  private EntityRelationParseUtil() {
  }

  /**
   * 解析参数
   */
  public static void parseParameter(List<ResourceElementDTO> elements, Long tenantId, SceneGraphDTO graph) {
    if (graph == null) {
      return;
    }
    if (CollectionUtils.isEmpty(graph.getNodes())) {
      return;
    }
    Set<String> envCodes = new HashSet<>();
    // 遍历节点, 解析节点参数
    for (SceneGraphNodeDTO node : graph.getNodes()) {
      if (node.getNodeData() == null) {
        continue;
      }
      Object parameters = node.getNodeData().get("parameters");
      if (parameters instanceof Map) {
        // 对于数据库节点（insertRecord、updateRecords等），parameters 是数组，不是 ParameterSpec
        ParameterSpec parameter = JsonUtil.convert(parameters, ParameterSpec.class);
        extractEnvCodes(parameter, envCodes);
      }
    }
    buildEnvVariableElements(elements, envCodes, tenantId);
  }

  /**
   * 构建环境变量元素
   */
  private static void buildEnvVariableElements(List<ResourceElementDTO> elements, Set<String> envCodes, Long tenantId) {
    if (CollectionUtils.isEmpty(envCodes)) {
      return;
    }
    List<String> codes = envCodes.stream().toList();
    List<Long> envVariableIds = mapper.selectEnvVariableIdsByCodes(tenantId, codes);
    for (Long envVariableId : CollectionUtils.emptyIfNull(envVariableIds)) {
      ResourceElementDTO element = new ResourceElementDTO();
      element.setElementId(envVariableId);
      element.setElementType(DataSyncCodeEnum.ENV_VAR.getCode());
      elements.add(element);
    }
  }

  /**
   * 提取环境变量code
   */
  private static void extractEnvCodes(ParameterSpec parameter, Set<String> envCodes) {
    if (parameter == null) {
      return;
    }
    String value = parameter.getValue();
    String defaultValue = parameter.getDefaultValue();
    // 获取环境变量code
    if (StringUtils.isNotEmpty(value)) {
      envCodes.add(parseEnvCode(value));
    }
    // 获取环境变量code
    if (StringUtils.isNotEmpty(defaultValue)) {
      envCodes.add(parseEnvCode(defaultValue));
    }
    if (parameter.hasChildren()) {
      parameter.getChildren().forEach(child -> extractEnvCodes(child, envCodes));
    }
  }

  /**
   * 获取环境变量的code
   */
  private static String parseEnvCode(String envVar) {
    if (StringUtils.isEmpty(envVar)) {
      return null;
    }
    if (envVar.startsWith("$.envVar.")) {
      return StringUtils.substringAfterLast(envVar, ".");
    }
    return null;
  }

  /**
   * 处理模型元素（步骤上为字面量或 {@code $} 表达式字符串时：仅字面量数字建立依赖，引用忽略）
   */
  public static void handleModelElement(List<ResourceElementDTO> elements, @Nullable String modelIdOrExpr, Long tenantId) {
    if (StringUtils.isEmpty(modelIdOrExpr)) {
      return;
    }
    if (NumberUtils.isCreatable(modelIdOrExpr)) {
      handleModelElement(elements, Long.parseLong(modelIdOrExpr), tenantId);
    }
  }

  /**
   * 处理模型元素
   */
  public static void handleModelElement(List<ResourceElementDTO> elements, Long modelId, Long tenantId) {
    if (modelId == null) {
      return;
    }
    // 非默认模型
    if (!Objects.equals(modelId, ModelConsts.DEFAULT_MODEL)) {
      addModelElement(elements, modelId);
      return;
    }
    // 默认模型
    Long defaultModelId = tenantSettingInfoCache.getModelIdOrNull(tenantId);
    if (defaultModelId == null) {
      return;
    }
    addModelElement(elements, defaultModelId);
  }

  /**
   * 添加模型元素到列表
   */
  private static void addModelElement(List<ResourceElementDTO> elements, Long modelId) {
    ResourceElementDTO element = new ResourceElementDTO();
    element.setElementId(modelId);
    element.setElementType(DataSyncCodeEnum.MODEL.getCode());
    elements.add(element);
  }
}
