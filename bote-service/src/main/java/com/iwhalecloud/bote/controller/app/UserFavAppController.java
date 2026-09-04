package com.iwhalecloud.bote.controller.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.app.BatchSaveUserFavAppDTO;
import com.iwhalecloud.bote.dto.app.UserFavAppDTO;
import com.iwhalecloud.bote.dto.app.query.UserFavAppQueryParams;
import com.iwhalecloud.bote.service.app.IUserFavAppService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户常用应用 Controller
 *
 * @author wang.tingyun
 * @since 2025-09-12
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/userFavApp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "运行态：用户常用应用")
public class UserFavAppController {

  private final IUserFavAppService userFavAppService;

  @PostMapping("queryPage")
  @Operation(summary = "分页查询用户常用应用")
  public ResultVO<PageInfo<UserFavAppDTO>> queryUserFavAppPage(@RequestBody UserFavAppQueryParams params) {
    return ResultVO.success(userFavAppService.queryUserFavAppPage(params));
  }

  @GetMapping("queryList")
  @Operation(summary = "查询用户常用应用列表")
  public ResultVO<List<UserFavAppDTO>> queryUserFavAppList(@RequestParam Long spaceId) {
    return ResultVO.success(userFavAppService.queryUserFavAppList(spaceId));
  }

  @PostMapping("addFavApp")
  @Operation(summary = "添加用户常用应用")
  public ResultVO<Void> addUserFavApp(@RequestBody UserFavAppDTO favAppDTO) {
    return userFavAppService.addUserFavApp(favAppDTO);
  }

  @PostMapping("batchSave")
  @Operation(summary = "批量保存常用应用")
  public ResultVO<Void> batchSaveUserFavApp(@RequestBody BatchSaveUserFavAppDTO appDTO) {
    return userFavAppService.batchSaveUserFavApp(appDTO);
  }

  @GetMapping("remove")
  @Operation(summary = "移除某个常用的应用")
  public ResultVO<Void> removeUserFavApp(@RequestParam("appId") Long appId, @RequestParam("spaceId") Long spaceId) {
    return userFavAppService.removeUserFavApp(appId, spaceId);
  }

}