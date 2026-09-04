package com.iwhalecloud.bote.dto.knowledge;

import com.iwhalecloud.bote.entity.knowledge.CorpusInfoEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料
 *
 * @author qian.sisheng
 * @since 2025-3-10
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CorpusInfoDTO extends CorpusInfoEntity {
  @Schema(description = "文件ID")
  private Long fileInfoId;
  @Schema(description = "操作人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "表头列表")
  private List<String> parameters;
}
