package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.math.BigDecimal;

public class NumberNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    if (jsonNode.isDouble()) {
      return;
    }
    if (jsonSchemaSpec.isCompatibleTextNode()) {
      String text = jsonNode.asText();
      try {
        new BigDecimal(text);
      }
      catch (NumberFormatException e) {
        throw new BssException(path + "不是浮点数类型", e);
      }
      return;
    }
    throw new BssException(path + "不是浮点数类型");
  }
}
