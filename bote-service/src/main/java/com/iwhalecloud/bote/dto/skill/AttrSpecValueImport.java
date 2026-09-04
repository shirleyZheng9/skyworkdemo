package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bss.litchi.transform.dto.BatchImportDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 静态数据导入模型
 *
 * @author qian.sisheng
 * @since 2024/1/9
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttrSpecValueImport extends BatchImportDTO {

  private static final long serialVersionUID = -1L;

  @Schema(description = "静态属性编码")
  private String attrNbr;

  @Schema(description = "静态属性名称")
  private String attrName;

  @Schema(description = "静态属性说明")
  private String attrDesc;

  @Schema(description = "取值数据")
  private String attrValue;

  @Schema(description = "取值名称")
  private String attrValueName;
}
