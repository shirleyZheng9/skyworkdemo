package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.node.EndNodeData;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphNodeDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

/**
 * 结束节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class EndNodeConverter extends AbstractNodeConverter<EndNodeData> {
  public EndNodeConverter() {
    super(EndNodeData.class);
  }

  @Override
  protected EndNodeData convertNodeData(SceneGraphNodeDTO node) {
    EndNodeData data = new EndNodeData();
    // 对话型工作流的结束节点不应该有参数，但由于前端 bug, 有些历史数据中有 "parameters": [], 会导致 JSON 解析失败（应该是对象而非数组），
    // 需做兼容处理（不是对象时忽略）
    Object parameters = node.getNodeData().get("parameters");
    if (parameters instanceof Map) {
      data.setParameters(JsonUtil.convert(parameters, ParameterSpec.class));
    }
    return data;
  }

  @Override
  protected void simplifyNodeData(EndNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, EndNodeData data) {
    if (context.getResponse() != null && data.getParameters() != null) {
      if (context.getResponseKeyMap() == null) {
        context.setResponseKeyMap(buildKeyMap(context.getResponse()));
      }
      ParameterSpec parameters = supplementParameter(data.getParameters());
      // 将节点出参的 key 同步为与工作流出参的 key 一致, 否则前端会有问题
      unifyParameterKey(context.getResponseKeyMap(), parameters, null, null);
      data.setParameters(parameters);
    }
  }

  /**
   * 构造参数 key 的映射
   */
  private Map<String, String> buildKeyMap(ParameterSpec response) {
    Map<String, String> keyMap = new HashMap<>();
    keyMap.put(response.getName(), response.getKey());
    if (CollectionUtils.isNotEmpty(response.getChildren())) {
      fillKeyMap(response.getName(), response.getChildren(), keyMap);
    }
    return keyMap;
  }

  /**
   * 递归填充参数 key 的映射
   */
  private void fillKeyMap(String parentPath, List<ParameterSpec> children, Map<String, String> keyMap) {
    for (ParameterSpec child : children) {
      String childPath = parentPath + "." + child.getName();
      keyMap.put(childPath, child.getKey());
      if (CollectionUtils.isNotEmpty(child.getChildren())) {
        fillKeyMap(childPath, child.getChildren(), keyMap);
      }
    }
  }

  /**
   * 递归统一参数的 key
   */
  private void unifyParameterKey(Map<String, String> keyMap, ParameterSpec spec, @Nullable String parentPath, @Nullable String parentKey) {
    String path = parentPath == null ? spec.getName() : parentPath + "." + spec.getName();
    String key = keyMap.getOrDefault(path, spec.getKey());
    spec.setParentKey(parentKey);
    spec.setKey(key);
    if (CollectionUtils.isNotEmpty(spec.getChildren())) {
      for (ParameterSpec child : spec.getChildren()) {
        unifyParameterKey(keyMap, child, path, key);
      }
    }
  }
}
