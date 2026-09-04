package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 广场技能类型统计信息
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Schema(description = "SKILL广场数量统计")
public class SkillTypeStatisticsVO {
    @Schema(description = "技能类型")
    private String skillType;
    @Schema(description = "数量")
    private Integer count;
}
