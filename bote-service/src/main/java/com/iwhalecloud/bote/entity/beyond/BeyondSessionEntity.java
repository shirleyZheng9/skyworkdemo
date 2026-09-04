package com.iwhalecloud.bote.entity.beyond;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应会话
 *
 * <p>记录百应会话 ID 和登录token的关联关系</p>
 *
 * @author 赵旭
 * @since 2025-08-04
 */
@Getter
@Setter
@ToString
public class BeyondSessionEntity {
  /** 百应会话 ID ,具有唯一性,作为主键*/
  private String beyondSessionId;
  /** 租户 ID */
  private Long tenantId;
  /** 鲸加TOKEN */
  private String ssoToken;
  /** 状态 */
  private String statusCd;
  /** 创建人 */
  private Long creatorId;
  /** 创建时间 */
  private Date createdTime;
  /** 更新时间 */
  private Date updatedTime;
}
