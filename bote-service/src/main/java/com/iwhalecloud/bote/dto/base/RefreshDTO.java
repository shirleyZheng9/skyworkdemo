package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * 缓存刷新描述
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Getter
@Setter
@ToString
public class RefreshDTO {
  /** 缓存名称 */
  private String cacheName;
  /** 是否刷新所有 */
  private boolean refreshAll;
  /** 原始 keys */
  private String rawKeys;
  /** 解析后的 key 列表 */
  private List<String> keys;
  /** 解析后的 key 数组 */
  private String[] keysArr;

  /**
   * 按逗号分隔解析 keys 列表、数组，以方便使用
   */
  public void parseKeys() {
    if (!refreshAll) {
      if (StringUtils.isEmpty(rawKeys)) {
        throw BaseErrorConstant.CACHE_REFRESH_ERROR.toException("缓存 key 不能为空");
      }
      else {
        keysArr = rawKeys.trim().split("\\s*,\\s*");
        keys = Arrays.asList(keysArr);

        if (keysArr.length == 0) {
          throw BaseErrorConstant.CACHE_REFRESH_ERROR.toException("请输入要刷新的键");
        }
      }
    }
  }
}
