package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 抖音 aBogus签名算法
 */
@SuppressWarnings("unchecked")
public final class ABogusUtil {

  private ABogusUtil() {

  }

  /**
   * 使用RC4算法加密文本
   *
   * @param plaintext 明文
   * @param key 密钥
   * @return 加密后的字符串
   */
  private static String encryptWithRC4(String plaintext, String key) {
    int[] s = new int[256];
    for (int i = 0; i < 256; i++) {
      s[i] = i;
    }
    int j = 0;
    for (int i = 0; i < 256; i++) {
      j = (j + s[i] + key.charAt(i % key.length())) % 256;
      int temp = s[i];
      s[i] = s[j];
      s[j] = temp;
    }

    int i = 0;
    j = 0;
    StringBuilder cipher = new StringBuilder();
    for (int k = 0; k < plaintext.length(); k++) {
      i = (i + 1) % 256;
      j = (j + s[i]) % 256;
      int temp = s[i];
      s[i] = s[j];
      s[j] = temp;
      int t = (s[i] + s[j]) % 256;
      cipher.append((char) (s[t] ^ plaintext.charAt(k)));
    }
    return cipher.toString();
  }

  /**
   * 对整数进行循环左移操作
   *
   * @param value 要移位的值
   * @param shift 左移位数
   * @return 移位后的结果
   */
  private static int leftRotate(int value, int shift) {
    shift %= 32;
    return value << shift | value >>> (32 - shift);
  }

  /**
   * 获取SM3哈希算法中使用的常量
   *
   * @param index 索引值(0-63)
   * @return 对应的常量值
   */
  private static int getConstantDE(int index) {
    if (0 <= index && index < 16) {
      return 2043430169;
    }
    else if (16 <= index && index < 64) {
      return 2055708042;
    }
    else {
      return 0;
    }
  }

  /**
   * SM3算法中的P函数计算
   *
   * @param index 索引值
   * @param r 第一个参数
   * @param t 第二个参数
   * @param n 第三个参数
   * @return 计算结果
   */
  public static int calculatePE(int index, int r, int t, int n) {
    if (0 <= index && index < 16) {
      return r ^ t ^ n;
    }
    else if (16 <= index && index < 64) {
      return r & t | r & n | t & n;
    }
    else {
      return 0;
    }
  }

  /**
   * SM3算法中的H函数计算
   *
   * @param index 索引值
   * @param r 第一个参数
   * @param t 第二个参数
   * @param n 第三个参数
   * @return 计算结果
   */
  private static int calculateHE(int index, int r, int t, int n) {
    if (0 <= index && index < 16) {
      return r ^ t ^ n;
    }
    else if (16 <= index && index < 64) {
      return r & t | ~r & n;
    }
    else {
      return 0;
    }
  }

  private static String getEncodingTable(String encodingType) {
    String[] sObj = new String[] {
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
      "Dkdpgh4ZKsQB80/Mfvw36XI1R25+WUAlEi7NLboqYTOPuzmFjJnryx9HVGcaStCe=",
      "Dkdpgh4ZKsQB80/Mfvw36XI1R25-WUAlEi7NLboqYTOPuzmFjJnryx9HVGcaStCe=",
      "ckdp1h4ZKsUB80/Mfvw36XIgR25+WQAlEi7NLboqYTOPuzmFjJnryx9HVGDaStCe",
      "Dkdpgh2ZmsQB80/MfvV36XI1R45-WUAlEixNLwoqYTOPuzKFjJnry79HbGcaStCe"
    };

    switch (encodingType) {
      case "s0":
        return sObj[0];
      case "s1":
        return sObj[1];
      case "s2":
        return sObj[2];
      case "s3":
        return sObj[3];
      default:
        return sObj[4];
    }
  }

