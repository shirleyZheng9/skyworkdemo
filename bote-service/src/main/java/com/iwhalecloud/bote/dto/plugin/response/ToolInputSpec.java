package com.iwhalecloud.bote.dto.plugin.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.common.enums.ToolInputType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 工具入参配置
 *
 * @author bianjp
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ToolInputSpec {
  /** header 参数列表 */
  private List<ParameterSpec> header;
  /** path 参数列表 */
  private List<ParameterSpec> path;
  /** query 参数列表 */
  private List<ParameterSpec> query;
  /** body 参数列表 */
  private List<ParameterSpec> body;
  /** body 类型 */
  private ToolInputType bodyType;

  /**
   * 整合所有入参定义，方便 LLM 工具调用
   */
  @JsonIgnore
  public ParameterSpec convert() {
    if (CollectionUtils.isEmpty(header) && CollectionUtils.isEmpty(path) && CollectionUtils.isEmpty(query) && CollectionUtils.isEmpty(body)) {
      return null;
    }
    ParameterSpec root = ParameterSpec.newRoot();
    List<ParameterSpec> children = new ArrayList<>();
    root.setChildren(children);
    if (CollectionUtils.isNotEmpty(header)) {
      children.add(ParameterSpec.newObject("header", "header", header));
    }
    if (CollectionUtils.isNotEmpty(path)) {
      children.add(ParameterSpec.newObject("path", "path", path));
    }
    if (CollectionUtils.isNotEmpty(query)) {
      children.add(ParameterSpec.newObject("query", "query", query));
    }
    if (CollectionUtils.isNotEmpty(body)) {
      children.add(ParameterSpec.newObject("body", "body", body));
    }
    return root;
  }
}
