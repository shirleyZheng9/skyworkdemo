package com.iwhalecloud.bote.doc.module.knowledge.dto.knowledgegraph.rsp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * knowledgeGraph 通用 JSON 响应包装（与第三方 code / message / payload 对齐）
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class KnowledgeGraphApiResponse<T> {

  /** 响应码 0成功 */
  private Integer code;
  /** 响应信息 */
  private String message;
  /** 响应数据 */
  private T payload;
}
