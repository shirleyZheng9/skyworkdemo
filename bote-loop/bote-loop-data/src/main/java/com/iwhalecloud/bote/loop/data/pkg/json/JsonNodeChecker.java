package com.iwhalecloud.bote.loop.data.pkg.json;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonNodeChecker {

  void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode);

}
