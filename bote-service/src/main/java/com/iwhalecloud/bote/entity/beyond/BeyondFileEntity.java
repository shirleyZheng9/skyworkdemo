package com.iwhalecloud.bote.entity.beyond;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应文件
 *
 * <p>记录百应文件 ID 和博特文件的关联关系</p>
 *
 * @author bianjp
 * @since 2025-07-19
 */
@Getter
@Setter
@ToString
public class BeyondFileEntity {
  /** 主键 */
  private Long id;
  /** 租户 ID */
  private Long tenantId;
  /** 百应文件 ID */
  private Long beyondFileId;
  /** 博特文件 ID */
  private Long fileId;
  /** 状态 */
  private String statusCd;
  /** 创建人 */
  private Long creatorId;
  /** 创建时间 */
  private Date createdTime;
}
