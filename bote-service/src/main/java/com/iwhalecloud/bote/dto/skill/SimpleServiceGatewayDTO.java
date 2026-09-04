package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 简单服务网关信息
 *
 * @author bianjp
 * @since 2024-08-14
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleServiceGatewayDTO {
  /** 环境地址 */
  private String url;
  /** 请求头 JSON */
  private String headerJson;
  /** 请求头(headerJson 解析结果) */
  private Map<String, String> headers;

  /**
   * 解析请求头
   */
  public void parse() {
    if (StringUtils.isNotEmpty(headerJson)) {
      List<HeaderItem> headerItems = JsonUtil.parseJsonRequired(headerJson, new TypeReference<List<HeaderItem>>() {
      });
      if (CollectionUtils.isNotEmpty(headerItems)) {
        headers = new HashMap<>();
        for (HeaderItem headerItem : headerItems) {
          headers.put(headerItem.getName(), headerItem.getValue());
        }
      }
    }
    headerJson = null;
  }

  /**
   * 请求头
   */
  @Getter
  @Setter
  @ToString
  private static final class HeaderItem {
    /** 请求头名称 */
    private String name;
    /** 请求头值 */
    private String value;
  }
}
