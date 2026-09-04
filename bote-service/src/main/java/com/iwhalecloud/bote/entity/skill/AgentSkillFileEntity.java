package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Agent Skill 文件
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_agent_skill_file")
@Schema(description = "Agent Skill 文件")
public class AgentSkillFileEntity extends BaseEntity {
 @DiffId
 @Schema(description = "文件主键")
 private Long skillFileId;
 @DiffField
 @Schema(description = "所属目录 ID")
 private Long dirId;
 @DiffField
 @Schema(description = "技能 ID")
 private Long skillId;
 @DiffField
 @Schema(description = "租户 ID")
 private Long tenantId;
 @DiffField
 @Schema(description = "文件名")
 private String fileName;
 @DiffField
 @Schema(description = "文件类型（后缀小写，不含点）")
 private String fileType;
 @DiffField
 @Schema(description = "文本文件内容")
 private String fileContent;
 @DiffField
 @Schema(description = "二进制文件对应的 bt_file_info 主键")
 private Long fileInfoId;
}

