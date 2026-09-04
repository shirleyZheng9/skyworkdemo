package com.iwhalecloud.bote.dto.a2a;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A2A 鉴权配置
 *
 * @author bianjp
 * @since 2025-09-17
 */
@Getter
@Setter
@ToString
@Schema(description = "A2A 鉴权配置")
public class A2aAuthConfig {
  @Schema(description = "请求头")
  private List<HeaderItem> headers;

  /**
   * 校验请求头配置
   */
  public void validate() {
    if (CollectionUtils.isEmpty(headers)) {
      return;
    }

    // 名称不能为空
    for (HeaderItem header : headers) {
      if (header == null || StringUtils.isBlank(header.getName())) {
        throw new IllegalArgumentException("请求头名称不能为空");
      }
      if (StringUtils.containsWhitespace(header.getName())) {
        throw new IllegalArgumentException("请求头名称不能包含空白字符: " + header.getName());
      }
    }

    // 名称不能重复
    Set<String> duplicateHeaderNames = headers.stream()
      .collect(Collectors.groupingBy(HeaderItem::getName, Collectors.counting()))
      .entrySet().stream()
      .filter(entry -> entry.getValue() > 1)
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
    if (!duplicateHeaderNames.isEmpty()) {
      throw new IllegalArgumentException("请求头名称不能重复: " + String.join(", ", duplicateHeaderNames));
    }
  }
}
