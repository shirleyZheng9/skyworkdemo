package com.iwhalecloud.bote.portal.config.properties;

import com.iwhalecloud.bote.llm.client.dto.HeaderItem;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 门户配置抽象类
 *
 * @author bianjp
 * @since 2024-10-28
 */
@Getter
@Setter
@ToString
@SuppressWarnings("PMD.AbstractClassWithoutAnyMethod")
public abstract class AbstractPortalProperties implements InitializingBean {
  /** cookie 名称 */
  @NotEmpty
  protected String cookieName = "SESSION";
  /** URL 参数名称 */
  protected String paramName;
  /** 新用户的默认租户 ID */
  protected Long defaultTenantId;
  /** 新用户的默认角色 */
  protected String defaultRole;
  /** 访问地址 */
  protected String loginUrl;
  /** 检查登录状态接口地址 */
  protected String loggedUrl;
  /** 自定义请求头列表 */
  protected List<HeaderItem> headers;
}