  /**
   * 使用自定义编码表进行Base64变种编码
   *
   * @param data 要编码的数据
   * @param encodingType 编码表类型(s0-s4)
   * @return 编码后的字符串
   */
  private static String encodeBase64Variant(String data, String encodingType) {
    if (StringUtils.isEmpty(encodingType)) {
      encodingType = "s4";
    }

    // Constants definition
    int[] constant = new int[] {16515072, 258048, 4032};

    // Get encoding table
    String strTable = getEncodingTable(encodingType);

    StringBuilder result = new StringBuilder();
    int lound = 0;
    int longInt = extract24BitInteger(lound, data);

    for (int i = 0; i < data.length() / 3.0 * 4; i++) {
      if (Math.floor(i / 4.0) != lound) {
        lound += 1;
        longInt = extract24BitInteger(lound, data);
      }

      int key = i % 4;
      int tempInt = processEncodingKey(key, longInt, constant, strTable);
      result.append((char) tempInt);
    }
    return result.toString();
  }

  private static int processEncodingKey(int key, int longInt, int[] constant, String strTable) {
    switch (key) {
      case 0:
        return strTable.charAt((longInt & constant[0]) >> 18);
      case 1:
        return strTable.charAt((longInt & constant[1]) >> 12);
      case 2:
        return strTable.charAt((longInt & constant[2]) >> 6);
      case 3:
        return strTable.charAt(longInt & 63);
      default:
        return 0;
    }
  }


  /**
   * 从字符串中提取24位整数
   *
   * @param round 轮次
   * @param data 源字符串
   * @return 24位整数值
   */
  private static int extract24BitInteger(int round, String data) {
    round = round * 3;
    int char1 = round < data.length() ? data.charAt(round) << 16 : 0;
    int char2 = round + 1 < data.length() ? data.charAt(round + 1) << 8 : 0;
    int char3 = round + 2 < data.length() ? data.charAt(round + 2) : 0;
    return char1 | char2 | char3;
  }

  /**
   * 生成随机数对
   *
   * @param random 随机种子
   * @param options 配置选项
   * @return 生成的随机数数组
   */
  private static int[] generateRandomPair(int random, int[] options) {
    return new int[] {(random & 255 & 170) | options[0] & 85, (random & 255 & 85) | options[0] & 170, ((random >> 8) & 255 & 170) | options[1] & 85,
      ((random >> 8) & 255 & 85) | options[1] & 170 };
  }

