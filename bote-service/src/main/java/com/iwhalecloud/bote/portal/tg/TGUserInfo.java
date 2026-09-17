package com.iwhalecloud.bote.portal.tg;

/**
 * 天工认证上下文的 AADP 中间模型。
 *
 * <p>该类型不依赖 starter DTO。真实 starter 接入层负责将其返回对象转换为本类型。</p>
 *
 * @author zhengxueli
 * @since 2026-09-07
 */
public class TGUserInfo {
  /** 天工用户 ID，是 AADP 识别存量外部用户的稳定标识。 */
  private final Long userId;
  /** 天工登录名，仅用于真实姓名兜底，不作为 AADP 的稳定用户编码。 */
  private final String username;
  /** 天工昵称，优先映射为 AADP realName。 */
  private final String nickname;
  /** 用户手机号；由 starter 返回已授权、可使用的值。 */
  private final String phoneNo;
  /** 当前天工团队 ID，对应 AADP workspace 的 extSpaceId。 */
  private final Long currentTenantId;
  /** 当前天工团队名称，对应 AADP workspace 名称。 */
  private final String currentTenantName;
  /** 天工角色类型仅保留上下文，不直接映射为 AADP 管理角色。 */
  private final String roleType;

  private TGUserInfo(Builder builder) {
    this.userId = builder.userId;
    this.username = builder.username;
    this.nickname = builder.nickname;
    this.phoneNo = builder.phoneNo;
    this.currentTenantId = builder.currentTenantId;
    this.currentTenantName = builder.currentTenantName;
    this.roleType = builder.roleType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getUserId() { return userId; }
  public String getUsername() { return username; }
  public String getNickname() { return nickname; }
  public String getPhoneNo() { return phoneNo; }
  public Long getCurrentTenantId() { return currentTenantId; }
  public String getCurrentTenantName() { return currentTenantName; }
  public String getRoleType() { return roleType; }

  /** 显式 Builder，避免认证核心代码依赖 Lombok 注解处理器。 */
  public static final class Builder {
    private Long userId;
    private String username;
    private String nickname;
    private String phoneNo;
    private Long currentTenantId;
    private String currentTenantName;
    private String roleType;

    private Builder() { }

    public Builder userId(Long value) { this.userId = value; return this; }
    public Builder username(String value) { this.username = value; return this; }
    public Builder nickname(String value) { this.nickname = value; return this; }
    public Builder phoneNo(String value) { this.phoneNo = value; return this; }
    public Builder currentTenantId(Long value) { this.currentTenantId = value; return this; }
    public Builder currentTenantName(String value) { this.currentTenantName = value; return this; }
    public Builder roleType(String value) { this.roleType = value; return this; }

    public TGUserInfo build() { return new TGUserInfo(this); }
  }
}
