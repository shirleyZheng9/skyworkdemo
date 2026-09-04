package com.iwhalecloud.bote.common.interceptor;

import com.google.common.collect.ImmutableList;
import com.iwhalecloud.bote.common.annotation.EncryptField;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.common.util.FieldEncryptUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * MyBatis字段加密拦截器
 *
 * <p>拦截查询和更新操作，自动对标记了{@link EncryptField}注解的字段进行加密处理</p>
 *
 * @author tingyun.wang
 * @since 2025-07-10
 */
@Component
@Intercepts({
  @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
  @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@SuppressWarnings({"PMD.UnusedFormalParameter", "PMD.GuardLogStatement"})
public class MybatisParamsInterceptor implements Interceptor {

  private final Logger logger = LoggerFactory.getLogger(MybatisParamsInterceptor.class);

  /** 需要跳过拦截的 Mapper 文件路径 */
  private static final List<String> SKIP_MAPPER_LIST = ImmutableList.of(
    "com/iwhalecloud/bote/mapper/base/DcParamQueryMapper.xml"
  );

  @Override
  public Object intercept(Invocation invocation) throws InvocationTargetException, IllegalAccessException {
    Object[] args = invocation.getArgs();

    // 基本参数检查
    if (!(args[0] instanceof MappedStatement) || !(invocation.getTarget() instanceof Executor)) {
      return invocation.proceed();
    }

    MappedStatement mappedStatement = (MappedStatement) args[0];

    // 检查是否需要跳过处理
    if (shouldSkipIntercept(mappedStatement)) {
      return invocation.proceed();
    }

    // 处理参数加密
    Object parameterObject = args[1];
    if (parameterObject != null) {
      args[1] = encryptParameter(parameterObject, mappedStatement);
    }

    return invocation.proceed();
  }

  /**
   * 判断是否应该跳过拦截处理
   */
  private boolean shouldSkipIntercept(MappedStatement mappedStatement) {
    String resource = mappedStatement.getResource();
    return SKIP_MAPPER_LIST.contains(resource) || !isEncryptionEnabled();
  }

  /**
   * 检查是否开启字段加密功能
   */
  private boolean isEncryptionEnabled() {
    return SystemParameter.ENCRYPT_FIELD_ENABLED.getBooleanValueFromDb();
  }

  /**
   * 加密参数对象的核心入口方法
   */
  @SuppressWarnings("unchecked")
  private Object encryptParameter(Object parameter, MappedStatement mappedStatement) {
    if (parameter == null) {
      return null;
    }

    // 根据参数类型选择相应的处理策略
    if (parameter instanceof Map) {
      return encryptMapParameter((Map<Object, Object>) parameter, mappedStatement);
    }
    else if (parameter instanceof List) {
      return encryptListParameter((List<Object>) parameter, mappedStatement);
    }
    else if (parameter instanceof String) {
      return encryptStringParameter((String) parameter, mappedStatement);
    }
    else {
      return encryptObjectParameter(parameter, mappedStatement);
    }
  }

  /**
   * 处理 Map 类型参数
   */
  private Map<Object, Object> encryptMapParameter(Map<Object, Object> map, MappedStatement mappedStatement) {
    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      Object key = entry.getKey();
      Object value = entry.getValue();

      if (value != null) {
        // 根据值的类型进行相应处理
        if (value instanceof String && isMethodParameterEncrypted(mappedStatement, String.valueOf(key))) {
          entry.setValue(FieldEncryptUtil.encryptData((String) value));
        }
        else {
          entry.setValue(encryptParameter(value, mappedStatement));
        }
      }
    }
    return map;
  }

  /**
   * 处理 List 类型参数
   */
  private List<Object> encryptListParameter(List<Object> list, MappedStatement mappedStatement) {
    // 处理不可变列表
    List<Object> mutableList = ensureMutableList(list);

    for (int i = 0; i < mutableList.size(); i++) {
      Object item = mutableList.get(i);
      mutableList.set(i, encryptParameter(item, mappedStatement));
    }

    return mutableList;
  }

  /**
   * 处理 String 类型参数
   */
  private String encryptStringParameter(String parameter, MappedStatement mappedStatement) {
    if (isMethodSingleParameterEncrypted(mappedStatement)) {
      return FieldEncryptUtil.encryptData(parameter);
    }
    return parameter;
  }

  /**
   * 处理普通对象类型参数
   */
  private Object encryptObjectParameter(Object obj, MappedStatement mappedStatement) {
    if (obj == null || !isEncryptableObject(obj)) {
      return obj;
    }

    Class<?> clazz = obj.getClass();

    // 遍历对象的所有字段
    for (Field field : FieldUtils.getAllFieldsList(clazz)) {
      if (shouldEncryptField(field, mappedStatement)) {
        encryptFieldValue(obj, field, mappedStatement);
      }
    }

    return obj;
  }

  /**
   * 加密对象字段的值
   */
  @SuppressWarnings("unchecked")
  private void encryptFieldValue(Object obj, Field field, MappedStatement mappedStatement) {
    try {
      field.setAccessible(true);
      Object fieldValue = field.get(obj);

      if (fieldValue == null) {
        return;
      }

      // 根据字段值类型进行加密处理
      if (fieldValue instanceof String) {
        field.set(obj, FieldEncryptUtil.encryptData((String) fieldValue));
      } else if (fieldValue instanceof List) {
        field.set(obj, encryptListParameter((List<Object>) fieldValue, mappedStatement));
      } else {
        field.set(obj, encryptParameter(fieldValue, mappedStatement));
      }
    }
    catch (Exception e) {
      logger.error("Failed to encrypt field value. error={}", ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 确保 List 是可变的（处理 singletonList 等不可变列表）
   */
  private List<Object> ensureMutableList(List<Object> list) {
    try {
      // 尝试修改操作来检测是否可变
      if (!list.isEmpty()) {
        Object first = list.get(0);
        list.set(0, first);
      }
      return list;
    }
    catch (UnsupportedOperationException e) {
      // 如果是不可变列表，创建新的可变列表
      return new ArrayList<>(list);
    }
  }

  /**
   * 判断对象是否可以进行加密处理
   */
  private boolean isEncryptableObject(Object obj) {
    return AnnotationUtils.findAnnotation(obj.getClass(), EncryptField.class) != null;
  }

  /**
   * 判断字段是否需要加密
   */
  private boolean shouldEncryptField(Field field, MappedStatement mappedStatement) {
    return field.isAnnotationPresent(EncryptField.class) ||
           isMethodParameterEncrypted(mappedStatement, field.getName());
  }

  /**
   * 检查方法的单个参数是否标记了加密注解
   */
  private boolean isMethodSingleParameterEncrypted(MappedStatement mappedStatement) {
    Method method = getMapperMethod(mappedStatement);
    if (method == null) {
      return false;
    }

    for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
      for (Annotation annotation : parameterAnnotations) {
        if (annotation instanceof EncryptField) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 检查指定名称的方法参数是否标记了加密注解
   */
  private boolean isMethodParameterEncrypted(MappedStatement mappedStatement, @Nullable String parameterName) {
    Method method = getMapperMethod(mappedStatement);
    if (method == null || parameterName == null) {
      return false;
    }

    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    Parameter[] parameters = method.getParameters();

    for (int i = 0; i < parameterAnnotations.length; i++) {
      if (hasEncryptFieldAnnotation(parameterAnnotations[i])) {
        if (matchesParameterName(parameterAnnotations[i], parameters[i], parameterName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 检查参数注解数组中是否包含 EncryptField 注解
   */
  private boolean hasEncryptFieldAnnotation(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof EncryptField) {
        return true;
      }
    }
    return false;
  }

  /**
   * 检查参数名称是否匹配
   */
  private boolean matchesParameterName(Annotation[] annotations, Parameter parameter, String targetName) {
    // 首先检查 @Param 注解的值
    for (Annotation annotation : annotations) {
      if (annotation instanceof Param) {
        return targetName.equals(((Param) annotation).value());
      }
    }
    // 如果没有 @Param 注解，则比较参数名
    return targetName.equals(parameter.getName());
  }

  /**
   * 根据 MappedStatement 获取对应的 Mapper 方法
   */
  @Nullable
  private Method getMapperMethod(MappedStatement mappedStatement) {
    String statementId = mappedStatement.getId();
    int lastDot = statementId.lastIndexOf(".");

    if (lastDot == -1) {
      return null;
    }

    String namespace = statementId.substring(0, lastDot);
    String methodName = statementId.substring(lastDot + 1);

    try {
      Class<?> mapperInterface = Class.forName(namespace);
      for (Method method : mapperInterface.getDeclaredMethods()) {
        if (method.getName().equals(methodName)) {
          return method;
        }
      }
    } catch (ClassNotFoundException e) {
      // 忽略异常，返回 null
    }

    return null;
  }

  @Override
  public void setProperties(Properties properties) {
    // 默认实现，预留扩展点
  }
}
