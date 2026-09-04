package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实体关系数据对象
 *
 * @author qian.sisheng
 * @since 2025-12-04
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class EntityInfoDTO {
  /** 实体类型 */
  private String entityType;
  /** 实体ID */
  private Long entityId;
  /** 实体名称 */
  private String entityName;
  /** 实体编码 */
  private String entityCode;
}
