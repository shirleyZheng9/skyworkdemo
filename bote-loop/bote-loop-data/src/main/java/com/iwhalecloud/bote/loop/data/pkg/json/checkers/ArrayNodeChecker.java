package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpecChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

public class ArrayNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (!jsonNode.isArray()) {
      throw new BssException(path + "不是数组类型");
    }
    JSONSchemaSpec items = jsonSchemaSpec.getItems();
    int size = jsonNode.size();
    for (int i = 0; i < size; i++) {
      JsonNode itemJsonNode = jsonNode.get(i);
      JSONSchemaSpecChecker.check(path + "[" + i + "]", items, itemJsonNode);
    }
  }
}
