package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserRemoveRequest;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserSyncResponse;
import com.iwhalecloud.bote.dto.portal.tiangong.TiangongProjectUserUpsertRequest;
import com.iwhalecloud.bote.service.portal.tiangong.ITiangongProjectUserSyncService;
import com.iwhalecloud.bote.service.portal.tiangong.TiangongProjectSyncException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天工项目成员内部同步接口
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 仅供天工 MQ 同步账号调用；不加 IgnoreSession 或 IgnoreSign，继续使用 AADP 的会话和签名保护。
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "internal/tiangong/project-users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "内部：天工项目成员同步")
public class TiangongProjectUserSyncController {
  private final ITiangongProjectUserSyncService syncService;

  /**
   * 新增成员并按需绑定创建人；业务错误以稳定的 TG_* resultCode 返回给 MQ 消费端。
   */
  @Operation(summary = "新增天工项目成员并按需绑定创建人")
  @PostMapping("upsert")
  public ResultVO<TiangongProjectUserSyncResponse> upsert(@RequestBody TiangongProjectUserUpsertRequest request) {
    try {
      return ResultVO.success(syncService.upsert(request));
    }
    catch (TiangongProjectSyncException e) {
      return new ResultVO<>(e.getCode(), e.getMessage());
    }
  }

  /**
   * 删除指定天工成员；与新增接口使用相同的业务错误码封装方式。
   */
  @Operation(summary = "移除天工项目成员")
  @PostMapping("remove")
  public ResultVO<TiangongProjectUserSyncResponse> remove(@RequestBody TiangongProjectUserRemoveRequest request) {
    try {
      return ResultVO.success(syncService.remove(request));
    }
    catch (TiangongProjectSyncException e) {
      return new ResultVO<>(e.getCode(), e.getMessage());
    }
  }
}
