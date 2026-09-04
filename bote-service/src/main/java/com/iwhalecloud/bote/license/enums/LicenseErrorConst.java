package com.iwhalecloud.bote.license.enums;

import com.iwhalecloud.bss.litchi.base.error.IErrorConstant;
import lombok.Getter;

/**
 * license包错误码
 *
 * @author cheng.xu
 */
@Getter
public enum LicenseErrorConst implements IErrorConstant {

  // license相关
  LICENSE_CHECK_FAILED("000001", "license 校验不通过，请使用有效的 license！", "请在线更新有效的 license 以继续使用"),
  LICENSE_FILE_NOT_FOUND("000002", "license 文件不存在", "请添加 license 配置文件"),
  LICENSE_FILE_READ_FAILED("000003", "license 文件读取失败: %s"),
  LICENSE_FILE_EMPTY("000004", "license 文件为空", "请检查 license 配置文件"),
  LICENSE_INVALID("000010", "license 内容无效: %s", "请检查 license 配置文件内容是否有效"),
  LICENSE_EXTEND_INFO_PARSE_FAILED("000011", "license 扩展信息解析失败: %s", "请查看 license 扩展信息格式是否正确"),
  LICENSE_SIGNATURE_INVALID("000012", "license 签名错误", "请检查 license 配置文件,并联系运维管理人员"),
  LICENSE_SIGNATURE_FAILED("000013", "license 签名校验失败: %s"),
  PUBLIC_KEY_FILE_EMPTY("000020", "license 公钥文件为空", "请检查 license 公钥文件"),
  PUBLIC_KEY_FILE_READ_FAILED("000021", "license 公钥文件读取失败: %s"),
  PUBLIC_KEY_PARSE_FAILED("000022", "解析 license 公钥失败: %s", "请检查 license 公钥配置"),
  USER_NUM_EXCEEDED_LIMIT("000023", "用户数量超过限制。有效用户数：%s, license限制的用户数：%s"),
  // 占位不使用
  DO_NOT_USE("", "");

  /** 错误编码 */
  private final transient ErrorConstant errorConstant;

  LicenseErrorConst(String code, String message) {
    this(code, message, null);
  }

  LicenseErrorConst(String code, String message, String guide) {
    this.errorConstant = new ErrorConstant("0001" + code, message, guide);
  }

}
