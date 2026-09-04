package com.iwhalecloud.bote.common.task;

import com.iwhalecloud.bote.config.properties.PythonProperties;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UV 缓存管理定时任务
 *
 * <p>每天凌晨检查 uv 缓存目录大小，如果超出限制就执行清理操作。</p>
 *
 * @author wangtingyun
 * @since 2026-03-14
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "bote.python.uvCache.job.enabled", matchIfMissing = true)
public class UvCacheCleanTask {

  private static final Logger logger = LoggerFactory.getLogger(UvCacheCleanTask.class);

  private final PythonProperties pythonProperties;

  /**
   * 定时检查并清理 UV 缓存
   * 默认每天凌晨 2 点执行（可通过配置项修改）
   */
  @Scheduled(cron = "${bote.python.uvCache.cron:0 0 2 * * ?}")
  public void checkAndCleanCache() {
    logger.info("开始执行 UV 缓存检查任务...");
    
    try {
      // 获取当前缓存大小
      long currentSize = getCacheSize();
      long sizeLimit = pythonProperties.getUvCacheSize();
      if (currentSize <= sizeLimit) {
        return;
      }

      // 执行 uv cache prune 清理未使用的缓存
      logger.debug("UV 缓存大小超出限制，开始执行清理操作...");
      cleanUnusedCache();
      
      // 等待片刻后重新检查
      Thread.sleep(1000);
      long sizeAfterPrune = getCacheSize();

      // 如果清理效果不理想，执行彻底清空
      if (sizeAfterPrune > sizeLimit) {
        logger.debug("UV 缓存清理效果不理想，执行彻底清空...");
        cleanAllCache();
      }
      logger.info("UV 缓存检查任务执行完成!");
    }
    catch (Exception e) {
      logger.error("UV 缓存检查任务执行失败", e);
    }
  }

  /**
   * 获取 UV 缓存大小（单位：Byte）
   *
   * @return 缓存大小（Byte）
   */
  private long getCacheSize() {
    // 构建命令
    List<String> command = Arrays.asList(pythonProperties.getUvExecutable(), "cache", "size", "--preview-features", "cache-size");
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    try {
      // 执行命令
      if (logger.isDebugEnabled()) {
        logger.debug("执行查询uv缓存大小命令：{}", String.join(" ", command));
      }
      Process process = processBuilder.start();
      // 读取命令输出结果
      String output;
      try (var inputStream = process.getInputStream()) {
        output = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
      // 等待命令执行完成
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BssException("执行 uv cache size 命令失败，执行结果内容：" + output + "，退出码：" + exitCode);
      }
      // 返回cache大小: 单位为 Byte
      return Long.parseLong(output.trim());
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("获取 UV 缓存大小失败：" + e.getMessage(), e);
    }
  }

  /**
   * 清理未使用的缓存（prune 操作）
   */
  private void cleanUnusedCache() {
    List<String> command = Arrays.asList(pythonProperties.getUvExecutable(), "cache", "prune");
    executeUvCommand(command);
  }

  /**
   * 清空所有缓存（clean 操作）
   */
  private void cleanAllCache() {
    List<String> command = Arrays.asList(pythonProperties.getUvExecutable(), "cache", "clean");
    executeUvCommand(command);
  }

  /**
   * 执行 UV 命令
   *
   * @param command 命令列表
   */
  private void executeUvCommand(List<String> command) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    
    try {
      if (logger.isDebugEnabled()) {
        logger.debug("执行 UV 命令：{}", String.join(" ", command));
      }
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BssException("执行 UV 命令: " + String.join(" ", command) + " 失败，退出码：" + exitCode);
      }
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException(e.getMessage(), e);
    }
  }
}
