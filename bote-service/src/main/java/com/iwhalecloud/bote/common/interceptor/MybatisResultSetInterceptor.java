package com.iwhalecloud.bote.common.interceptor;

import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.common.annotation.EncryptField;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.FieldEncryptUtil;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * MyBatis字段解密拦截器
 *
 * <p>将Mybatis查询结果中包含加密字段的数据进行解密处理</p>
 *
 * @author tingyun.wang
 * @since 2025-07-10
 */
@Component
@Intercepts({
  @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class)
})
@SuppressWarnings({"unchecked", "PMD.UnusedFormalParameter", "PMD.GuardLogStatement"})
public class MybatisResultSetInterceptor implements Interceptor {

  private final Logger logger = LoggerFactory.getLogger(MybatisResultSetInterceptor.class);

  /** 需要跳过拦截的 Mapper 文件路径 */
  private static final List<String> SKIP_MAPPER_LIST = ImmutableList.of(
    "com/iwhalecloud/bote/mapper/base/DcParamQueryMapper.xml"
  );

  @Override
  @Nullable
  public Object intercept(Invocation invocation) throws InvocationTargetException, IllegalAccessException {
    String resource;
    try {
      ResultSetHandler resultSetHandler = (ResultSetHandler) invocation.getTarget();
      MetaObject metaObject = SystemMetaObject.forObject(resultSetHandler);
      MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
      resource = mappedStatement.getResource();
    }
    catch (Exception e) {
      // 该异常不需要处理，是否抛异常由 invocation.proceed() 决定
      return invocation.proceed();
    }

    // 检查是否为需要跳过拦截
    if (shouldSkipIntercept(resource)) {
      return invocation.proceed();
    }

    // 取出查询的结果
    Object resultObject = invocation.proceed();
    if (resultObject == null) {
      return null;
    }
    //基于selectList
    if (resultObject instanceof ArrayList) {
      List<Object> resultList = (List<Object>) resultObject;
      return CollectionUtils.emptyIfNull(resultList).stream().map(this::handleSingleObject).collect(Collectors.toList());
    }
    //基于selectOne
    return handleSingleObject(resultObject);
  }

  /**
   * 判断是否应该跳过拦截处理
   */
  private boolean shouldSkipIntercept(String resource) {
    return SKIP_MAPPER_LIST.contains(resource) || !isEncryptionEnabled();
  }

  /**
   * 检查是否开启字段加密功能
   */
  private boolean isEncryptionEnabled() {
    return SystemParameter.ENCRYPT_FIELD_ENABLED.getBooleanValueFromDb();
  }

  /**
   * 处理单个对象
   */
  @Nullable
  private Object handleSingleObject(@Nullable Object result) {
    if (result == null) {
      return null;
    }
    //判断string对象是否是加密值
    if (result instanceof String) {
      String value = String.valueOf(result);
      if (value.startsWith(FieldEncryptUtil.ENCRYPT_FIELD_PRE)) {
        return FieldEncryptUtil.decryptData(value);
      }

    }
    //目前判断的是实体类是否存在加密字段注解 也可以换成判断是否是加密值
    else if (result.getClass().isAnnotationPresent(EncryptField.class)) {
      // 遍历单个对象的所有字段
      for (Field field : FieldUtils.getAllFieldsList(result.getClass())) {
        // 设置字段可访问，即使是私有字段也可以访问到
        field.setAccessible(true);
        try {
          // 判断字段是否存在加密字段注解, 存在则需要解密
          if (Objects.nonNull(field.get(result)) && Objects.nonNull(field.getAnnotation(EncryptField.class))) {
            field.set(result, JsonUtil.convert(FieldEncryptUtil.decryptData(String.valueOf(field.get(result))), field.getType()));
          }
        }
        catch (Exception e) {
          logger.error("Failed to decrypt field value. error={}", ExpUtil.getMsg(e), e);
        }
      }
    }
    return result;
  }


  @Override
  public Object plugin(Object target) {
    return Interceptor.super.plugin(target);
  }

  @Override
  public void setProperties(Properties properties) {
    Interceptor.super.setProperties(properties);
  }

}
