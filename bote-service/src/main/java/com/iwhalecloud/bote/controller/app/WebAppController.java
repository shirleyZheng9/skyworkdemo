package com.iwhalecloud.bote.controller.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppQueryParams;
import com.iwhalecloud.bote.service.app.IWebAppService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 网页应用 Controller
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/webapp", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "运行态：网页应用")
public class WebAppController {

  private final IWebAppService webAppService;

  @PostMapping("saveWebApp")
  @Operation(summary = "保存网页应用")
  public ResultVO<WebAppDTO> saveWebApp(@RequestBody @Valid WebAppDTO webAppDTO) {
    return webAppService.saveWebApp(webAppDTO);
  }

  @PostMapping("queryWebAppPage")
  @Operation(summary = "分页查询网页应用")
  public ResultVO<PageInfo<WebAppDTO>> queryWebAppPage(@RequestBody WebAppQueryParams queryParams) {
    return ResultVO.success(webAppService.queryWebAppPage(queryParams));
  }

  @GetMapping("getWebAppList")
  @Operation(summary = "查询网页应用列表")
  public ResultVO<List<WebAppDTO>> getWebAppList(@RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(webAppService.queryWebAppList(spaceId));
  }

  @GetMapping("getWebAppDetail")
  @Operation(summary = "查询网页应用详情")
  public ResultVO<WebAppDTO> getWebAppDetail(@RequestParam("webAppId") Long webAppId, @RequestParam("spaceId") Long spaceId) {
    return ResultVO.success(webAppService.getWebApp(webAppId, spaceId));
  }

  @GetMapping("deleteWebApp")
  @Operation(summary = "删除网页应用")
  public ResultVO<Void> deleteWebApp(@RequestParam("webAppId") Long webAppId, @RequestParam("spaceId") Long spaceId) {
    return webAppService.deleteWebApp(webAppId, spaceId);
  }

  @IgnoreSign
  @IgnoreSession
  @Operation(summary = "获取网页应用图标")
  @GetMapping(value = "webAppIcon", produces = MediaType.ALL_VALUE)
  @RequestCacheable(sql = "SELECT updated_time FROM bt_web_app WHERE web_app_id = #{param2}", cacheOnNotFound = true)
  public void getWebAppIcon(@RequestParam("spaceId") Long spaceId, @RequestParam("webAppId") Long webAppId,
                            HttpServletResponse response) throws IOException {
    Assert.notNull(spaceId, "企业空间 ID 不能为空");
    Assert.notNull(webAppId, "网页应用 ID 不能为空");
    String icon = webAppService.getWebAppIcon(webAppId, spaceId);
    IconUtil.sendBase64Icon(response, icon);
  }

}
