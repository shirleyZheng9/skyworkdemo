package com.iwhalecloud.bote.dto.model;

import com.iwhalecloud.bote.dto.model.query.CorpusParams;
import com.iwhalecloud.bote.entity.model.ModelFinetuneEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 模型微调 DTO
 *
 * @author auto
 * @since 2025-02-19
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ModelFinetuneDTO extends ModelFinetuneEntity {
  @Schema(description = "创建人名称")
  private String creatorName;

  @Schema(description = "推测文本")
  private String text;

  @Schema(description = "语料参数")
  private List<CorpusParams> corpus;
}
