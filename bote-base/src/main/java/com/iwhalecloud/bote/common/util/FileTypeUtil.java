package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件类型判断工具类
 * <p>此工具根据文件的前几位bytes猜测文件类型，对于文本、zip判断不准确，对于视频、图片类型判断准确</p>
 * <p>需要注意的是，xlsx、docx等Office2007格式，全部识别为zip，因为新版采用了OpenXML格式，这些格式本质上是XML文件打包为zip</p>
 *
 * @author chen.linfa
 * @since 2024-08-19
 */
public final class FileTypeUtil {
  private FileTypeUtil() {

  }

  /**
   * 用于建立十六进制字符的输出的大写字符数组
   */
  private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  /** 文件类型映射，key 为 magic number, value 为文件类型列表。有些 magic number 可能对应多种文件类型，需要结合文件扩展名判断 */
  private static final Map<String, List<String>> FILE_TYPE_MAP;

  static {
    FILE_TYPE_MAP = new ConcurrentSkipListMap<>((s1, s2) -> {
      int len1 = s1.length();
      int len2 = s2.length();
      if (len1 == len2) {
        return s1.compareTo(s2);
      }
      else {
        return len2 - len1;
      }
    });

    // JPEG (jpg)
    FILE_TYPE_MAP.put("ffd8ff", List.of("jpg"));
    // PNG (png)
    FILE_TYPE_MAP.put("89504e47", List.of("png"));
    // GIF (gif)
    FILE_TYPE_MAP.put("4749463837", List.of("gif"));
    // GIF (gif)
    FILE_TYPE_MAP.put("4749463839", List.of("gif"));
    // TIFF (tif)
    FILE_TYPE_MAP.put("49492a00227105008037", List.of("tif"));
    // 16色位图(bmp)
    FILE_TYPE_MAP.put("424d228c010000000000", List.of("bmp"));
    // 24色位图(bmp)
    FILE_TYPE_MAP.put("424d8240090000000000", List.of("bmp"));
    // 256色位图(bmp)
    FILE_TYPE_MAP.put("424d8e1b030000000000", List.of("bmp"));
    // CAD (dwg)
    FILE_TYPE_MAP.put("41433130313500000000", List.of("dwg"));
    // Rich Text Format (rtf)
    FILE_TYPE_MAP.put("7b5c727466315c616e73", List.of("rtf"));
    // Photoshop (psd)
    FILE_TYPE_MAP.put("38425053000100000000", List.of("psd"));
    // Email [Outlook Express 6] (eml)
    FILE_TYPE_MAP.put("46726f6d3a203d3f6762", List.of("eml"));
    // MS Access (mdb)
    FILE_TYPE_MAP.put("5374616E64617264204A", List.of("mdb"));
    FILE_TYPE_MAP.put("252150532D41646F6265", List.of("ps"));
    // Adobe Acrobat (pdf)
    FILE_TYPE_MAP.put("255044462d312e", List.of("pdf"));
    // rmvb/rm相同
    FILE_TYPE_MAP.put("2e524d46000000120001", List.of("rmvb"));
    // flv与f4v相同
    FILE_TYPE_MAP.put("464c5601050000000900", List.of("flv"));
    FILE_TYPE_MAP.put("0000001C66747970", List.of("mp4"));
    FILE_TYPE_MAP.put("00000020667479706", List.of("mp4"));
    FILE_TYPE_MAP.put("00000018667479706D70", List.of("mp4"));
    FILE_TYPE_MAP.put("49443303000000002176", List.of("mp3"));
    FILE_TYPE_MAP.put("000001ba210001000180", List.of("mpg"));
    // wmv与asf相同
    FILE_TYPE_MAP.put("3026b2758e66cf11a6d9", List.of("wmv"));
    // Wave (wav)
    FILE_TYPE_MAP.put("52494646e27807005741", List.of("wav"));
    FILE_TYPE_MAP.put("52494646d07d60074156", List.of("avi"));
    // MIDI (mid)
    FILE_TYPE_MAP.put("4d546864000000060001", List.of("mid"));
    // WinRAR
    FILE_TYPE_MAP.put("526172211a0700cf9073", List.of("rar"));
    FILE_TYPE_MAP.put("235468697320636f6e66", List.of("ini"));
    // 有些文件类型的 magic number 相同
    FILE_TYPE_MAP.put("d0cf11e0a1b11ae1", List.of("doc", "xls", "ppt", "msi"));
    FILE_TYPE_MAP.put("504B0304", List.of("zip", "jar", "xlsx", "docx", "pptx", "odt", "ods", "odp", "apk", "epub", "ipa", "vsix", "xpi"));
    // 可执行文件
    FILE_TYPE_MAP.put("4d5a9000030000000400", List.of("exe"));
    // jsp文件
    FILE_TYPE_MAP.put("3c25402070616765206c", List.of("jsp"));
    // MF文件
    FILE_TYPE_MAP.put("4d616e69666573742d56", List.of("mf"));
    // java文件
    FILE_TYPE_MAP.put("7061636b616765207765", List.of("java"));
    // bat文件
    FILE_TYPE_MAP.put("406563686f206f66660d", List.of("bat"));
    // bat文件
    FILE_TYPE_MAP.put("4946204e4f5420444546", List.of("bat"));
    // gz文件
    FILE_TYPE_MAP.put("1f8b0800000000000000", List.of("gz"));
    // class文件
    FILE_TYPE_MAP.put("cafebabe0000002e0041", List.of("class"));
    // class文件
    FILE_TYPE_MAP.put("CAFEBABE0000003", List.of("class"));
    // chm文件
    FILE_TYPE_MAP.put("49545346030000006000", List.of("chm"));
    // mxp文件
    FILE_TYPE_MAP.put("04000000010000001300", List.of("mxp"));
    FILE_TYPE_MAP.put("6431303a637265617465", List.of("torrent"));
    // Quicktime (mov)
    FILE_TYPE_MAP.put("6D6F6F76", List.of("mov"));
    // WordPerfect (wpd)
    FILE_TYPE_MAP.put("FF575043", List.of("wpd"));
    // Outlook Express (dbx)
    FILE_TYPE_MAP.put("CFAD12FEC5FD746F", List.of("dbx"));
    // Outlook (pst)
    FILE_TYPE_MAP.put("2142444E", List.of("pst"));
    // Quicken (qdf)
    FILE_TYPE_MAP.put("AC9EBD8F", List.of("qdf"));
    // Windows Password (pwl)
    FILE_TYPE_MAP.put("E3828596", List.of("pwl"));
    // Real Audio (ram)
    FILE_TYPE_MAP.put("2E7261FD", List.of("ram"));
  }

