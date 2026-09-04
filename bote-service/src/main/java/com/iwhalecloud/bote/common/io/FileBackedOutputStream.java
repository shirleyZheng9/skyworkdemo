package com.iwhalecloud.bote.common.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

/**
 * 文件支撑的输出流
 *
 * <p>输出内容不超过阈值时使用内存以避免文件系统开销，超过时使用文件以避免占用过多内存</p>
 *
 * <p>参考 Guava 的 {@link com.google.common.io.FileBackedOutputStream}</p>
 *
 * <p>注意: 使用 inputStream 后必须正确关闭，或调用 {@link #release} 方法释放资源，否则可能导致临时文件残留</p>
 *
 * @author qian.sisheng
 * @since 2025-07-08
 */
@SuppressWarnings("UnstableApiUsage")
public final class FileBackedOutputStream extends OutputStream {
  /** 默认文件阈值(byte) */
  public static final int DEFAULT_FILE_THRESHOLD = 1024 * 1024 * 5;

  /** 文件阈值(byte) */
  private final int fileThreshold;
  /** 输出流 */
  private OutputStream out;
  /** 内存输出流 */
  private ByteArrayOutputStream memory;
  /** 文件 */
  private File file;

  /**
   * 构造输出流，使用默认文件阈值
   */
  public FileBackedOutputStream() {
    this(DEFAULT_FILE_THRESHOLD, -1);
  }

  /**
   * 构造输出流
   *
   * @param fileThreshold 文件阈值
   */
  public FileBackedOutputStream(int fileThreshold) {
    this(fileThreshold, -1);
  }

  /**
   * 构造输出流
   *
   * @param fileThreshold 文件阈值
   * @param estimateSize 预估大小。合理预估大小可以避免频繁扩容 ByteArrayOutputStream 的开销
   */
  public FileBackedOutputStream(int fileThreshold, int estimateSize) {
    this.fileThreshold = fileThreshold;
    // 预估大小大于文件阈值，直接使用文件
    if (estimateSize > fileThreshold) {
      try {
        this.file = createTempFile();
        this.out = Files.newOutputStream(this.file.toPath());
      }
      catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    // 指定 ByteArrayOutputStream 初始大小，避免频繁扩容
    else if (estimateSize > 0) {
      this.memory = new ByteArrayOutputStream(estimateSize);
      this.out = memory;
    }
    else {
      this.memory = new ByteArrayOutputStream();
      this.out = memory;
    }
  }

  /**
   * 获取输入流
   *
   * @return 输入流。使用后必须关闭
   */
  public InputStream inputStream() throws IOException {
    if (file != null) {
      return new BufferedFileInputStream(file);
    }
    else {
      return new ByteArrayInputStream(memory.toByteArray());
    }
  }

  /**
   * 获取内容大小
   *
   * @return 内容大小(byte)
   */
  public long size() {
    if (file != null) {
      return file.length();
    }
    return memory.size();
  }

  /**
   * 释放资源
   */
  public void release() {
    if (file != null) {
      FileUtils.deleteQuietly(file);
    }
  }

  @Override
  public synchronized void write(int b) throws IOException {
    update(1);
    out.write(b);
  }

  @Override
  public synchronized void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) throws IOException {
    update(len);
    out.write(b, off, len);
  }

  @Override
  public synchronized void close() throws IOException {
    out.close();
  }

  @Override
  public synchronized void flush() throws IOException {
    out.flush();
  }

  /**
   * Checks if writing {@code len} bytes would go over threshold, and switches to file buffering if
   * so.
   */
  @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
  @SuppressWarnings({"java:S2095", "PMD.CloseResource"})
  private void update(int len) throws IOException {
    if (file == null && (memory.size() + len > fileThreshold)) {
      File temp = createTempFile();
      OutputStream transfer = Files.newOutputStream(temp.toPath());
      transfer.write(memory.toByteArray());
      transfer.flush();

      // We've successfully transferred the data; switch to writing to file
      out = transfer;
      file = temp;
      memory = null;
    }
  }

  /**
   * 创建临时文件
   */
  private File createTempFile() throws IOException {
    File file = File.createTempFile("FileBackedOutputStream", null);
    // JVM 退出时删除文件，避免使用方未正确删除文件导致残留
    file.deleteOnExit();
    return file;
  }

  /**
   * 文件输入流，关闭时自动删除文件
   */
  private static final class BufferedFileInputStream extends BufferedInputStream {
    private final File file;

    private BufferedFileInputStream(File file) throws IOException {
      super(Files.newInputStream(file.toPath()));
      this.file = file;
    }

    @Override
    public void close() throws IOException {
      super.close();
      FileUtils.deleteQuietly(file);
    }
  }
}
