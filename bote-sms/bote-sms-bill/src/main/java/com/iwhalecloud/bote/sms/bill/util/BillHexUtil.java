package com.iwhalecloud.bote.sms.bill.util;


/**
 * 河南电信提供的 hex 编码工具类
 */
public final class BillHexUtil {

  private BillHexUtil() {
  }

  /**
   * 字节流转成十六进制表示
   */
  public static String bytes2HexString(byte[] src) {
    String strHex;
    StringBuilder sb = new StringBuilder();
    for (byte b : src) {
      strHex = Integer.toHexString(b & 0xFF);
      // 每个字节由两个字符表示，位数不够，高位补0
      sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
    }
    return sb.toString().trim();
  }

  /**
   * 16进制字符串转字节数组 @param src
   */
  public static byte[] hexString2Bytes(String src) {
    int m = 0;
    int n = 0;
    // 每两个字符描述一个字节
    int byteLen = src.length() / 2;
    byte[] ret = new byte[byteLen];
    for (int i = 0; i < byteLen; i++) {
      m = i * 2 + 1;
      n = m + 1;
      int intVal = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
      ret[i] = Byte.valueOf((byte) intVal);
    }
    return ret;
  }

  /**
   * 字符GBK串转16进制字符串
   * @param strPart 字符串
   * @return 16进制字符串 @throws
   */
  public static String string2HexGBK(String strPart) {
    return string2HexString(strPart, "GBK");
  }

  /**
   * 字符串转16进制字符串
   * @param strPart 字符串
   * @param tochartype hex目标编码 @return 16进制字符串 @throws
   */
  public static String string2HexString(String strPart, String tochartype) {
    try {
      return bytes2HexString(strPart.getBytes(tochartype));
    }
    catch (Exception e) {
      return "";
    }
  }

  /**
   * 16进制UTF-8字符串转字符串
   * @param src 16进制字符串
   * @return 字节数组 @throws
   */
  public static String hexUTF82String(String src) {
    return hexString2String(src, "UTF-8", "UTF-8");
  }

  /**
   * 16进制GBK字符串转字符串
   * @param src 16进制字符串
   * @return 字节数组 @throws
   */
  public static String hexGBK2String(String src) {
    return hexString2String(src, "GBK", "UTF-8");
  }


  /**
   * 16进制字符串转字符串
   * @param src 16进制字符串
   * @return 字节数组 @throws
   */
  public static String hexString2String(String src, String oldchartype, String chartype) {
    byte[] bts = hexString2Bytes(src);
    try {
      if (oldchartype.equals(chartype)) {
        return new String(bts, oldchartype);
      }
      else {
        return new String(new String(bts, oldchartype).getBytes(chartype), chartype);
      }
    }
    catch (Exception e) {
      return "";
    }
  }

}
