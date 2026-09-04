package com.iwhalecloud.bote.dto.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.entity.app.WebAppEntity;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网页应用 DTO
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_web_app")
@JsonInclude(Include.NON_NULL)
public class WebAppDTO extends WebAppEntity {

  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "租户名称")
  private String tenantName;
  @Schema(description = "企业名称")
  private String spaceName;

}
