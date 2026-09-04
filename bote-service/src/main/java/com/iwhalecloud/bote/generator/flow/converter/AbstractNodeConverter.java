package com.iwhalecloud.bote.generator.flow.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.SimplifiedNodeDTO;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.mapper.generator.FlowAiQueryMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 转换器抽象类
 *
 * @author bianjp
 * @since 2025-03-31
 */
public abstract class AbstractNodeConverter<T> {
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected static final FlowAiQueryMapper flowAiQueryMapper = SpringUtil.getBean(FlowAiQueryMapper.class);

  /** 节点数据的类型 */
  protected final Class<T> nodeDataType;

  protected AbstractNodeConverter(Class<T> nodeDataType) {
    this.nodeDataType = nodeDataType;
  }

  /**
   * 转换节点，用于将流程图节点转为大模型理解的节点
   */
  public final SimplifiedNodeDTO convertNode(SceneGraphNodeDTO node) {
    SimplifiedNodeDTO dto = new SimplifiedNodeDTO();
    dto.setName(node.getNodeName());
    dto.setCode(node.getNodeCode());
    dto.setType(node.getNodeType());
    Object data = convertNodeData(node);
    if (data != null) {
      Map<String, Object> map = JsonUtil.convert(data, new TypeReference<Map<String, Object>>() {
      });
      if (!map.isEmpty()) {
        dto.setData(map);
      }
    }
    return dto;
  }

  /**
   * 还原节点，用于将大模型生成的节点转为流程图节点
   *
   * <p>处理逻辑和转换相反</p>
   */
  public final SceneGraphNodeDTO revertNode(FlowConverterContext context, SimplifiedNodeDTO node) {
    // 节点数据
    Map<String, Object> nodeData = new LinkedHashMap<>();
    nodeData.put("nodeName", node.getName());
    nodeData.put("nodeCode", node.getCode());
    nodeData.put("nodeType", node.getType());

    if (nodeDataType != Void.class) {
      try {
        T data = JsonUtil.convert(node.getData(), nodeDataType);
        Map<String, Object> extraData = revertNodeData(context, data);
        if (!extraData.isEmpty()) {
          nodeData.putAll(extraData);
        }
      }
      catch (RuntimeException e) {
        // 大模型不稳定，生成的节点数据可能不正确，忽略异常的异常，确保流程图大体上还能渲染出来
        logger.warn("Failed to revert node data: node={}", node, e);
      }
    }

    SceneGraphNodeDTO dto = new SceneGraphNodeDTO();
    dto.setNodeName(node.getName());
    dto.setNodeCode(node.getCode());
    dto.setNodeType(node.getType());
    dto.setNodeData(nodeData);
    return dto;
  }

  /**
   * 转换节点数据
   */
  @Nullable
  protected T convertNodeData(SceneGraphNodeDTO node) {
    if (nodeDataType == Void.class) {
      return null;
    }
    T data = JsonUtil.convert(node.getNodeData(), nodeDataType);
    simplifyNodeData(data);
    return data;
  }

  /**
   * 还原节点数据
   *
   * <p>处理逻辑和转换相反</p>
   */
  protected Map<String, Object> revertNodeData(FlowConverterContext context, T data) {
    supplementNodeData(context, data);
    return JsonUtil.convert(data, new TypeReference<LinkedHashMap<String, Object>>() { //NOPMD - suppressed LooseCoupling - 确保生成的 Map 可修改
    });
  }

  /**
   * 简化节点数据
   */
  protected void simplifyNodeData(T data) {
    // 可选，子类有必要时覆盖
  }

  /**
   * 补充节点数据
   *
   * <p>处理逻辑和简化相反</p>
   */
  protected void supplementNodeData(FlowConverterContext context, T data) {
    // 可选，子类有必要时覆盖
  }

  /**
   * 补充参数，避免前端报错
   *
   * <p>处理逻辑和简化相反</p>
   */
  public static ParameterSpec supplementParameter(@Nullable ParameterSpec root) {
    if (root == null) {
      root = ParameterSpec.newRoot();
    }
    else if (StringUtils.isEmpty(root.getName())) {
      root.setName(BaseConsts.PARAMETER_NODE_ROOT);
      root.setDescription(BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION);
    }
    else if (BaseConsts.PARAMETER_NODE_ROOT.equals(root.getName())) {
      root.setDescription(BaseConsts.PARAMETER_NODE_ROOT_DESCRIPTION);
    }
    root.setKey("-1");
    if (CollectionUtils.isNotEmpty(root.getChildren())) {
      supplementParameterKey(root.getKey(), root.getChildren());
    }
    return root;
  }

  /**
   * 递归填充参数的 key, 避免前端出现赋值错乱
   */
  private static void supplementParameterKey(String parentKey, List<ParameterSpec> children) {
    for (ParameterSpec child : children) {
      String key = UUID.randomUUID().toString();
      child.setParentKey(parentKey);
      child.setKey(key);
      if (CollectionUtils.isNotEmpty(child.getChildren())) {
        supplementParameterKey(key, child.getChildren());
      }
    }
  }

  /**
   * 简化参数，看起来更简洁，也能降低发给大模型时的 token 消耗
   */
  @Nullable
  public static ParameterSpec simplifyParameter(@Nullable ParameterSpec spec) {
    if (spec == null || ("root".equals(spec.getName()) && spec.isObject() && !spec.hasChildren())) {
      return null;
    }
    if ("root".equals(spec.getName())) {
      spec.setDescription(null);
    }
    doSimplifyParameter(spec);
    return spec;
  }

  /**
   * 简化参数
   */
  @Nullable
  public static List<ParameterSpec> simplifyParameters(@Nullable List<ParameterSpec> parameters) {
    if (CollectionUtils.isEmpty(parameters)) {
      return null;
    }
    for (ParameterSpec spec : parameters) {
      doSimplifyParameter(spec);
    }
    return parameters;
  }

  /**
   * 简化参数，递归处理
   */
  private static void doSimplifyParameter(ParameterSpec spec) {
    if (Boolean.FALSE.equals(spec.getRequired())) {
      spec.setRequired(null);
    }
    spec.setKey(null);
    spec.setParentKey(null);
    spec.setAttrCode(null);
    spec.setDefaultValue(null);
    if (Objects.equals(spec.getName(), spec.getDescription())) {
      spec.setDescription(null);
    }
    if (StringUtils.isEmpty(spec.getValue())) {
      spec.setValue(null);
    }
    if (CollectionUtils.isEmpty(spec.getChildren())) {
      spec.setChildren(null);
    }
    else {
      for (ParameterSpec child : spec.getChildren()) {
        doSimplifyParameter(child);
      }
    }
  }
}
