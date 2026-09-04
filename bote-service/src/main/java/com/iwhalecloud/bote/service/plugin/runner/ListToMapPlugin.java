package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.ListToMapPluginParams;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 数组转Map插件
 *
 * <p>将对象列表转换为Map形式，支持指定键字段和值字段</p>
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class ListToMapPlugin extends AbstractPlugin<ListToMapPluginParams> {

  public ListToMapPlugin() {
    super(ListToMapPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_LIST_TO_MAP;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newList("list", "对象列表",
        ParameterSpec.newProperty("listItem", "列表元素", AttrDataType.OBJECT)));
    children.add(ParameterSpec.newProperty("leftKey", "Map的键字段名", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("rightKey", "Map的值字段名（可选，为空时整个对象作为值）", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(
        Collections.singletonList(ParameterSpec.newProperty("result", "转换后的Map结果", AttrDataType.OBJECT)));
  }

  @Override
  public void validateParams(ListToMapPluginParams params) {
    Assert.notNull(params.getList(), "list不能为空");
    Assert.hasText(params.getLeftKey(), "leftKey不能为空");
  }

  @Override
  public Object doRun(ListToMapPluginParams pluginParams) {
    try {
      // 防御式编程：参数空值校验，返回空集合而非抛出异常
      if (pluginParams == null || CollectionUtils.isEmpty(pluginParams.getList())
          || StringUtils.isEmpty(pluginParams.getLeftKey())) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", Collections.emptyMap());
        return result;
      }

      // 将对象列表转换为List<Map<String, Object>>格式
      List<Map<String, Object>> datas;
      try {
        String jsonString = JsonUtil.toJsonString(pluginParams.getList());
        datas = JsonUtil.parseJson(jsonString, new TypeReference<>() {
        });
      }
      catch (Exception e) {
        logger.warn("Failed to parse list to Map format, returning empty map", e);
        Map<String, Object> result = new HashMap<>();
        result.put("result", Collections.emptyMap());
        return result;
      }

      if (CollectionUtils.isEmpty(datas)) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", Collections.emptyMap());
        return result;
      }

      // 如果rightKey为空，则整个对象作为值
      if (StringUtils.isEmpty(pluginParams.getRightKey())) {
        Map<Object, Object> mapResult = CollectionUtils.emptyIfNull(datas).stream()
            .collect(Collectors.toMap(
                p -> getObjectValue(p, pluginParams.getLeftKey()),
                p -> p));
        Map<String, Object> result = new HashMap<>();
        result.put("result", mapResult);
        return result;
      }

      // 如果rightKey不为空，则使用rightKey字段的值作为Map的值
      Map<Object, Object> mapResult = CollectionUtils.emptyIfNull(datas).stream()
          .collect(Collectors.toMap(
              p -> getObjectValue(p, pluginParams.getLeftKey()),
              p -> getObjectValue(p, pluginParams.getRightKey())));
      Map<String, Object> result = new HashMap<>();
      result.put("result", mapResult);
      return result;
    }
    catch (Exception e) {
      logger.error("Failed to convert list to map, returning empty map", e);
      Map<String, Object> result = new HashMap<>();
      result.put("result", Collections.emptyMap());
      return result;
    }
  }

  /**
   * 从Map中获取指定key的值
   *
   * @param map Map对象
   * @param key 键名
   * @return 值
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private Object getObjectValue(Map<String, Object> map, String key) {
    if (map == null || StringUtils.isEmpty(key)) {
      return null;
    }
    return map.get(key);
  }
}
