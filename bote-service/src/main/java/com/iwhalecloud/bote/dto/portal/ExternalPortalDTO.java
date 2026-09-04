package com.iwhalecloud.bote.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.entity.portal.ExternalPortalEntity;
import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 外部门户
 *
 * @author bianjp
 * @since 2025-02-24
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "外部门户")
public class ExternalPortalDTO extends ExternalPortalEntity {

  @Schema(description = "是否启用")
  private Boolean enabled;
  @Schema(description = "请求头")
  private List<HeaderItem> headers;

  /**
   * 构建请求头列表
   */
  public void buildHeaders(String headerJson) {
    if (StringUtils.isNotEmpty(headerJson)) {
      headers = JsonUtil.parseJsonRequired(headerJson, new TypeReference<>() {
      });
    }
  }

}
