package com.iwhalecloud.bote.portal.config.properties;

/**
 * 天工共享 Cookie 门户配置。
 *
 * <p>复用 {@code bt_external_portal} 的现有字段，不新增配置表，也不把环境相关的 Cookie 名称和
 * 登录地址写死在代码中。这样 SIT、生产环境可以使用各自的门户记录完成配置。</p>
 *
 * @author zhengxueli
 * @since 2026-09-07
 */
public class TGPortalProperties extends AbstractPortalProperties {

  /**
   * 是否允许根据天工当前项目自动创建 AADP 租户。
   * 最终结果还要与入口 state 中的 targetPlatforms 联合判断，不能只看门户开关。
   * targetPlatforms本质上是一个项目准入开关，避免所有天工项目都被自动同步到 AADP。
   */
  private Boolean autoCreateTenant;

  public Boolean getAutoCreateTenant() {
    return autoCreateTenant;
  }

  public void setAutoCreateTenant(Boolean autoCreateTenant) {
    this.autoCreateTenant = autoCreateTenant;
  }

  @Override
  public void afterPropertiesSet() {
    // 门户配置来自 bt_external_portal，由保存接口做必填校验，避免在这里重复维护校验规则。
  }
}
