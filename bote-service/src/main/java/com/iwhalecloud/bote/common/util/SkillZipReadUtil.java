package com.iwhalecloud.bote.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.springframework.lang.Nullable;

/**
 * 技能相关 zip 遍历：统一 {@link ZipInputStream}（UTF-8）的条目循环与 {@code closeEntry} 用法。
 */
public final class SkillZipReadUtil {

  private SkillZipReadUtil() {
  }

  /**
   * 按 UTF-8 打开 zip 并依次处理每个条目；每个条目处理完后在 {@code finally} 中调用 {@link ZipInputStream#closeEntry()}。
   * <p>
   * {@code zipBytes} 为 null 或长度过小时直接返回，不调用 handler。
   */
  public static void walkUtf8(byte[] zipBytes, SkillZipEntryHandler handler) throws IOException {
    if (zipBytes.length < 2) {
      return;
    }
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
      for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
        try {
          handler.accept(e, zis);
        }
        finally {
          zis.closeEntry();
        }
      }
    }
  }

  /**
   * 与 {@link #walkUtf8(byte[], SkillZipEntryHandler)} 相同契约；会关闭 {@code zipStream}（通过 {@link ZipInputStream} 关闭底层流）。
   */
  public static void walkUtf8(InputStream zipStream, SkillZipEntryHandler handler) throws IOException {
    if (zipStream == null) {
      return;
    }
    try (ZipInputStream zis = new ZipInputStream(zipStream, StandardCharsets.UTF_8)) {
      for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
        try {
          handler.accept(e, zis);
        }
        finally {
          zis.closeEntry();
        }
      }
    }
  }

  /**
   * 使用 {@link ZipFile} 按条目打开输入流并回调；适合已落盘的大 zip，避免整包读入 {@code byte[]}。
   */
  public static void walkUtf8ZipFile(Path zipPath, SkillZipFileEntryHandler handler) throws IOException {
    Objects.requireNonNull(zipPath, "zipPath");
    Objects.requireNonNull(handler, "handler");
    try (ZipFile zf = new ZipFile(zipPath.toFile(), StandardCharsets.UTF_8)) {
      Enumeration<? extends ZipEntry> entries = zf.entries();
      while (entries.hasMoreElements()) {
        ZipEntry e = entries.nextElement();
        if (e.isDirectory()) {
          continue;
        }
        try (InputStream in = zf.getInputStream(e)) {
          handler.accept(e, in);
        }
      }
    }
  }

  /**
   * 路径任一段为 {@code ..} 时视为 zip 路径穿越风险。
   */
  public static boolean entryPathHasDotDotSegment(@Nullable String entryName) {
    if (entryName == null || entryName.isEmpty()) {
      return true;
    }
    String n = entryName.replace('\\', '/');
    for (String seg : n.split("/")) {
      if ("..".equals(seg)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 将条目流复制到 {@code out}，超过 {@code maxBytes} 时抛出 {@link IOException}。
   *
   * @return 实际复制的字节数
   */
  public static long copyStreamLimited(InputStream in, OutputStream out, long maxBytes) throws IOException {
    byte[] buf = new byte[8192];
    long total = 0;
    int n;
    while ((n = in.read(buf)) >= 0) {
      total += n;
      if (total > maxBytes) {
        throw new IOException("ZIP 条目超过允许大小（上限 " + maxBytes + " 字节）");
      }
      out.write(buf, 0, n);
    }
    return total;
  }

  /**
   * 读取条目到内存，带大小上限（用于文本类条目）。
   */
  public static byte[] readEntryBytesLimited(InputStream in, long maxBytes) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      copyStreamLimited(in, bos, maxBytes);
      return bos.toByteArray();
    }
  }

  public static byte[] readNonEmptyEntryBytes(ZipInputStream zis) throws IOException {
    byte[] content = zis.readAllBytes();
    return content.length > 0 ? content : new byte[]{};
  }

  public static String utf8StringOrNull(byte[] content) {
    return new String(content, StandardCharsets.UTF_8);
  }

  /**
   * 条目路径的最后一段文件名；无 {@code '/'} 时返回原串。
   */
  @Nullable
  public static String basename(@Nullable String entryPath) {
    if (entryPath == null) {
      return null;
    }
    int i = entryPath.lastIndexOf('/');
    return i >= 0 ? entryPath.substring(i + 1) : entryPath;
  }

  @FunctionalInterface
  public interface SkillZipEntryHandler {
    void accept(ZipEntry entry, ZipInputStream zis) throws IOException;
  }

  @FunctionalInterface
  public interface SkillZipFileEntryHandler {
    void accept(ZipEntry entry, InputStream entryIn) throws IOException;
  }
}
