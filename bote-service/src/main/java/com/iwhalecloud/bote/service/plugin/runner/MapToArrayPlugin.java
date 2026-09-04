package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.MapToArrayPluginParams;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Map转数组插件
 *
 * <p>将 Map 转换为数组，每个数组元素包含 leftKey 和 rightKey 两个属性</p>
 *
 * @author lizuyin
 * @since 2025-11-17
 */
@Component
public class MapToArrayPlugin extends AbstractPlugin<MapToArrayPluginParams> {

  public MapToArrayPlugin() {
    super(MapToArrayPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_MAP_TO_ARRAY;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("map", "Map对象", AttrDataType.OBJECT));
    children.add(ParameterSpec.newProperty("leftKey", "数组对象中的键属性名", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("rightKey", "数组对象中的值属性名", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("success", "是否成功", AttrDataType.BOOLEAN));
    children.add(ParameterSpec.newProperty("message", "执行信息", AttrDataType.STRING));
    children.add(ParameterSpec.newList("result", "转换后的数组结果",
        ParameterSpec.newProperty("arrayItem", "数组元素", AttrDataType.OBJECT)));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(MapToArrayPluginParams params) {
    Assert.notNull(params.getMap(), "map不能为空");
    Assert.notNull(params.getLeftKey(), "leftKey不能为空");
    Assert.notNull(params.getRightKey(), "rightKey不能为空");
  }

  @Override
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
  public Object doRun(MapToArrayPluginParams pluginParams) {
    try {
      if (pluginParams == null || pluginParams.getMap() == null
          || StringUtils.isBlank(pluginParams.getLeftKey())
          || StringUtils.isBlank(pluginParams.getRightKey())) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "参数不能为空");
        result.put("result", Collections.emptyList());
        return result;
      }

      Map<String, Object> mapData;
      try {
        String jsonString = JsonUtil.toJsonString(pluginParams.getMap());
        mapData = JsonUtil.parseJson(jsonString, new TypeReference<>() {
        });
      }
      catch (Exception e) {
        logger.warn("Failed to parse map to Map format, returning empty array", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Map对象格式错误");
        result.put("result", Collections.emptyList());
        return result;
      }

      if (mapData.isEmpty()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "转换成功");
        result.put("result", Collections.emptyList());
        return result;
      }

      // Map 转数组逻辑
      List<Map<String, Object>> arrayResult = new ArrayList<>();
      for (Map.Entry<String, Object> entry : mapData.entrySet()) {
        Map<String, Object> item = new HashMap<>();
        item.put(pluginParams.getLeftKey(), entry.getKey());
        item.put(pluginParams.getRightKey(), entry.getValue());
        arrayResult.add(item);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "转换成功");
      result.put("result", arrayResult);
      return result;
    }
    catch (Exception e) {
      logger.error("Failed to convert map to array, returning empty array", e);
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("message", "转换失败：" + e.getMessage());
      result.put("result", Collections.emptyList());
      return result;
    }
  }
}

