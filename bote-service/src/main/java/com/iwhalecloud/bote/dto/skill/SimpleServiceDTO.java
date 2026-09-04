package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.ApiParamUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 服务
 *
 * @author bianjp
 * @since 2024-11-05
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimpleServiceDTO {
  /** 租户 ID */
  private Long tenantId;
  /** 服务 ID */
  private Long serviceId;
  /** 服务名称 */
  private String serviceName;
  /** 服务编码 */
  private String serviceCode;
  /** 平台 ID */
  private Long platformId;
  /** 请求方法 */
  private String reqMethod;
  /** 相对路径 */
  private String relativePath;
  /** 入参结构 */
  private ParameterSpec request;
  /** 出参结构 */
  private ParameterSpec response;
  /** 是否是 SSE 接口 */
  private Boolean sse;
  /** 是否开启接口模拟 */
  private Boolean mockEnabled;
  /** 默认模拟响应 */
  private Object defaultMockResponse;
  /** 模拟报文用例列表 */
  private List<SimpleServiceMockDTO> mocks;
  /** 请求体类型 */
  private String bodyType;
  /** 后置脚本 */
  private String postScript;
  /** 请求超时时间（毫秒） */
  private Integer connectTimeout;
  /** 读取超时时间（毫秒） */
  private Integer readTimeout;
  /** 是否接口加密 */
  private Boolean encrypt;

  /**
   * 转换服务对象
   */
  public static SimpleServiceDTO from(SkillServiceDTO service) {
    SimpleServiceDTO dto = new SimpleServiceDTO();
    dto.tenantId = service.getTenantId();
    dto.serviceId = service.getServiceId();
    dto.serviceName = service.getServiceName();
    dto.serviceCode = service.getServiceCode();
    dto.platformId = service.getPlatformId();
    dto.reqMethod = service.getReqMethod();
    dto.relativePath = service.getRelativePath();
    dto.bodyType = service.getBodyType();
    dto.postScript = service.getPostScript();
    dto.connectTimeout = service.getConnectTimeout();
    dto.readTimeout = service.getReadTimeout();
    dto.request = ApiParamUtil.buildRequestSpec(service);
    if (StringUtils.isNotEmpty(service.getResponseJson())) {
      dto.response = JsonUtil.parseJsonRequired(service.getResponseJson(), ParameterSpec.class);
    }
    dto.sse = BaseConsts.TRUE.equals(service.getIsSse());
    dto.encrypt = BaseConsts.TRUE.equals(service.getIsEncrypt());
    if (BaseConsts.TRUE.equals(service.getIsMock())) {
      dto.mockEnabled = true;
      if (StringUtils.isNotEmpty(service.getMockResponseJson())) {
        dto.defaultMockResponse = JsonUtil.parseJsonRequired(service.getMockResponseJson(), Object.class);
      }
    }
    return dto;
  }

}
