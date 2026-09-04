package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.entity.skill.SkillPageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：页面 DTO
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SkillPageDTO extends SkillPageEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "文件 ID")
  private Long fileId;
  @Schema(description = "文件信息")
  private FileInfoDTO fileInfo;
  @Schema(description = "复制页面 ID")
  private Long copyPageId;
}
