package com.iwhalecloud.bote.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.mcp.dto.ResourceContent;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 读取资源结果
 *
 * @author zhusenliang
 * @since 2026-09-08
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ReadResourceResult {
  /** 资源内容列表，文本资源返回 text，二进制资源返回 base64 编码的 blob */
  private List<ResourceContent> contents;
}
