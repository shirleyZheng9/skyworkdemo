package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 视频文件切分工具类
 *
 * @author chen.linfa
 * @since 2025-07-08
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class VideoSplitUtil {
  private VideoSplitUtil() {
  }

  private static final Logger logger = LoggerFactory.getLogger(VideoSplitUtil.class);

  public static final int DEFAULT_CHUNK_SIZE_MB = 3;
  /** FFmpeg 命令路径 */
  private static final String FFMPEG_CMD = SystemParameter.BOTE_FFMPEG_PATH.getValueFromEnv();
  /** 视频时长正则表达式 */
  private static final Pattern DURATION_PATTERN = Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
  /** 视频信息正则表达式 */
  private static final Pattern VIDEO_INFO_PATTERN = Pattern.compile("Stream.*Video.*(\\d+)x(\\d+).*([0-9.]+) fps");

  /**
   * 按照指定大小切分视频文件
   *
   * @param videoFile 视频文件
   * @param chunkSizeMB 每个分片的大小（MB）`
   * @return 切分结果
   */
  public static SplitResult splitVideoBySize(File videoFile, int chunkSizeMB) {
    // 如果文件小于分片大小，直接返回原文件
    if (videoFile.length() <= (long) chunkSizeMB * 1024 * 1024) {
      List<String> result = new ArrayList<>();
      result.add(videoFile.getAbsolutePath());
      return new SplitResult(null, result);
    }
    if (StringUtils.isEmpty(FFMPEG_CMD)) {
      throw new BssException("不支持处理超过3M的音频文件");
    }
    // 获取视频信息
    VideoInfo videoInfo = getVideoInfo(videoFile);
    long fileSize = videoFile.length();
    // 计算分片时长（基于文件大小比例）
    double chunkDuration = (double) chunkSizeMB * 1024 * 1024 / fileSize * videoInfo.getDuration();
    int totalChunks = (int) Math.ceil(videoInfo.getDuration() / chunkDuration);
    // 创建输出目录
    String outputDir;
    try {
      outputDir = Files.createTempDirectory("bote-video-").toString();
    }
    catch (Exception e) {
      throw new BssException("创建临时目录出现异常：" + e.getMessage(), e);
    }
    List<String> chunkFiles = new ArrayList<>();
    // 执行切分
    for (int i = 0; i < totalChunks; i++) {
      double startTime = i * chunkDuration;
      double endTime = Math.min((i + 1) * chunkDuration, videoInfo.getDuration());
      // 生成分片文件名
      String chunkFileName = generateChunkFileName(videoFile.getName(), i + 1, totalChunks);
      File chunkFile = new File(outputDir, chunkFileName);
      // 使用 FFmpeg 切分视频
      boolean success = splitVideoWithFFmpeg(videoFile, chunkFile, startTime, endTime);
      if (!success) {
        throw new BssException("FFmpeg 切分视频失败: " + chunkFileName);
      }
      chunkFiles.add(chunkFile.getAbsolutePath());
    }
    return new SplitResult(outputDir, chunkFiles);
  }

  /**
   * 获取视频信息
   */
  @SuppressFBWarnings({ "REC_CATCH_EXCEPTION", "COMMAND_INJECTION" })
  private static VideoInfo getVideoInfo(File videoFile) {
    try {
      ProcessBuilder pb = new ProcessBuilder(FFMPEG_CMD, "-i", videoFile.getAbsolutePath());
      pb.redirectErrorStream(true);
      Process process = pb.start();
      double duration = 0;
      int width = 0;
      int height = 0;
      double fps = 0;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          // 解析时长
          Matcher durationMatcher = DURATION_PATTERN.matcher(line);
          if (durationMatcher.find()) {
            int hours = Integer.parseInt(durationMatcher.group(1));
            int minutes = Integer.parseInt(durationMatcher.group(2));
            int seconds = Integer.parseInt(durationMatcher.group(3));
            int centiseconds = Integer.parseInt(durationMatcher.group(4));
            duration = hours * 3600 + minutes * 60 + seconds + centiseconds / 100.0;
          }
          // 解析视频信息
          Matcher videoMatcher = VIDEO_INFO_PATTERN.matcher(line);
          if (videoMatcher.find()) {
            width = Integer.parseInt(videoMatcher.group(1));
            height = Integer.parseInt(videoMatcher.group(2));
            fps = Double.parseDouble(videoMatcher.group(3));
          }
        }
      }
      process.waitFor();
      return new VideoInfo(duration, width, height, fps, videoFile.length());
    }
    catch (Exception e) {
      logger.error("Failed t0 get video info", e);
      throw new BssException("获取视频信息失败", e);
    }
  }

  /**
   * 使用 FFmpeg 切分视频
   */
  @SuppressFBWarnings("COMMAND_INJECTION")
  private static boolean splitVideoWithFFmpeg(File inputFile, File outputFile, double startTime, double endTime) {
    try {
      ProcessBuilder pb = new ProcessBuilder(FFMPEG_CMD, "-i", inputFile.getAbsolutePath(), "-ss", String.format("%.3f", startTime), "-t",
        String.format("%.3f", endTime - startTime), "-c", "copy", "-avoid_negative_ts", "make_zero", "-y", outputFile.getAbsolutePath());
      Process process = pb.start();
      int exitCode = process.waitFor();
      return exitCode == 0 && outputFile.exists() && outputFile.length() > 0;
    }
    catch (Exception e) {
      logger.error("FFmpeg 切分视频失败: {} -> {}", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), e);
      return false;
    }
  }

  /**
   * 生成分片文件名
   */
  private static String generateChunkFileName(String originalFileName, int chunkIndex, int totalChunks) {
    String baseName = FilenameUtils.getBaseName(originalFileName);
    String extension = FilenameUtils.getExtension(originalFileName);
    // 格式: 原文件名_part001_总片数.扩展名
    return String.format("%s_part%03d_%d.%s", baseName, chunkIndex, totalChunks, extension);
  }

  /**
   * 视频信息
   */
  @Getter
  @Setter
  @ToString
  public static class VideoInfo {
    private Double duration;
    private Integer width;
    private Integer height;
    private Double fps;
    private Long fileSize;

    public VideoInfo(Double duration, Integer width, Integer height, Double fps, Long fileSize) {
      this.duration = duration;
      this.width = width;
      this.height = height;
      this.fps = fps;
      this.fileSize = fileSize;
    }
  }

  /**
   * 视频切分结果
   */
  @Getter
  @Setter
  @ToString
  public static class SplitResult {
    @Nullable
    private String outputDir;
    private List<String> chunkFiles;

    public SplitResult(@Nullable String outputDir, List<String> chunkFiles) {
      this.outputDir = outputDir;
      this.chunkFiles = chunkFiles;
    }
  }
}