  /**
   * 获取上传文件的文件类型
   *
   * @param file 文件
   */
  public static String getType(MultipartFile file) throws IOException {
    try (InputStream inputStream = file.getInputStream()) {
      String type = getType(inputStream, FilenameUtils.getExtension(file.getOriginalFilename()));
      Assert.notNull(type, "未知的文件类型");
      return type;
    }
  }

  /**
   * 根据文件流的头部信息获得文件类型
   *
   * @param in 输入流
   * @param fileSuffix 文件后缀类型
   * @return 类型，文件的扩展名，未找到为{@code null}
   */
  @Nullable
  public static String getType(InputStream in, @Nullable String fileSuffix) {
    try {
      String fileStreamHexHead = readHex28Upper(in);
      List<String> fileTypes = FILE_TYPE_MAP.entrySet().stream()
        .filter(e -> Strings.CI.startsWith(fileStreamHexHead, e.getKey()))
        .map(Entry::getValue)
        .findFirst()
        .orElse(List.of());
      // 无法根据 magic number 识别文件类型，返回后缀名
      if (fileTypes.isEmpty()) {
        return fileSuffix;
      }
      // 根据 magic number 只有一种文件类型，直接返回
      if (fileTypes.size() == 1) {
        return fileTypes.getFirst();
      }
      // 根据 magic number 有多种文件类型，优先使用文件后缀名
      return fileSuffix != null && fileTypes.contains(fileSuffix) ? fileSuffix : fileTypes.getFirst();
    }
    catch (IOException e) {
      throw BaseErrorConstant.FILE_GET_TYPE_ERROR.toException(e);
    }
  }

