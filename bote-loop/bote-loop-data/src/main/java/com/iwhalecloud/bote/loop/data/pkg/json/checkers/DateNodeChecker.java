package com.iwhalecloud.bote.loop.data.pkg.json.checkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.loop.data.pkg.json.JSONSchemaSpec;
import com.iwhalecloud.bote.loop.data.pkg.json.JsonNodeChecker;
import com.cmsr.tiangong.base.exception.BssException;
import com.cmsr.tiangong.util.DateUtil;

public class DateNodeChecker implements JsonNodeChecker {
  @Override
  public void check(String path, JSONSchemaSpec jsonSchemaSpec, JsonNode jsonNode) {
    String text = jsonNode.asText();
    try {
      DateUtil.parseDate(text);
    }
    catch (Exception e) {
      throw new BssException(path + "不是日期类型", e);
    }
  }
}