  /**
   * 生成用于签名的RC4加密数据
   *
   * @param urlSearchParams URL查询参数
   * @param userAgent 用户代理字符串
   * @param arguments 参数数组
   * @return 加密后的签名数据
   */
  private static String buildEncryptedSignatureData(String urlSearchParams, String userAgent, int[] arguments) {
    String suffix = "cus";
    SM3 sm3 = new SM3();
    long startTime = System.currentTimeMillis();

    // Double SM3 hash of url_search_params
    int[] urlSearchParamsList = (int[]) sm3.computeHash(sm3.computeHash(urlSearchParams + suffix));

    // Double SM3 hash of suffix
    int[] cus = (int[]) sm3.computeHash(sm3.computeHash(suffix));

    // Process user agent
    String uaKey = String.valueOf((char) 0) + (char) 1 + (char) arguments[2];
    String encryptedUa = encryptWithRC4(userAgent, uaKey);
    int[] ua = (int[]) sm3.computeHash(encodeBase64Variant(encryptedUa, "s3"));

    long endTime = System.currentTimeMillis();

    // Create b array
    int[] b = new int[76];

    b[8] = 3;
    b[10] = (int) endTime;
    b[16] = (int) startTime;
    b[18] = 44;
    b[19] = 1;
    b[20] = 0;
    b[21] = 1;
    b[22] = 5;

    // 3 encryption start time
    b[23] = (b[16] >> 24) & 255;
    b[24] = (b[16] >> 16) & 255;
    b[25] = (b[16] >> 8) & 255;
    b[26] = b[16] & 255;
    b[27] = (int) (b[16] / 256.0 / 256.0 / 256.0 / 256.0);
    b[28] = (int) (b[16] / 256.0 / 256.0 / 256.0 / 256.0 / 256.0);

    // Arguments
    b[29] = (arguments[0] >> 24) & 255;
    b[30] = (arguments[0] >> 16) & 255;
    b[31] = (arguments[0] >> 8) & 255;
    b[32] = arguments[0] & 255;

    b[33] = (arguments[1] / 256) & 255;
    b[34] = (arguments[1] % 256) & 255;
    b[35] = (arguments[1] >> 24) & 255;
    b[36] = (arguments[1] >> 16) & 255;

    b[37] = (arguments[2] >> 24) & 255;
    b[38] = (arguments[2] >> 16) & 255;
    b[39] = (arguments[2] >> 8) & 255;
    b[40] = arguments[2] & 255;

    // url_search_params_list
    b[41] = urlSearchParamsList[21];
    b[42] = urlSearchParamsList[22];

    // cus
    b[43] = cus[21];
    b[44] = cus[22];

    // ua
    b[45] = ua[23];
    b[46] = ua[24];

    // 3 encryption end time
    b[47] = (b[10] >> 24) & 255;
    b[48] = (b[10] >> 16) & 255;
    b[49] = (b[10] >> 8) & 255;
    b[50] = b[10] & 255;
    b[51] = b[8];
    b[52] = (int) (b[10] / 256.0 / 256.0 / 256.0 / 256.0);
    b[53] = (int) (b[10] / 256.0 / 256.0 / 256.0 / 256.0 / 256.0);

    // object configuration
    int pageId = 6241;
    int aid = 6383;

    b[54] = pageId;
    b[55] = 0;
    b[56] = 0;
    b[57] = (pageId >> 8) & 255;
    b[58] = pageId & 255;

    b[59] = aid;
    b[60] = aid & 255;
    b[61] = (aid >> 8) & 255;
    b[62] = 0;
    b[63] = 0;

    // Environment detection
    int[] windowEnvList = new int["1536|747|1536|834|0|30|0|0|1536|834|1536|864|1525|747|24|24|Win32".length()];
    for (int index = 0; index < "1536|747|1536|834|0|30|0|0|1536|834|1536|864|1525|747|24|24|Win32".length(); index++) {
      windowEnvList[index] = "1536|747|1536|834|0|30|0|0|1536|834|1536|864|1525|747|24|24|Win32".charAt(index);
    }

    b[67] = windowEnvList.length;
    b[68] = b[67] & 255;
    b[69] = 0;

    b[72] = 0;
    b[73] = 0;
    b[74] = 0;

    // XOR operation
    int xor1 = b[18] ^ b[23] ^ b[29] ^ b[33] ^ b[41] ^ b[43] ^ b[45];
    int xor2 = b[24] ^ b[30] ^ b[34] ^ b[38] ^ b[42] ^ b[44] ^ b[46];
    int xor3 = b[25] ^ b[31] ^ b[35] ^ b[39] ^ b[26] ^ b[32] ^ b[36];
    int xor4 = b[40] ^ b[47] ^ b[48] ^ b[49] ^ b[50] ^ b[51] ^ b[52];
    int xor5 = b[53] ^ b[27] ^ b[28] ^ b[55] ^ b[56] ^ b[57] ^ b[58];
    int xor6 = b[60] ^ b[61] ^ b[62] ^ b[63] ^ b[68] ^ b[69];
    int xor7 = 0;

    // 最终结果
    b[75] = xor1 ^ xor2 ^ xor3 ^ xor4 ^ xor5 ^ xor6 ^ xor7;

    // Build bb array
    int[] bb = {b[18], b[23], b[55], b[29], b[33], b[37], b[61], b[41], b[43], b[56], b[45], b[24], b[30], b[57], b[58], b[34], b[38], b[60], b[42],
      b[44], b[46], b[25], b[31], b[35], b[63], b[39], b[26], b[32], b[36], b[40], b[47], b[48], b[62], b[49], b[50], b[51], b[52], b[53], b[27],
      b[28], b[68], b[69], b[73], b[74]};

    // Merge arrays
    int[] finalBb = new int[bb.length + windowEnvList.length + 1];
    System.arraycopy(bb, 0, finalBb, 0, bb.length);
    System.arraycopy(windowEnvList, 0, finalBb, bb.length, windowEnvList.length);
    finalBb[finalBb.length - 1] = b[75];

    // Convert to string
    StringBuilder bbStr = new StringBuilder();
    for (int value : finalBb) {
      bbStr.append((char) value);
    }

    return encryptWithRC4(bbStr.toString(), String.valueOf((char) 121));
  }

