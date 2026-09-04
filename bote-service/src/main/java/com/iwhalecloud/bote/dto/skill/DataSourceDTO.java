package com.iwhalecloud.bote.dto.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bote.entity.skill.DataSourceEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：数据源 DTO
 *
 * @author auto
 * @since 2024-09-16
 */
@Getter
@Setter
@ToString(callSuper = true)
public class DataSourceDTO extends DataSourceEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @DiffField(childNode = true)
  @Schema(description = "数据源实例列表")
  private List<DataSourceInstDTO> insts;
}
