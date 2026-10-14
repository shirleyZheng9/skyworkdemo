package com.iwhalecloud.bote.dto.portal.tiangong;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 天工项目成员同步响应
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 明确返回每个成员的处理结果和创建人绑定结果，供 MQ 消费端记录审计信息。
 */
@Getter
@Setter
public class TiangongProjectUserSyncResponse {
  /** 原样回传 MQ 消息标识，便于调用方校验响应归属。 */
  private String requestId;
  /** 实际命中的 AADP 租户 ID。 */
  private Long tenantId;
  /** 创建人绑定结果：BOUND、UNCHANGED 或 NOT_REQUESTED。 */
  private String creatorAction;
  /** 与请求用户顺序一致的逐人成员处理结果。 */
  private List<UserAction> users = new ArrayList<>();

  /** 单个天工用户在 AADP 项目成员同步中的处理结果。 */
  @Getter
  @Setter
  public static class UserAction {
    /** 天工外部用户 ID。 */
    private String extUserId;
    /** 命中的 AADP 业务用户 ID；用户不存在时为空。 */
    private Long userId;
    /** 新增为 CREATED/EXISTS，移除为 REMOVED/NOT_FOUND。 */
    private String memberAction;
  }
}
