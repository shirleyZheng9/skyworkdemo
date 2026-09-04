package com.iwhalecloud.bote.dto.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.io.Resource;

/**
 * Agent Skill 简单信息
 *
 * @author bianjp
 * @since 2026-02-03
 */
@Getter
@Setter
@ToString
public class SimpleAgentSkillDTO {
  @Schema(description = "技能 ID")
  private Long skillId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "技能名称")
  private String skillName;
  @Schema(description = "技能编码")
  private String skillCode;
  @Schema(description = "已安装技能版本")
  private String skillVersion;
  @Schema(description = "文件 ID")
  private Long fileId;

  @Schema(description = "是否是调试(不要求上架)")
  private Boolean debug;

  /** 平台级技能的压缩包 */
  @JsonIgnore
  private Resource resource;
  /** 文件信息 */
  private FileInfoVO fileInfo;
  /** 文件的 MD5 哈希值 */
  private String md5sum;
  @Schema(description = "关联的工具")
  private List<SimpleAgentSkillToolDTO> tools;

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("tenantId", tenantId);
    map.put("skillId", skillId);
    map.put("skillName", skillName);
    map.put("skillCode", skillCode);
    return map;
  }
}
