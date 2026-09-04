package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

public class IntegerNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (jsonNode.isInt()) {
      return;
    }
    if (jsonSchemaSpec.isCompatibleTextNode()) {
      String text = jsonNode.asText();
      try {
        Integer.parseInt(text);
        return;
      }
      catch (NumberFormatException e) {
        throw new BssException(path + "不是整数类型", e);
      }
    }
    throw new BssException(path + "不是整数类型");
  }
}
