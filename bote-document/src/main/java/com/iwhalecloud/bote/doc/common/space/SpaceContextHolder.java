package com.iwhalecloud.bote.doc.common.space;


/**
 * 企业空间上下文 Holder
 */
public final class SpaceContextHolder {
  private SpaceContextHolder() {
  }

  /**
   * 当前企业空间编号
   */
  private static final ThreadLocal<Long> SPACE_ID = new ThreadLocal<>();

  /**
   * 是否忽略企业空间
   */
  private static final ThreadLocal<Boolean> IGNORE = new ThreadLocal<>();

  /**
   * 获得企业空间编号。
   *
   * @return 企业空间编号
   */
  public static Long getSpaceId() {
    return SPACE_ID.get();
  }

  public static void setSpaceId(Long spaceId) {
    SPACE_ID.set(spaceId);
  }

  /**
   * 获得企业空间编号。如果不存在，则抛出 NullPointerException 异常
   *
   * @return 企业空间编号
   */
  public static Long getRequiredSpaceId() {
    return getSpaceId();
  }

  /**
   * 当前是否忽略企业空间
   *
   * @return 是否忽略
   */
  public static boolean isIgnore() {
    return Boolean.TRUE.equals(IGNORE.get());
  }

  public static void setIgnore(Boolean ignore) {
    IGNORE.set(ignore);
  }

  public static void clear() {
    SPACE_ID.remove();
    IGNORE.remove();
  }

}
