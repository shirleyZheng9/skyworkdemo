package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.PublishRecordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 发布记录 DTO
 *
 * @author auto
 * @since 2024-10-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PublishRecordDTO extends PublishRecordEntity {
  private List<PublishStepDTO> steps;
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "文件信息")
  private String fileInfo;
  @Schema(description = "入参 JSON")
  private Map<String, Object> inputJson;
  @Schema(description = "复制的租户 ID")
  private Long copyTenantId;
  @Schema(description = "复制的租户名称")
  private String copyTenantName;
}