  /**
   * 根据文件流的头部信息获得文件类型
   *
   * @param fileStreamHexHead 文件流头部16进制字符串
   * @return 文件类型，未找到为<code>null</code>
   */
  @Nullable
  public static String getType(String fileStreamHexHead) {
    for (Entry<String, List<String>> fileTypeEntry : FILE_TYPE_MAP.entrySet()) {
      if (Strings.CI.startsWith(fileStreamHexHead, fileTypeEntry.getKey())) {
        return fileTypeEntry.getValue().getFirst();
      }
    }
    return null;
  }

  /**
   * 从流中读取前28个byte并转换为16进制，字母部分使用大写
   *
   * @param in {@link InputStream}
   * @return 16进制字符串
   * @throws IOException IO异常
   */
  private static String readHex28Upper(InputStream in) throws IOException {
    byte[] data = readBytes(in, 28);
    return new String(encodeHex(data));
  }

  /**
   * 将字节数组转换为十六进制字符数组
   *
   * @param data byte[]
   * @return 十六进制char[]
   */
  private static char[] encodeHex(byte[] data) {
    final int len = data.length;
    //len*2
    final char[] out = new char[len << 1];
    // two characters from the hex value.
    for (int i = 0, j = 0; i < len; i++) {
      // 高位
      out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
      // 低位
      out[j++] = DIGITS_UPPER[0x0F & data[i]];
    }
    return out;
  }

  /**
   * 读取指定长度的byte数组，不关闭流
   *
   * @param in {@link InputStream}，为null返回null
   * @param length 长度，小于等于0返回空byte数组
   * @return bytes
   * @throws IOException IO异常
   */
  public static byte[] readBytes(InputStream in, int length) throws IOException {
    if (length <= 0) {
      return new byte[0];
    }
    byte[] b = new byte[length];
    int readLength;
    readLength = in.read(b);
    if (readLength > 0 && readLength < length) {
      byte[] b2 = new byte[readLength];
      System.arraycopy(b, 0, b2, 0, readLength);
      return b2;
    }
    else {
      return b;
    }
  }

  /**
   * 根据文件后缀，判断是否图片
   *
   * @param fileType 文件后缀类型
   * @return 是否图片
   */
  public static boolean isPicture(@Nullable String fileType) {
    List<String> list = Arrays.asList("jpg", "png", "gif", "tif", "bmp", "svg");
    return StringUtils.isNotEmpty(fileType) && list.contains(fileType.toLowerCase());
  }

  /**
   * 根据文件后缀，判断是否文档
   *
   * @param fileType 文件后缀类型
   * @return 是否文档
   */
  public static boolean isDocument(String fileType) {
    List<String> list = Arrays.asList("jsp", "pst", "pdf", "wpd");
    return StringUtils.isNotEmpty(fileType) && list.contains(fileType.toLowerCase());
  }

  /**
   * 根据文件后缀，判断是否视频
   *
   * @param fileType 文件后缀类型
   * @return 是否视频
   */
  public static boolean isVideo(@Nullable String fileType) {
    List<String> list = Arrays.asList("mp3", "mp4", "wmv", "wav", "avi", "mid");
    return StringUtils.isNotEmpty(fileType) && list.contains(fileType.toLowerCase());
  }

  /**
   * 检查是否是表格
   *
   * @param fileType 文件后缀类型
   * @return 是否是表格
   */
  public static boolean isExcel(String fileType) {
    List<String> list = Arrays.asList("xls", "xlsx", "csv");
    return StringUtils.isNotEmpty(fileType) && list.contains(fileType.toLowerCase());
  }
}
