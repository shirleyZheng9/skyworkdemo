package com.iwhalecloud.bote.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * 系统参数
 *
 * @author chen.linfa
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
public class SimpleDcParamDTO {
  /** 参数编码 */
  private String paramCode;
  /** 参数值 */
  @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
  private String paramVal;

  /** 解析后的值 */
  @JsonIgnore
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private volatile Object parsedValue;
  /** 是否已解析 */
  @JsonIgnore
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private volatile boolean parsed;

  /**
   * 获取解析后的值
   *
   * <p>注意: 解析后的值会缓存起来，只有一个，因此每个参数只能固定使用一种解析逻辑（比如，不能某处解析为 Long, 另一处又尝试解析为 Integer）</p>
   *
   * @param converter 转换器
   * @return 解析后的值
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T getParsedValue(Function<String, T> converter) {
    if (!parsed) {
      synchronized (this) {
        if (!parsed) {
          parsedValue = converter.apply(paramVal);
          parsed = true;
          // 将原始参数值置为空，以减少内存占用（图片的 base64 字符串比较占内存）
          paramVal = null;
        }
      }
    }
    return (T) parsedValue;
  }
}
