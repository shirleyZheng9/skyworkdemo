package com.iwhalecloud.bote.dto.skill;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * curl解析结果
 *
 * @author qian.sisheng
 * @since 2025-09-16
 */
@Getter
@Setter
@ToString
public class CurlParseResultDTO {
  /** 服务名称 */
  private String serviceName;
  /** 服务编码 */
  private String serviceCode;
  /** 相对路径 */
  private String relativePath;
  /** 请求方式 */
  private String reqMethod;
  /** 请求头 */
  private String headerJson;
  /** path参数 */
  private String pathJson;
  /** query参数 */
  private String queryJson;
  /** body参数 */
  private String bodyJson;
  /** 请求体类型, 如Multipart/form-data */
  private String bodyType;
}
