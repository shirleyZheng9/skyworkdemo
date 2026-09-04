package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class BooleanNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (jsonNode.isBoolean()) {
      return;
    }
    if (jsonSchemaSpec.isCompatibleTextNode()) {
      String text = jsonNode.asText().toUpperCase();
      List<String> booleanValueList = Lists.newArrayList();
      booleanValueList.add("TRUE");
      booleanValueList.add("FALSE");
      if (booleanValueList.contains(text)) {
        return;
      }
    }
    throw new BssException(path + "不是布尔类型");
  }
}
