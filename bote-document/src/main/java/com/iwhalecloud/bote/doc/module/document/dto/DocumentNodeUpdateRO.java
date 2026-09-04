package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

/**
 *
 * 文档节点更新请求参数
 *
 * @author Aiqing
 * @since 2025/8/19
 */
@Getter
@Setter
@ToString
public class DocumentNodeUpdateRO extends TenantBaseRO {

  @Schema(description = "文档名称")
  @NotEmpty(message = "文档名称不能为空")
  @Length(min = 1, max = 100, message = "名称不能超过100个字符")
  @Pattern(regexp = "^[^<>:\"/\\\\|?*]+$", message = "文档名称包含非法字符，不允许包含 < > : \" / \\ | ? * 等特殊字符")
  private String documentName;

}
