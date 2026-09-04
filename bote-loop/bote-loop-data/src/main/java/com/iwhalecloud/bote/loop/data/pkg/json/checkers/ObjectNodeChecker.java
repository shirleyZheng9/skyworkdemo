package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpecChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class ObjectNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (!jsonNode.isObject()) {
      throw new BssException(path + "不是对象类型");
    }
    Map<String, JSONSchemaSpec> properties = jsonSchemaSpec.getProperties();
    if (MapUtils.isEmpty(properties)) {
      return;
    }
    List<String> required = jsonSchemaSpec.getRequired();
    for (Map.Entry<String, JSONSchemaSpec> entry : properties.entrySet()) {
      String checkPath = path + "/" + entry.getKey();
      String key = entry.getKey();
      JSONSchemaSpec propertyJSONSchemaSpec = entry.getValue();
      JsonNode propertyJsonNode = jsonNode.get(key);
      if (propertyJsonNode == null || propertyJsonNode.isNull()) {
        // 非必填，不存在或者为NULL则跳过校验
        if (CollectionUtils.isNotEmpty(required) && required.contains(key)) {
          throw new BssException(checkPath + "必填");
        }
        continue;
      }
      JSONSchemaSpecChecker.check(checkPath, propertyJSONSchemaSpec, propertyJsonNode);
    }
  }
}
