package com.iwhalecloud.bote.license.check;

import com.iwhalecloud.bote.license.enums.LicenseErrorConst;
import com.iwhalecloud.bss.litchi.base.error.IErrorConstant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import org.springframework.lang.Nullable;


/**
 * 检查结果对象
 *
 * @author cheng.xu
 */
@Getter
public final class CheckResult {
  /** 检查成功实例 */
  private static final CheckResult SUCCESS_INSTANCE = new CheckResult(true, null, null);

  /** 是否检查通过 */
  private final boolean success;
  /** 检查未通过时的错误编码 */
  private final String errorCode;
  /** 检查未通过时的错误信息 */
  private final String errorMsg;

  private CheckResult(boolean success, @Nullable String errorCode, @Nullable String errorMsg) {
    this.success = success;
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
  }

  @SuppressFBWarnings("SING_SINGLETON_GETTER_NOT_SYNCHRONIZED")
  public static CheckResult success() {
    return SUCCESS_INSTANCE;
  }

  public static CheckResult fail(IErrorConstant errorConstant, Object... args) {
    IErrorConstant.ErrorConstant constant = errorConstant.getErrorConstant();
    String errorCode = constant.getCode();
    String errorMsg = String.format(constant.getMessage(), args);
    return fail(errorCode, errorMsg);
  }

  public static CheckResult fail(String errorCode, String errorMsg) {
    return new CheckResult(false, errorCode, errorMsg);
  }

  public static CheckResult fail(String errorMsg) {
    return new CheckResult(false, LicenseErrorConst.LICENSE_CHECK_FAILED.getErrorConstant().getCode(), errorMsg);
  }
}
