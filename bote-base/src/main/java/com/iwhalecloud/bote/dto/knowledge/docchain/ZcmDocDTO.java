package com.iwhalecloud.bote.dto.knowledge.docchain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 研发云文档中心的文档对象
 *
 * @author bianjp
 * @since 2024-11-30
 */
@Getter
@Setter
@ToString
public class ZcmDocDTO {
  /** 文档 ID */
  private Integer docId;
  /** 文档名称 */
  private String name;
  /** 别名 */
  private String alias;
  /** 类型 */
  private String type;
  /** 子文档列表 */
  private List<ZcmDocDTO> children;

  /**
   * 是否是文档
   */
  @JsonIgnore
  public boolean isDoc() {
    return "DOC".equals(type);
  }

  /**
   * 获取别名或名称
   */
  @JsonIgnore
  public String getAliasOrName() {
    return StringUtils.isNotEmpty(alias) ? alias : name;
  }
}
