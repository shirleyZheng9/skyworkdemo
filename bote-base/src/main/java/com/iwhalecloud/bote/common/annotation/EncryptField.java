package com.iwhalecloud.bote.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段存储加解密的注解：
 * <li>1.在需要加解密的字段及其类上添加该注解，可以实现字段在存储到数据库时自动加密，从数据库查询时自动解密</li>
 * <li>2.实现原理通过 Mybatis 拦截器实现</li>
 * <li>3.MybatisParamsInterceptor实现入库时加密处理功能，MybatisResultSetInterceptor实现查询时解密功能</li>
 * <b>注意</b>：加密字段入库之后，字段值会被变更为加密后的值，如果入库之后还需要使用该字段的明文值时需要单独对其解密。
 *
 * @author tingyun.wang
 * @since 2025-07-10
 */
@Inherited
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EncryptField {
}
