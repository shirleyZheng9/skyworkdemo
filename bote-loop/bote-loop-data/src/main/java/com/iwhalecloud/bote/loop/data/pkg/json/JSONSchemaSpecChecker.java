package com.iwhalecloud.bote.loop.data.pkg.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.AnyNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.ArrayNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.BooleanNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.DateNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.DateTimeNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.FileNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.IntegerNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.NumberNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.ObjectNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.StringNodeChecker;
import com.iwhalecloud.bote.loop.data.pkg.json.checkers.TextNodeChecker;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;


public abstract class JSONSchemaSpecChecker {

  private static final Map<String, JsonNodeChecker> checkers = new HashMap<>();

  static {
    checkers.put(AttrDataType.OBJECT.getCode(), new ObjectNodeChecker());
    checkers.put(AttrDataType.ARRAY.getCode(), new ArrayNodeChecker());
    checkers.put(AttrDataType.STRING.getCode(), new StringNodeChecker());
    checkers.put(AttrDataType.TEXT.getCode(), new TextNodeChecker());
    checkers.put(AttrDataType.INTEGER.getCode(), new IntegerNodeChecker());
    checkers.put(AttrDataType.NUMBER.getCode(), new NumberNodeChecker());
    checkers.put(AttrDataType.BOOLEAN.getCode(), new BooleanNodeChecker());
    checkers.put(AttrDataType.DATE.getCode(), new DateNodeChecker());
    checkers.put(AttrDataType.DATETIME.getCode(), new DateTimeNodeChecker());
    checkers.put(AttrDataType.FILE.getCode(), new FileNodeChecker());
    checkers.put(AttrDataType.ANY.getCode(), new AnyNodeChecker());
  }

  public static void check(JSONSchemaSpec jsonSchemaSpec, String content) {
    String path = "root";
    String type = jsonSchemaSpec.getType();
    if (AttrDataType.OBJECT.getCode().equals(type) || AttrDataType.ARRAY.getCode().equals(type)) {
      try {
        JsonNode jsonNode = JsonUtil.getObjectMapper().readTree(content);
        check(path, jsonSchemaSpec, jsonNode);
      }
      catch (JsonProcessingException e) {
        throw new BssException("JSON解析失败：" + e.getMessage(), e);
      }
    }
    else {
      JsonNode jsonNode = new TextNode(content);
      jsonSchemaSpec.setCompatibleTextNode(true);
      check(path, jsonSchemaSpec, jsonNode);
    }
  }

  public static void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    String type = jsonSchemaSpec.getType();
    JsonNodeChecker jsonNodeChecker = checkers.get(type);
    jsonNodeChecker.check(path, jsonSchemaSpec, jsonNode);
  }

}
