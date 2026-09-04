package com.iwhalecloud.bote.common.annotation;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要OAuth2认证标记注解, 区别于session认证、apiKey认证的另一认证方式
 * <p>
 * 用于标记需要OAuth2 accessToken认证的接口，支持类级别和方法级别使用。
 * 方法级别注解优先级高于类级别注解。
 * </p>
 * <p>
 * Tips: 由于底层litchi包中SpringSecurity的配置拦截未判断元数据类，此处的@IgnoreSession无效，必须单独在实现接口类中增加
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 类级别：整个Controller都需要OAuth2认证
 * &#64;RestController
 * &#64;RequireOAuth2
 * public class UserController {
 *     // ...
 * }
 *
 * // 方法级别：特定接口需要OAuth2认证
 * &#64;GetMapping("/users/{id}")
 * &#64;RequireOAuth2
 * public ResponseEntity&lt;UserVO&gt; getUser(@PathVariable Long id) {
 *     // ...
 * }
 *
 * // 指定权限范围
 * &#64;PostMapping("/admin/users")
 * &#64;RequireOAuth2(scope = {"admin", "write"})
 * public ResponseEntity&lt;UserVO&gt; createUser(@RequestBody CreateUserDTO dto) {
 *     // ...
 * }
 * </pre>
 * </p>
 *
 * @author Aiqing
 * @since 2025/12/26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@IgnoreSign
@IgnoreSession
public @interface RequireOAuth2 {

  /**
   * 需要的权限范围（scope）
   * <p>
   * 如果指定了scope，则验证accessToken的scope是否包含所有指定的权限。
   * 如果token的scope不满足要求，将返回403 Forbidden。
   * </p>
   * <p>
   * 示例：scope = {"admin", "write"} 表示需要admin和write两个权限
   * </p>
   *
   * @return 权限范围数组
   */
  String[] scope() default {};

  /**
   * 是否必须认证
   * <p>
   * - true（默认）：必须提供有效的accessToken，否则返回401
   * - false：可选认证，如果提供了token则验证，未提供token也允许访问
   * </p>
   * <p>
   * 当required=false时，如果提供了token但验证失败，仍然返回401。
   * 如果未提供token，则允许访问，但不会设置用户信息到线程变量。
   * </p>
   *
   * @return 是否必须认证
   */
  boolean required() default true;
}

