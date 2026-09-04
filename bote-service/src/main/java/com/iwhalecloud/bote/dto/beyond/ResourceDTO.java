package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 资源DTO
 *
 * @author lizuyin
 * @since 2025-07-21
 */
@Getter
@Setter
@ToString
public class ResourceDTO {
  /** 外系统编码 */
  private String systemCode;
  /** 资源来源ID */
  private Long resourceSourcePkId;
  /** 资源业务类型 */
  private String resourceBizType;
  /** 资源名称 */
  private String resourceName;
  /** 资源描述 */
  private String resourceDesc;
  /** 资源图标 */
  private String avatar;
  /** 常见问题 */
  private List<String> sample;
  /** 标签 */
  private String tags;
  /** 服务模式 */
  private String hostType;
  /** 资源类型 */
  private String resourceType;
  /** 参数 */
  private Object param;
}
