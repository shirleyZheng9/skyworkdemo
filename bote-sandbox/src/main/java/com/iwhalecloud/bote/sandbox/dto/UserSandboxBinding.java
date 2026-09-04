package com.iwhalecloud.bote.sandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户与沙箱的绑定信息
 *
 * @author zhaoxu
 * @author bianjp
 * @since 2026-03-27
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserSandboxBinding {
  /** 沙箱 ID */
  private String sandboxId;
}
