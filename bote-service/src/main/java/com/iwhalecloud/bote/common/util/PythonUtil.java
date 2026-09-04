package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Suppliers;
import com.iwhalecloud.bote.config.properties.PythonProperties;
import com.iwhalecloud.bote.dto.base.PythonEnvInfo;
import com.iwhalecloud.bote.dto.base.PythonPackage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Python 工具类
 *
 * <p>用于执行 Python 3 脚本。</p>
 *
 * <p>Java 平台暂没有很好的方式集成 Python 3（GraalPython 尚不成熟），目前通过执行系统命令调用 Python 脚本。
 * 为了支持入参、出参，使用自定义的一段 Python 脚本封装对用户脚本的调用，由封装脚本解析入参、序列化出参，Java 和 Python 代码之间使用文件方式传递入参、出参。</p>
 *
 * @author bianjp
 * @since 2023-04-04
 */
@SuppressFBWarnings({"COMMAND_INJECTION", "CRLF_INJECTION_LOGS"})
@SuppressWarnings("java:S2142")
public final class PythonUtil {
  private static final Logger logger = LoggerFactory.getLogger(PythonUtil.class);

  private PythonUtil() {
  }

  /** 进程状态码: 脚本未定义 invoke 方法 */
  private static final int EXIT_CODE_NO_INVOKE_METHOD = 101;
  /** Python 配置 */
  private static final PythonProperties pythonProperties = SpringUtil.getBean(PythonProperties.class, PythonProperties::new);
  /** 封装脚本（用于处理脚本的入参、出参、错误信息） */
  private static final String wrapperScript = readWrapperScript(new ClassPathResource("python/wrapper.py"));
  /** PlayWright 封装脚本 */
  private static final String playwrightWrapperScript = readWrapperScript(new ClassPathResource("python/playwright_wrapper.py"));
  /** Python 环境信息缓存 */
  private static final Supplier<PythonEnvInfo> pythonEnvInfoCache = Suppliers.memoizeWithExpiration(PythonUtil::doCheckPythonEnv, 30, TimeUnit.MINUTES);


