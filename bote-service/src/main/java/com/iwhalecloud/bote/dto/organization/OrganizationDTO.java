package com.iwhalecloud.bote.dto.organization;

import com.iwhalecloud.bote.entity.organization.OrganizationEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 组织信息
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "组织信息")
public class OrganizationDTO extends OrganizationEntity {
  @Schema(description = "扩展字段")
  private Map<String, Object> extFieldsMap;
}
