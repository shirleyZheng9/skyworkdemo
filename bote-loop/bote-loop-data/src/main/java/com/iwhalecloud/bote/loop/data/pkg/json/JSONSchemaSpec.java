package com.iwhalecloud.bote.loop.data.pkg.json;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JSONSchemaSpec {
  // 类型
  private String type;
  // 对象
  private Map<String, JSONSchemaSpec> properties;
  // 数组
  private JSONSchemaSpec items;
  // 必填
  private List<String> required;
  // 兼容JSON文本节点
  private boolean compatibleTextNode = false;
}
