package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;

public class FileNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    // 暂不支持文件属性校验
  }
}
