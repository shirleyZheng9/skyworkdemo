package com.iwhalecloud.bote.common.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * {@link LockHelper} 单元测试
 *
 * <p>覆盖所有静态工具方法：formatBizLockKey、formatTenantLockKey、sanitizeLockKey、
 * buildLockPath、validateLockKey、buildCacheKeyPrefix 的正常与异常分支。
 * 纯静态方法，无需 Mock。</p>
 */
class LockHelperTest {

  // ==================== formatBizLockKey ====================

  @Test
  void formatBizLockKey_validInput_returnsFormattedKey() {
    assertThat(LockHelper.formatBizLockKey("order", "123")).isEqualTo("order:123");
  }

  @Test
  void formatBizLockKey_trimsInput_returnsTrimmedKey() {
    assertThat(LockHelper.formatBizLockKey("  order  ", "  123  ")).isEqualTo("order:123");
  }

  @Test
  void formatBizLockKey_nullBizType_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatBizLockKey(null, "123"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("业务类型不能为空");
  }

  @Test
  void formatBizLockKey_emptyBizType_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatBizLockKey("  ", "123"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("业务类型不能为空");
  }

  @Test
  void formatBizLockKey_nullBizId_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatBizLockKey("order", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("业务ID不能为空");
  }

  @Test
  void formatBizLockKey_emptyBizId_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatBizLockKey("order", "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("业务ID不能为空");
  }

  // ==================== formatTenantLockKey ====================

  @Test
  void formatTenantLockKey_validInput_returnsFormattedKey() {
    assertThat(LockHelper.formatTenantLockKey(100L, "my-lock")).isEqualTo("tenant:100:my-lock");
  }

  @Test
  void formatTenantLockKey_trimsLockKey_returnsTrimmedKey() {
    assertThat(LockHelper.formatTenantLockKey(1L, "  op  ")).isEqualTo("tenant:1:op");
  }

  @Test
  void formatTenantLockKey_nullTenantId_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatTenantLockKey(null, "lock"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("租户ID不能为空");
  }

  @Test
  void formatTenantLockKey_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatTenantLockKey(1L, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  @Test
  void formatTenantLockKey_emptyLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.formatTenantLockKey(1L, "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  // ==================== sanitizeLockKey ====================

  @Test
  void sanitizeLockKey_nullInput_returnsNull() {
    assertThat(LockHelper.sanitizeLockKey(null)).isNull();
  }

  @Test
  void sanitizeLockKey_noIllegalChars_returnsSame() {
    assertThat(LockHelper.sanitizeLockKey("abc_123:order-1.0")).isEqualTo("abc_123:order-1.0");
  }

  @Test
  void sanitizeLockKey_withSpaces_replacesWithUnderscore() {
    assertThat(LockHelper.sanitizeLockKey("my key")).isEqualTo("my_key");
  }

  @Test
  void sanitizeLockKey_withSpecialChars_replacesAll() {
    // @#$%^&*() 共 9 个非法字符，每个替换为下划线
    assertThat(LockHelper.sanitizeLockKey("key@#$%^&*()")).isEqualTo("key_________");
  }

  // ==================== buildLockPath ====================

  @Test
  void buildLockPath_baseWithoutSlash_appendsSlash() {
    assertThat(LockHelper.buildLockPath("/locks", "my-key")).isEqualTo("/locks/my-key");
  }

  @Test
  void buildLockPath_baseWithSlash_doesNotDuplicate() {
    assertThat(LockHelper.buildLockPath("/locks/", "my-key")).isEqualTo("/locks/my-key");
  }

  @Test
  void buildLockPath_sanitizesLockKey() {
    assertThat(LockHelper.buildLockPath("/locks", "my key!")).isEqualTo("/locks/my_key_");
  }

  @Test
  void buildLockPath_nullBasePath_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.buildLockPath(null, "key"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("基础路径不能为空");
  }

  @Test
  void buildLockPath_emptyBasePath_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.buildLockPath("  ", "key"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("基础路径不能为空");
  }

  @Test
  void buildLockPath_nullLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.buildLockPath("/locks", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  @Test
  void buildLockPath_emptyLockKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.buildLockPath("/locks", "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  // ==================== validateLockKey ====================

  @Test
  void validateLockKey_validKey_doesNotThrow() {
    LockHelper.validateLockKey("valid-key:123");
  }

  @Test
  void validateLockKey_nullKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.validateLockKey(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  @Test
  void validateLockKey_emptyKey_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.validateLockKey("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键不能为空");
  }

  @Test
  void validateLockKey_tooLong_throwsIllegalArgument() {
    String longKey = "a".repeat(251);
    assertThatThrownBy(() -> LockHelper.validateLockKey(longKey))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("锁键长度不能超过250");
  }

  @Test
  void validateLockKey_containsNewline_throwsIllegalArgument() {
    // 控制字符位于中间，避免被 trim() 去除
    assertThatThrownBy(() -> LockHelper.validateLockKey("ke\ny"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("控制字符");
  }

  @Test
  void validateLockKey_containsCarriageReturn_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.validateLockKey("ke\ry"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("控制字符");
  }

  @Test
  void validateLockKey_containsTab_throwsIllegalArgument() {
    assertThatThrownBy(() -> LockHelper.validateLockKey("ke\ty"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("控制字符");
  }

  @Test
  void validateLockKey_exactly250Chars_doesNotThrow() {
    LockHelper.validateLockKey("a".repeat(250));
  }

  // ==================== buildCacheKeyPrefix ====================

  @Test
  void buildCacheKeyPrefix_bothNull_returnsEmpty() {
    assertThat(LockHelper.buildCacheKeyPrefix(null, null)).isEmpty();
  }

  @Test
  void buildCacheKeyPrefix_onlyNamespace_returnsNamespaceWithColon() {
    assertThat(LockHelper.buildCacheKeyPrefix("ns", null)).isEqualTo("ns:");
  }

  @Test
  void buildCacheKeyPrefix_namespaceWithTrailingColon_doesNotDuplicate() {
    assertThat(LockHelper.buildCacheKeyPrefix("ns:", null)).isEqualTo("ns:");
  }

  @Test
  void buildCacheKeyPrefix_onlyPrefix_returnsPrefixWithColon() {
    assertThat(LockHelper.buildCacheKeyPrefix(null, "pref")).isEqualTo("pref:");
  }

  @Test
  void buildCacheKeyPrefix_prefixWithTrailingColon_doesNotDuplicate() {
    assertThat(LockHelper.buildCacheKeyPrefix(null, "pref:")).isEqualTo("pref:");
  }

  @Test
  void buildCacheKeyPrefix_bothPresent_returnsCombined() {
    assertThat(LockHelper.buildCacheKeyPrefix("ns", "pref")).isEqualTo("ns:pref:");
  }

  @Test
  void buildCacheKeyPrefix_emptyStrings_returnsEmpty() {
    assertThat(LockHelper.buildCacheKeyPrefix("  ", "  ")).isEmpty();
  }
}
