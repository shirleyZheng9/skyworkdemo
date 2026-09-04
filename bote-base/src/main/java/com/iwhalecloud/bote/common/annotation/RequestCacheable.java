package com.iwhalecloud.bote.common.annotation;

import com.iwhalecloud.bote.common.requestcache.RequestCacheableParamResolver;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;

/**
 * 请求可缓存注解
 *
 * <p>用于 Controller 的方法，以标记请求的响应可以被浏览器等客户端缓存，并指定计算 ETag 的方式（ETag 用于验证缓存是否有效）。</p>
 *
 * <p>支持两种指定 ETag 的方式:</p>
 * <ol>
 *   <li>执行指定 SQL: 执行 {@link #sql()} 指定的查询语句, 使用 SQL 参数 + 查询结果计算 ETag。适用于能用一条 SQL 就查出判断缓存是否有效的关键信息（比如数据的修改时间）的简单场景。</li>
 *   <li>调用指定方法: 在 Controller 实例上调用 {@link #method()} 指定的方法名称，使用方法的返回值计算 ETag。适用于复杂场景，可以根据 Controller 方法参数做任意的计算。</li>
 * </ol>
 *
 * <p>不要求，实际上也难以完全可靠地计算 ETag。一般使用数据的查询条件 + 修改时间计算即可, 有多条记录的取最新一条记录的修改时间，涉及多个表的只取会被单独更新的表的修改时间。</p>
 *
 * <p>URL 参数不需要用于计算 ETag, 浏览器缓存时会将 URL 路径相同但参数不同的请求地址视为不同的资源，即使后端返回的 ETag 相同也不会混淆缓存。</p>
 *
 * <p>由于对每次请求都需要计算 ETag, 因此计算 ETag 的开销必须尽量降低以减少对应用性能的负面影响。</p>
 *
 * <p>应只对必要且缓存命中率高的请求做缓存，否则反而会影响系统性能（增加了处理切面和计算 ETag 的开销）。</p>
 *
 * <p>只适用于 GET 请求。</p>
 *
 * @author bianjp
 * @since 2023-06-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestCacheable {

  /**
   * SQL 查询语句
   *
   * <p>SQL 中可以使用 <code>#{param}</code> 语法指定参数，Controller 方法的参数可以使用 param1, param2, ..., paramN 表示，
   * 此外还可以使用 {@link RequestCacheableParamResolver} 提供的公共参数，如 appId, userId。</p>
   *
   * <p>支持引用参数的嵌套属性，比如 <code>#{param1.id}</code> 表示使用 Controller 方法第一个参数的 id 属性。</p>
   *
   * <p>使用了无法解析的参数时会报错而不会当作 null, 以便及时发现代码问题。</p>
   *
   * <p>SQL 语句要求:</p>
   * <ol>
   *   <li>查询结果可以是单个或多个字段、单条或多条记录，如果返回多条记录要保证顺序的稳定。</li>
   *   <li>应该只查出 id, updated_time 等足以判断缓存是否有效的基本信息，不要查出大字段、大量记录。</li>
   *   <li>可以使用聚合函数如 MAX(updated_time) 减少返回的数据量。</li>
   *   <li>要兼容不同数据库类型（只使用所有数据库都支持的语法、函数）。不支持使用 MyBatis 语法，有复杂需求时请使用 {@link #method()} 方式实现。</li>
   * </ol>
   *
   * @return SQL 查询语句
   */
  @Language("SQL")
  String sql() default "";

  /**
   * 要调用的方法名称
   *
   * <p>方法要求:</p>
   * <ol>
   *   <li>方法应放在添加同一 Controller 内，建议与添加此注解的 Controller 方法紧挨着，以方便修改 Controller 方法时同步修改。</li>
   *   <li>方法的参数数量、顺序、类型必须与 Controller 方法完全相同，但不需要参数上的注解。</li>
   *   <li>方法的 scope 建议配置为 protected。配置为 private 会导致代码分析工具误认为方法无用要求删除，或误认为部分参数无用要求删除；配置为 public 会导致外部可以调用。</li>
   *   <li>方法名称建议命名为 buildXXXEtagKeys。</li>
   *   <li>方法的返回值可以是任意类型，单个基本类型的值、列表（不要使用数组）、map、对象均可，但应有合理的 toString 实现（数据无变化时转为的字符串应该稳定，数据有变化时转为的字符串应该变化）。</li>
   *   <li>方法的返回值应足以判断缓存是否有效，如果计算时使用的参数不是来源于 URL 查询字符串（比如 URL 参数的默认值，或 session 中的 userId），则应将这些信息也放到返回值中。</li>
   *   <li>只返回足以判断缓存是否有效的基本信息（比如 id, updated_time）即可，不要返回大对象。</li>
   *   <li>如果参数值的类型为集合，执行时会展开为多个参数，这样可以支持 IN 条件。</li>
   * </ol>
   *
   * @return 方法名称
   */
  String method() default "";

  /**
   * 查不到数据时是否缓存
   *
   * <p>查不到数据是指 SQL 查不到记录，或查到空记录，或者方法的返回值为 null。</p>
   *
   * <p>查不到数据时默认不允许缓存（一般这种情况下业务逻辑会主动报错），如果有特殊场景需要支持缓存（比如查不到数据时会使用默认值），可以配置为 true。</p>
   *
   * @return 查不到数据时是否缓存
   */
  boolean cacheOnNotFound() default false;
}