  /**
   * 读取封装脚本
   */
  private static String readWrapperScript(ClassPathResource resource) {
    try (InputStream inputStream = resource.getInputStream()) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new IllegalStateException("读取 Python 封装脚本失败", e);
    }
  }

  /**
   * 调用 Python 脚本中的 invoke 方法
   *
   * @param scriptText Python 脚本内容
   * @param pyPackageList Python 依赖包列表
   * @param arguments 方法参数
   * @return 返回值
   */
  @Nullable
  public static Object invoke(String scriptText, @Nullable List<String> pyPackageList, Object... arguments) {
    return invoke(scriptText, "temp.py", pyPackageList, null, arguments);
  }

  /**
   * 调用 Python 脚本中的方法
   *
   * <p>每次执行脚本时会创建一个临时目录作为脚本的工作目录，在此目录下会有以下文件：</p>
   *
   * <ol>
   *   <li>script.py: 用户脚本内容</li>
   *   <li>in.json: 入参文件，内容为入参列表的 JSON 字符串。没有入参时不生成</li>
   *   <li>out.json: 出参文件，内容为脚本返回值的 JSON 字符串，由 Python 封装脚本负责生成。没有出参时不生成</li>
   *   <li>error.txt: 错误信息文件，内容为错误信息字符串（用户友好），由 Python 封装脚本负责生成</li>
   * </ol>
   *
   * @param scriptText Python 脚本内容
   * @param scriptName 脚本名称（用于日志中区分脚本，方便判断是哪个工具箱、代码块节点）
   * @param pyPackageList Python 依赖包列表
   * @param stdoutWriter 标准输出记录器（用于调用方获取脚本的标准输出，包含标准错误输出）
   * @param arguments 方法参数
   * @return 返回值
   */
  @Nullable
  @SuppressWarnings("PMD.AvoidRethrowingException")
  public static Object invoke(String scriptText, String scriptName, @Nullable List<String> pyPackageList,
                              @Nullable StringWriter stdoutWriter, Object... arguments) {
    // 创建一个临时目录作为脚本的工作目录
    File tmpDir = null;
    try {
      tmpDir = Files.createTempDirectory("bote-python-").toFile();
      return doExecuteScript(tmpDir, scriptName, wrapperScript, scriptText, pyPackageList, stdoutWriter, arguments);
    }
    catch (BssException e) {
      throw e;
    }
    catch (InterruptedException e) {
      throw new BssException("Python 执行中断： " + StringUtils.defaultIfEmpty(e.getMessage(), ""), e);
    }
    catch (Exception e) {
      throw new BssException("Python 执行失败： " + ExpUtil.getMsg(e), e);
    }
    finally {
      FileUtils.deleteQuietly(tmpDir);
    }
  }

  /**
   * 调用 PlayWright 脚本
   *
   * @param tmpDir 临时目录
   * @param scriptText 脚本内容
   * @param stdoutWriter 标准输出记录器（用于调用方获取脚本的标准输出，包含标准错误输出）
   * @param params 脚本入参
   * @return 脚本出参
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public static Map<String, Object> invokePlaywrightScript(File tmpDir, String scriptText, StringWriter stdoutWriter, Map<String, Object> params) {
    try {
      String scriptName = "playwright-wrapper";
      Object result = doExecuteScript(tmpDir, scriptName, playwrightWrapperScript, scriptText, null, stdoutWriter, params);
      // wrapper 脚本中已经检查了出参是对象，这里直接强制转换
      return result == null ? null : (Map<String, Object>) result;
    }
    catch (BssException e) {
      throw e;
    }
    catch (InterruptedException e) {
      throw new BssException("PlayWright 脚本执行中断： " + StringUtils.defaultIfEmpty(e.getMessage(), ""), e);
    }
    catch (Exception e) {
      throw new BssException("PlayWright 脚本执行失败： " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 执行 Python 脚本
   */
  @Nullable
  private static Object doExecuteScript(File tmpDir, String scriptName, String wrapperScript, String scriptText, @Nullable List<String> pyPackageList,
                                        @Nullable StringWriter stdoutWriter, Object params) throws IOException, InterruptedException {
    // 将入参以 JSON 格式写入 in.json 文件，交给 Python 封装脚本解析。没有入参时不写文件以减少文件系统操作
    if (ObjectUtils.isNotEmpty(params)) {
      JsonUtil.write(new File(tmpDir, "in.json"), params);
    }

    // 将脚本内容写到 script.py 文件
    FileUtils.writeStringToFile(new File(tmpDir, "script.py"), scriptText, StandardCharsets.UTF_8);

    // 构造命令
    List<String> command;
    if (CollectionUtils.isNotEmpty(pyPackageList)) {
      // 将 wrapperScript 内容写入 main.py 文件
      File mainFile = new File(tmpDir, "main.py");
      FileUtils.writeStringToFile(mainFile, wrapperScript, StandardCharsets.UTF_8);
      // 构造 uv 执行命令
      command = buildUvCommand(pyPackageList, mainFile.getAbsolutePath());
    }
    else {
      // 使用系统 Python 环境执行脚本
      command = Arrays.asList(pythonProperties.getExecutable(), "-B", "-c", wrapperScript);
    }
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(tmpDir);
    processBuilder.redirectErrorStream(true);

    // 执行命令
    logger.debug("[{}] Start executing Python script. workDir={}", scriptName, tmpDir);
    Process process = processBuilder.start();

    // 读取标准输出
    readStdout(scriptName, process, stdoutWriter);

    // 阻塞等待进程结束
    waitProcessFinish(scriptName, process, tmpDir);

    // 读取返回值
    return readReturnValue(tmpDir);
  }

  /**
   * 构造 uv 执行命令
   */
  private static List<String> buildUvCommand(List<String> pyPackageList, String mainFilePath) {
    List<String> command = new ArrayList<>();
    // 1. 添加 uv 执行命令
    command.add(pythonProperties.getUvExecutable());
    command.add("run");
    // 2. 添加依赖包参数
    for (String packageName : CollectionUtils.emptyIfNull(pyPackageList)) {
      command.add("--with");
      command.add(packageName);
    }
    // 3. 添加执行脚本文件路径
    command.add(mainFilePath);
    return command;
  }

  /**
   * 读取进程的标准输出
   */
  private static void readStdout(String scriptName, Process process, @Nullable StringWriter stdoutWriter) {
// 逐行读取进程输出日志，添加到输出列表，并追加到日志文件
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logger.debug("[{}] {}", scriptName, line);
        if (stdoutWriter != null) {
          stdoutWriter.write(line);
          stdoutWriter.write("\n");
        }
      }
    }
    catch (Exception e) {
      logger.error("[{}] Failed to read process output", scriptName, e);
    }
  }

  /**
   * 等待进程执行结束
   */
  private static void waitProcessFinish(String scriptName, Process process, File tmpDir) throws InterruptedException, IOException {
    int exitCode = process.waitFor();
    // 执行成功
    if (exitCode == 0) {
      logger.debug("[{}] Python script execute success", scriptName);
      return;
    }

    // 从错误信息文件读取错误信息
    File errorFile = new File(tmpDir, "error.txt");
    String msg = errorFile.exists() ? FileUtils.readFileToString(errorFile, StandardCharsets.UTF_8) : "进程状态码为 " + exitCode;
    logger.debug("[{}] Python script execute failed. exitCode={}, error={}", scriptName, exitCode, msg);
    if (exitCode == EXIT_CODE_NO_INVOKE_METHOD) {
      throw new BssException("Python 脚本未定义 invoke 方法");
    }
    throw new BssException(msg);
  }

  /**
   * 读取脚本的返回值
   */
  @Nullable
  private static Object readReturnValue(File tmpDir) {
    File outFile = new File(tmpDir, "out.json");
    if (outFile.exists() && outFile.length() > 0) {
      return JsonUtil.parseJsonRequired(outFile, Object.class);
    }
    return null;
  }

  /**
   * 检查 Python 环境信息
   *
   * <p>缓存环境信息以避免重复执行系统命令的开销</p>
   *
   * @return Python 环境信息
   */
  public static PythonEnvInfo checkPythonEnv() {
    return pythonEnvInfoCache.get();
  }

  /**
   * 检查 Python 环境信息
   */
  private static PythonEnvInfo doCheckPythonEnv() {
    Assert.hasLength(pythonProperties.getExecutable(), "未配置 python 命令");
    Assert.hasLength(pythonProperties.getPipExecutable(), "未配置 pip 命令");
    String pythonVersion = checkPythonVersion();
    List<PythonPackage> packages = checkAvailablePythonPackages();
    return new PythonEnvInfo(pythonVersion, packages);
  }

  /**
   * 检查 Python 版本号
   */
  @SuppressWarnings("PMD.AvoidRethrowingException")
  private static String checkPythonVersion() {
    try {
      List<String> command = Arrays.asList(pythonProperties.getExecutable(), "--version");
      Process process = new ProcessBuilder(command).start();
      String stdout = StringUtils.trim(IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
      String stderr = StringUtils.trim(IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BssException("检查 Python 版本失败: " + StringUtils.firstNonEmpty(stderr, stdout, String.valueOf(exitCode)));
      }
      else if (!stdout.startsWith("Python 3.")) {
        throw new BssException("检查 Python 版本失败: " + StringUtils.defaultIfEmpty(stderr, stdout));
      }
      else {
        return stdout.split("\\s+")[1];
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      String msg = ExpUtil.getMsg(e);
      if (msg.startsWith("Cannot run program")) {
        throw new BssException("python 命令不存在: " + pythonProperties.getExecutable(), e);
      }
      else {
        throw new BssException("检查 Python 版本失败: " + msg, e);
      }
    }
  }

  /**
   * 检查可用的包列表
   */
  @SuppressWarnings("PMD.AvoidRethrowingException")
  private static List<PythonPackage> checkAvailablePythonPackages() {
    try {
      List<String> command = Arrays.asList(pythonProperties.getPipExecutable(), "list", "--format", "json");
      Process process = new ProcessBuilder(command).start();
      String stdout = StringUtils.trim(IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
      String stderr = StringUtils.trim(IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BssException("检查可用 Python 包列表失败: " + StringUtils.firstNonEmpty(stderr, stdout, String.valueOf(exitCode)));
      }
      else if (!stdout.startsWith("[")) {
        throw new BssException("检查可用 Python 包列表失败: " + StringUtils.defaultIfEmpty(stderr, stdout));
      }
      else {
        return ListUtils.emptyIfNull(JsonUtil.parseJson(stdout, new TypeReference<>() {
        }));
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      String msg = ExpUtil.getMsg(e);
      if (msg.startsWith("Cannot run program")) {
        throw new BssException("pip 命令不存在: " + pythonProperties.getPipExecutable(), e);
      }
      else {
        throw new BssException("检查可用 Python 包列表失败: " + msg, e);
      }
    }
  }

}
