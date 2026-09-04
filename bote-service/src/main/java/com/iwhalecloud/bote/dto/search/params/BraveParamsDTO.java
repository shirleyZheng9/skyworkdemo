package com.iwhalecloud.bote.dto.search.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Brave 搜索参数配置 DTO
 *
 * @author wangtingyun
 * @since 2026-03-30
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BraveParamsDTO {
  /** API Key */
  private String apiKey;
  /** 请求地址 */
  private String url;
  /** 返回结果数量 */
  private Integer count;
}
