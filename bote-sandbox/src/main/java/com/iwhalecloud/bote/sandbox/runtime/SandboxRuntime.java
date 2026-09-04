package com.iwhalecloud.bote.sandbox.runtime;

import com.iwhalecloud.bote.sandbox.api.SandboxOutputHandlers;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileDeleteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileSearchResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunRequest;
import com.iwhalecloud.bote.sandbox.dto.SandboxRunResult;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 沙箱运行时
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public interface SandboxRuntime {

  /**
   * 检查沙箱是否健康
   *
   * @return 是否健康
   */
  boolean isHealthy();

  /**
   * 续期沙箱过期时间
   *
   * @param duration 续期时长（从当前时刻起）
   */
  void renew(Duration duration);

  /**
   * 删除沙箱
   */
  void destroy();

  /**
   * 获取沙箱 ID
   */
  String getSandboxId();

  /**
   * 执行 shell 命令
   *
   * @param request 执行命令请求
   * @return 执行结果
   */
  SandboxRunResult execute(SandboxRunRequest request);

  /**
   * 执行 shell 命令，流式处理命令输出
   *
   * @param request 执行命令请求
   * @param handlers 输出处理器
   * @param startMs 调用方记录的开始时间戳
   */
  void executeWithHandlers(SandboxRunRequest request, SandboxOutputHandlers handlers, long startMs);

  /**
   * 读取文件内容，只支持文本文件
   *
   * @param path 文件路径
   * @param encoding 文件编码，默认 UTF-8
   * @return 读取结果
   */
  SandboxFileReadResult readFile(String path, @Nullable String encoding);

  /**
   * 流式读取文件内容，支持二进制文件、大文件
   *
   * <p>使用完毕后必须关闭返回的 InputStream</p>
   *
   * @param path 文件路径
   * @return 文件输入流
   */
  InputStream readFileStream(String path);

  /**
   * 写入文件
   *
   * @param path 文件路径
   * @param data 文件内容，支持字符串、InputStream
   * @param mode 文件权限模式，默认 0644
   * @return 写入结果
   */
  SandboxFileWriteResult writeFile(String path, Object data, @Nullable Integer mode);

  /**
   * 搜索文件
   *
   * @param path 文件路径
   * @param pattern 匹配模式
   * @return 搜索结果
   */
  SandboxFileSearchResult searchFiles(String path, @Nullable String pattern);

  /**
   * 删除文件
   *
   * @param paths 文件路径列表
   * @return 删除结果
   */
  SandboxFileDeleteResult deleteFiles(List<String> paths);
}
