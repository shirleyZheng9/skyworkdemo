package com.iwhalecloud.bote.sandbox.session;

import com.iwhalecloud.bote.sandbox.dto.UserSandboxBinding;
import org.springframework.lang.Nullable;

/**
 * 用户沙箱绑定存储
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
public interface UserSandboxBindingRepository {

  /**
   * 获取用户绑定的沙箱信息
   *
   * @param userId 用户 ID
   * @return 沙箱信息
   */
  @Nullable
  UserSandboxBinding get(Long userId);

  /**
   * 存储用户绑定的沙箱信息
   *
   * @param userId 用户 ID
   * @param binding 沙箱信息
   */
  void put(Long userId, UserSandboxBinding binding);

  /**
   * 删除用户绑定的沙箱信息
   *
   * @param userId 用户 ID
   */
  void remove(Long userId);
}
