package com.iwhalecloud.bote.dto.bot;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 逻辑视图 DTO
 *
 * @author chen.linfa
 * @since 2024-09-25
 */
@Getter
@Setter
@ToString
public class LogicViewDTO {
  /** 主键 */
  private String id;
  /** 名称 */
  private String name;
  /** 类型 */
  private String type;
  /** 级别 */
  private Integer level;
  /** 父 ID */
  private String parentId;
  /** 属性 */
  private LogicViewAttrDTO attr;
  /** 子节点 */
  private List<LogicViewDTO> children;
}