  /**
   * 生成随机字符串前缀
   *
   * @return 随机字符串
   */
  private static String generateRandomPrefix() {
    StringBuilder randomStrList = new StringBuilder();

    // Generate random numbers and apply gener_random function
    int[] random1 = generateRandomPair(RandomUtils.insecure().randomInt(0, 10000), new int[] {3, 45});
    int[] random2 = generateRandomPair(RandomUtils.insecure().randomInt(0, 10000), new int[] {1, 0});
    int[] random3 = generateRandomPair(RandomUtils.insecure().randomInt(0, 10000), new int[] {1, 5});

    // Convert to string
    for (int value : random1) {
      randomStrList.append((char) value);
    }
    for (int value : random2) {
      randomStrList.append((char) value);
    }
    for (int value : random3) {
      randomStrList.append((char) value);
    }

    return randomStrList.toString();
  }

  /**
   * 生成抖音a_bogus签名
   *
   * @param urlSearchParams URL查询参数
   * @param userAgent 用户代理字符串
   * @param arguments 参数数组
   * @return 生成的签名
   */
  private static String generateSignature(String urlSearchParams, String userAgent, int[] arguments) {
    String resultStr = generateRandomPrefix() + buildEncryptedSignatureData(urlSearchParams, userAgent, arguments);

    return encodeBase64Variant(resultStr, "s4") + "=";
  }

  /**
   * 为详情请求生成a_bogus签名
   *
   * @param params 请求参数
   * @param userAgent 用户代理字符串
   * @return 生成的签名
   */
  public static String signDetail(String params, String userAgent) {
    return generateSignature(params, userAgent, new int[] {0, 1, 14});
  }

  /**
   * SM3哈希算法实现类
   */
  private static class SM3 {
    private final int[] reg;

    private List<Integer> chunk;

    private int size;

    public SM3() {
      this.reg = new int[8];
      this.chunk = new ArrayList<>();
      this.size = 0;
      this.reset();
    }

    /**
     * 重置SM3算法状态
     */
    private void reset() {
      this.reg[0] = 1937774191;
      this.reg[1] = 1226093241;
      this.reg[2] = 388252375;
      this.reg[3] = -628488704;
      this.reg[4] = -1452330820;
      this.reg[5] = 372324522;
      this.reg[6] = -477237683;
      this.reg[7] = -1325724082;
      this.chunk.clear();
      this.size = 0;
    }

    /**
     * 向哈希计算中添加数据
     *
     * @param data 要添加的数据，可以是字符串、整数列表或整数数组
     */
    private void appendData(Object data) {
      List<Integer> list;
      if (data instanceof String) {
        list = encodeString((String) data);
      }
      else if (data instanceof List) {
        list = (List<Integer>) data;
      }
      else if (data instanceof int[]) {
        int[] array = (int[]) data;
        list = new ArrayList<>(array.length);
        for (int value : array) {
          list.add(value);
        }
      }
      else {
        throw new BssException("不支持的类型: " + data.getClass().getName());
      }

      this.size += list.size();
      int f = 64 - this.chunk.size();
      if (list.size() < f) {
        this.chunk.addAll(list);
      }
      else {
        this.chunk.addAll(list.subList(0, f));
        while (this.chunk.size() >= 64) {
          this.compressChunk(this.chunk.subList(0, 64));
          if (f < list.size()) {
            this.chunk = new ArrayList<>(list.subList(f, Math.min(f + 64, list.size())));
          }
          else {
            this.chunk.clear();
          }
          f += 64;
        }
      }
    }

    /**
     * 将字符串编码为字节序列
     *
     * @param str 要编码的字符串
     * @return 编码后的字节列表
     */
    private List<Integer> encodeString(String str) {
      // Equivalent implementation of encodeURIComponent
      String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8).replace("+", "%20");

