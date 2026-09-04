package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bote.dto.plugin.lark.LarkUserAuthInfoDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 飞书授权服务
 *
 * @author qian.sisheng
 * @since 2025-08-21
 */
public interface ILarkAuthService {

  /**
   * 飞书授权回调
   *
   * @param code 授权码
   * @param state 状态参数
   * @return 响应结果
   */
  ResultVO<Void> callback(String code, String state);

  /**
   * 获取飞书用户信息
   *
   * @return 用户信息
   */
  LarkUserAuthInfoDTO getUserAuthInfo();

  /**
   * 取消飞书授权
   */
  void cancelLarkAuth();
}
