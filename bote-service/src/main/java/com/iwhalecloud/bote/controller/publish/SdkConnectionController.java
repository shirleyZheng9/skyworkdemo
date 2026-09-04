package com.iwhalecloud.bote.controller.publish;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishChannelEnum;
import com.iwhalecloud.bote.dto.beyond.PublishChannelDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.service.publish.IPublishResourceService;
import com.iwhalecloud.bote.service.publish.SdkConnectionManager;
import com.iwhalecloud.bote.wechat.WechatAuthHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SDK连接管理控制器
 *
 * @author system
 * @since 2025-01-09
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "publish/sdk")
@RequiredArgsConstructor
@Tag(name = "SDK连接管理", description = "管理SDK长连接状态")
public class SdkConnectionController {
  private final SdkConnectionManager sdkConnectionManager;

  private final IPublishResourceService publishResourceService;

  private final WechatAuthHelper wechatAuthHelper;

  /**
   * 获取连接状态
   */
  @GetMapping("/status/{callbackCode}")
  @Operation(summary = "获取SDK连接状态")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public ResultVO<String> getConnectionStatus(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode) {
    ResourcePublishRecordDTO record = publishResourceService.getRecordByCallbackCode(callbackCode);
    if (PublishChannelEnum.WECHAT.getCode().equals(record.getPublishChannel())) {
      try {
        PublishChannelDTO publishParams = JsonUtil.parseJson(record.getPublishParams(), PublishChannelDTO.class);
        wechatAuthHelper.refreshToken(publishParams.getAppId(), publishParams.getSecret());
        return ResultVO.success("CONNECTED");
      }
      catch (BssException e) {
        return ResultVO.success("DISCONNECTED");
      }
    }
    else {
      // 非WECHAT渠道，使用原有逻辑
      String status = sdkConnectionManager.getConnectionStatus(callbackCode);
      return ResultVO.success(status);
    }
  }

  /**
   * 重启连接
   */
  @PostMapping("/restart/{callbackCode}")
  @Operation(summary = "重启SDK连接")
  public ResultVO<String> restartConnection(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode) {
    sdkConnectionManager.restartConnection(callbackCode);
    return ResultVO.success("连接重启成功");
  }

  /**
   * 停止连接
   */
  @PostMapping("/stop/{callbackCode}")
  @Operation(summary = "停止SDK连接")
  public ResultVO<String> stopConnection(
    @Parameter(description = "回调编码") @PathVariable("callbackCode") String callbackCode) {
    sdkConnectionManager.broadcastStopConnection(callbackCode);
    return ResultVO.success("连接停止成功");
  }

  /**
   * 获取所有连接状态
   */
  @GetMapping("/status/all")
  @Operation(summary = "获取所有SDK连接状态")
  public ResultVO<Map<String, String>> getAllConnectionStatus() {
    Map<String, String> statusMap = sdkConnectionManager.getAllConnectionStatus();
    return ResultVO.success(statusMap);
  }
}