      List<Integer> result = new ArrayList<>();
      int i = 0;
      while (i < encoded.length()) {
        if (encoded.charAt(i) == '%' && i + 2 < encoded.length()) {
          // Process %XX sequences
          String hexStr = encoded.substring(i + 1, i + 3);
          int charCode = Integer.parseInt(hexStr, 16);
          result.add(charCode);
          i += 3;
        }
        else {
          // Process regular characters
          result.add((int) encoded.charAt(i));
          i++;
        }
      }
      return result;
    }

    /**
     * 计算数据的SM3哈希值
     *
     * @param data 输入数据
     * @return 哈希结果
     */
    private Object computeHash(Object data) {
      this.reset();
      this.appendData(data);
      this.padData();
      for (int f = 0; f < this.chunk.size(); f += 64) {
        this.compressChunk(this.chunk.subList(f, Math.min(f + 64, this.chunk.size())));
      }
      Object i = new int[32];
      for (int f = 0; f < 8; f++) {
        int c = this.reg[f];
        ((int[]) i)[4 * f + 3] = (255 & c);
        c >>>= 8;
        ((int[]) i)[4 * f + 2] = (255 & c);
        c >>>= 8;
        ((int[]) i)[4 * f + 1] = (255 & c);
        c >>>= 8;
        ((int[]) i)[4 * f] = (255 & c);
      }
      this.reset();
      return i;
    }

    /**
     * 压缩数据块
     *
     * @param chunk 64字节的数据块
     */
    private void compressChunk(List<Integer> chunk) {
      if (chunk.size() < 64) {
        return;
      }

      int[] f = new int[132];
      for (int i = 0; i < 16; i++) {
        f[i] = chunk.get(4 * i) << 24;
        f[i] |= chunk.get(4 * i + 1) << 16;
        f[i] |= chunk.get(4 * i + 2) << 8;
        f[i] |= chunk.get(4 * i + 3);
        f[i] >>>= 0;
      }

      for (int n = 16; n < 68; n++) {
        int a = f[n - 16] ^ f[n - 9] ^ leftRotate(f[n - 3], 15);
        a = a ^ leftRotate(a, 15) ^ leftRotate(a, 23);
        f[n] = (a ^ leftRotate(f[n - 13], 7) ^ f[n - 6]);
      }

      for (int n = 0; n < 64; n++) {
        f[n + 68] = (f[n] ^ f[n + 4]);
      }

      int[] i = Arrays.copyOf(this.reg, this.reg.length);

      for (int c = 0; c < 64; c++) {
        int o = leftRotate(i[0], 12) + i[4] + leftRotate(getConstantDE(c), c);
        o = leftRotate(o, 7);
        int s = o ^ leftRotate(i[0], 12);
        int u = calculatePE(c, i[0], i[1], i[2]);
        u = (u + i[3] + s + f[c + 68]);

        int b = calculateHE(c, i[4], i[5], i[6]);
        b = (b + i[7] + o + f[c]);

        i[3] = i[2];
        i[2] = leftRotate(i[1], 9);
        i[1] = i[0];
        i[0] = u;
        i[7] = i[6];
        i[6] = leftRotate(i[5], 19);
        i[5] = i[4];
        i[4] = (b ^ leftRotate(b, 9) ^ leftRotate(b, 17));
      }

      for (int l = 0; l < 8; l++) {
        this.reg[l] = (this.reg[l] ^ i[l]);
      }
    }

    /**
     * 对数据进行填充
     */
    private void padData() {
      long a = 8L * this.size;
      this.chunk.add(128);
      int f = this.chunk.size() % 64;

      if (64 - f < 8) {
        f -= 64;
      }

      while (f < 56) {
        this.chunk.add(0);
        f++;
      }

      for (int i = 0; i < 4; i++) {
        long c = a / 4294967296L;
        this.chunk.add((int) ((c >>> (8 * (3 - i))) & 255));
      }

      for (int i = 0; i < 4; i++) {
        this.chunk.add((int) ((a >>> (8 * (3 - i))) & 255));
      }
    }
  }
}
