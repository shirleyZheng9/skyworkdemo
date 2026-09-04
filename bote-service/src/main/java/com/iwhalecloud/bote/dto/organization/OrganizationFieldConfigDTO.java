package com.iwhalecloud.bote.dto.organization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.organization.OrganizationFieldConfigEntity;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 组织字段配置
 *
 * @author zhao.xu104
 * @since 2025-09-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "组织字段配置")
public class OrganizationFieldConfigDTO extends OrganizationFieldConfigEntity {
  @Schema(description = "字段选项列表")
  private List<FieldOptionDTO> fieldOptionList;

  /**
   * 解析字段选项JSON
   */
  public void parseFieldOptions() {
    if (StringUtils.isNotEmpty(fieldOptions)) {
      this.fieldOptionList = JsonUtil.parseJson(fieldOptions, new TypeReference<List<FieldOptionDTO>>() { });
    }
    this.fieldOptions = null;
  }

  /**
   * 字段选项
   */
  @Getter
  @Setter
  @ToString
  @Schema(description = "字段选项")
  public static class FieldOptionDTO {
    @Schema(description = "选项值")
    private String value;
    @Schema(description = "选项标签")
    private String label;
  }
}
