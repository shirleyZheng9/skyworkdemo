package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ElasticSearch建议字段封装类
 *
 * @author lizuyin
 * @since 2025-01-29
 */
@Getter
@Setter
@ToString
@Schema(description = "ElasticSearch建议字段")
public class BoteEsSuggest {

  @Schema(description = "建议输入内容列表")
  private List<String> input;

  @Schema(description = "建议上下文，key为context名称，value为context值列表")
  private Map<String, List<String>> contexts;

  /**
   * 创建一个带有单个输入内容的建议对象
   *
   * @param inputText 输入内容
   * @return 建议对象
   */
  public static BoteEsSuggest createWithSingleInput(String inputText) {
    BoteEsSuggest suggest = new BoteEsSuggest();
    suggest.setInput(Collections.singletonList(inputText));
    suggest.setContexts(new HashMap<>());
    return suggest;
  }

  /**
   * 添加上下文
   *
   * @param contextName 上下文名称
   * @param contextValues 上下文值列表
   */
  public void addContext(String contextName, List<String> contextValues) {
    if (this.contexts == null) {
      this.contexts = new HashMap<>();
    }
    this.contexts.put(contextName, contextValues);
  }

  /**
   * 添加单个上下文值
   *
   * @param contextName 上下文名称
   * @param contextValue 上下文值
   */
  public void addSingleContext(String contextName, String contextValue) {
    addContext(contextName, Collections.singletonList(contextValue));
  }
} 