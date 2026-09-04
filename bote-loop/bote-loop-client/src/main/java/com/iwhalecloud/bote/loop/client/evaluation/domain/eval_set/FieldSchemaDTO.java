package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldDisplayFormatDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldStatusDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.FieldTransformationConfigDTO;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.MultiModalSpecDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.ContentTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段Schema数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "字段Schema")
public class FieldSchemaDTO {

  @Schema(description = "唯一键")
  private String key;

  @Schema(description = "展示名称")
  private String name;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "内容类型")
  private ContentTypeDTO contentType;

  @Schema(description = "默认渲染格式")
  private FieldDisplayFormatDTO defaultDisplayFormat;

  @Schema(description = "字段状态")
  private FieldStatusDTO status;

  @Schema(description = "是否必填")
  private Boolean isRequired;

  @Schema(description = "文本Schema")
  private String textSchema;

  @Schema(description = "多模态规格限制")
  private MultiModalSpecDTO multiModelSpec;

  @Schema(description = "是否隐藏")
  private Boolean hidden;

  @Schema(description = "默认转换配置")
  private List<FieldTransformationConfigDTO> defaultTransformations;
}
