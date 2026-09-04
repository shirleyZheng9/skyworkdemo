package com.iwhalecloud.bote.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import groovy.transform.ThreadInterrupt;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

/**
 * Groovy 工具类
 *
 * @author bianjp
 * @since 2024-08-08
 */
public final class GroovyUtil {
  private GroovyUtil() {
  }

  /** 编译结果缓存 */
  private static final Cache<@NonNull String, @NonNull Class<GroovyObject>> classCache;
  /** 编译器配置 */
  private static final CompilerConfiguration compilerConfiguration;
  /** 编译错误信息前缀模式，匹配开头的文件名称和行号。文件名称为 {@link #parseClass} 中调用 {@link GroovyClassLoader#parseClass(String, String)} 传递的 fileName 参数 */
  private static final Pattern compileErrorMsgPrefixPattern = Pattern.compile("^Script\\w*\\.groovy: \\d+: ");
  /** 约定调用方法名称 */
  private static final String INVOKE_METHOD_NAME = "invoke";

  static {
    classCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(Duration.ofHours(1)).build();

    compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
    compilerConfiguration.setSourceEncoding(StandardCharsets.UTF_8.name());
    // 超时时让脚本抛出 InterruptedException, 避免停不下来
    compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt.class));
  }

  /**
   * 执行 Groovy 代码中的 invoke 方法
   */
  @Nullable
  public static <T> T invoke(String code, Object... methodParameters) {
    return invoke(code, null, INVOKE_METHOD_NAME, methodParameters);
  }

  /**
   * 执行 Groovy 代码中的指定方法并返回结果
   *
   * <p>支持两种代码形式:</p>
   *
   * <ol>
   * <li>代码内容为脚本 (编译成 Script), 并定义了指定了方法</li>
   * <li>代码内容为一个类定义，类中定义了指定的方法</li>
   * </ol>
   *
   * @param code Groovy 代码
   * @param binding 绑定变量
   * @param method 方法名
   * @param methodParameters 参数
   * @return 执行结果
   */
  @Nullable
  public static <T> T invoke(String code, @Nullable Map<String, Object> binding, String method, Object... methodParameters) {
    Class<GroovyObject> clazz = parseClass(code);
    return invoke(clazz, binding, method, methodParameters);
  }

  /**
   * 执行 Groovy 代码中的指定方法并返回结果
   */
  @SuppressWarnings({"unchecked", "PMD.AvoidRethrowingException"})
  @Nullable
  public static <T> T invoke(Class<GroovyObject> clazz, @Nullable Map<String, Object> binding, String method, Object... methodParameters) {
    GroovyObject groovyObject = getGroovyObjectInstance(clazz);
    setBinding(groovyObject, binding);
    try {
      return (T) groovyObject.invokeMethod(method, methodParameters);
    }
    catch (BssException e) {
      throw e;
    }
    catch (MissingMethodException e) {
      // 优化 invoke 方法不存在或方法签名不匹配时的提示信息
      if (method.equals(e.getMethod())) {
        throw new BssException(String.format(method, FormatHelper.toTypeString(methodParameters)), e);
      }
      String msg = extractErrorMsgWithLineNum(e);
      throw new BssException("Groovy 脚本执行失败: " + msg, e);
    }
    catch (Exception e) {
      String msg = extractErrorMsgWithLineNum(e);
      throw new BssException("Groovy 脚本执行失败: " + msg, e);
    }
  }

  /**
   * 设置绑定变量
   *
   * @param groovyObject Groovy 对象
   * @param binding 绑定变量
   */
  private static void setBinding(GroovyObject groovyObject, @Nullable Map<String, Object> binding) {
    if (MapUtils.isNotEmpty(binding)) {
      if (groovyObject instanceof Script) {
        ((Script) groovyObject).setBinding(new Binding(binding));
      }
      else {
        for (Entry<String, Object> entry : binding.entrySet()) {
          groovyObject.setProperty(entry.getKey(), entry.getValue());
        }
      }
    }
  }

  /**
   * 获取一个新的 Groovy 对象实例 每次都需创建新对象，以保证线程安全
   *
   * @param clazz Groovy 类
   * @return Groovy 对象实例
   */
  private static GroovyObject getGroovyObjectInstance(Class<GroovyObject> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    }
    catch (Exception e) {
      String msg = extractErrorMsg(e);
      throw new BssException("实例化 Groovy 脚本失败: " + msg, e);
    }
  }

  /**
   * 编译 Groovy 脚本为 Groovy 类
   *
   * @param scriptText Groovy 脚本
   * @return Groovy 类
   */
  @SuppressWarnings({"PMD.PreserveStackTrace", "unchecked"})
  public static Class<GroovyObject> parseClass(String scriptText) {
    String key = cacheKey(scriptText);
    try {
      return classCache.get(key, () -> {
        // 不复用 GroovyClassLoader 实例，以避免被 GroovyClassLoader 加载的类无法被 GC
        try (GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), compilerConfiguration)) {
          return classLoader.parseClass(scriptText, "Script_" + key + ".groovy");
        }
      });
    }
    catch (ExecutionException | UncheckedExecutionException e) {
      String msg = extractErrorMsg(e.getCause());
      throw new BssException("编译 Groovy 脚本失败: " + msg, e.getCause());
    }
    catch (RuntimeException e) {
      String msg = extractErrorMsg(e);
      throw new BssException("编译 Groovy 脚本失败: " + msg, e);
    }
  }

  /**
   * 清空缓存
   */
  public static void clearCache() {
    classCache.invalidateAll();
  }

  /**
   * 获取缓存 key
   *
   * @param scriptText 脚本内容
   * @return 缓存 key
   */
  private static String cacheKey(String scriptText) {
    return DigestUtils.sha256Hex(scriptText);
  }

  /**
   * 从异常提取错误信息
   */
  private static String extractErrorMsg(Throwable throwable) {
    // 编译错误
    if (throwable instanceof MultipleCompilationErrorsException) {
      MultipleCompilationErrorsException exp = (MultipleCompilationErrorsException) throwable;
      Message error = exp.getErrorCollector().getError(0);
      if (error != null) {
        StringWriter stringWriter = new StringWriter();
        error.write(new PrintWriter(stringWriter));
        return compileErrorMsgPrefixPattern.matcher(stringWriter.toString()).replaceFirst("");
      }
    }
    return ExpUtil.getMsg(throwable);
  }

  /**
   * 提取错误信息，并从异常堆栈中找出报错的行号
   */
  @SuppressWarnings("java:S1643")
  private static String extractErrorMsgWithLineNum(Throwable throwable) {
    // 如果是执行 invoke 方法过程中抛出的异常，从 cause 中提取错误信息
    if (throwable instanceof InvokerInvocationException) {
      throwable = throwable.getCause();
    }
    String msg = ExpUtil.getMsg(throwable);
    // 从异常堆栈中找出报错的行号
    for (StackTraceElement element : throwable.getStackTrace()) {
      String fileName = element.getFileName();
      if (fileName != null && fileName.startsWith("Script") && fileName.endsWith(".groovy")) {
        msg = "行号: " + element.getLineNumber() + ", " + msg;
        break;
      }
    }

    return msg;
  }
}
