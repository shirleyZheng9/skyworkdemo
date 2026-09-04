package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

public class StringNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (jsonNode.isTextual()) {
      return;
    }
    throw new BssException(path + "不是字符串类型");
  }
}
