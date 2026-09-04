package com.iwhalecloud.bote.controller.publish;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.HttpCacheUtil;
import com.iwhalecloud.bote.common.util.IconUtil;
import com.iwhalecloud.bote.dto.publish.PublishGatewayDTO;
import com.iwhalecloud.bote.dto.publish.PublishGatewayQueryParams;
import com.iwhalecloud.bote.service.publish.IPublishGatewayManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 在线环境维护 Controller
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/publish/gateway", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "在线环境维护管理")
public class PublishGatewayManageController {

  private final IPublishGatewayManageService publishGatewayManageService;

  @Operation(summary = "查询单个网关")
  @GetMapping("findPublishGateway")
  public ResultVO<PublishGatewayDTO> findPublishGateway(
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "网关ID") @RequestParam("gatewayId") Long gatewayId) {
    Assert.notNull(gatewayId, "网关ID不能为空");
    return ResultVO.success(publishGatewayManageService.findPublishGateway(tenantId, gatewayId));
  }

  @Operation(summary = "保存网关（新增或更新）")
  @PostMapping("savePublishGateway")
  public ResultVO<PublishGatewayDTO> savePublishGateway(@RequestBody PublishGatewayDTO dto) {
    return publishGatewayManageService.savePublishGateway(dto);
  }

  @Operation(summary = "删除网关")
  @GetMapping("deletePublishGateway")
  public ResultVO<Void> deletePublishGateway(
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "网关ID") @RequestParam("gatewayId") Long gatewayId) {
    Assert.notNull(gatewayId, "网关ID不能为空");
    return publishGatewayManageService.deletePublishGateway(tenantId, gatewayId);
  }

  @Operation(summary = "分页查询网关")
  @PostMapping("queryPublishGatewayPage")
  public ResultVO<PageInfo<PublishGatewayDTO>> queryPublishGatewayPage(@RequestBody PublishGatewayQueryParams queryParams) {
    return ResultVO.success(publishGatewayManageService.queryPublishGatewayPage(queryParams));
  }

  @Operation(summary = "获取网关图标")
  @GetMapping("getGatewayIcon")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_publish_gateway WHERE gateway_id = #{param1}", cacheOnNotFound = true)
  @IgnoreSign
  @IgnoreSession
  public void getGatewayIcon(@Parameter(description = "网关ID", required = true) @RequestParam("gatewayId") Long gatewayId,
    @Parameter(description = "租户ID") @RequestParam(value = "tenantId", required = false) Long tenantId,
    HttpServletResponse response) throws IOException {
    Assert.notNull(gatewayId, "网关ID不能为空");
    PublishGatewayDTO gateway = publishGatewayManageService.findPublishGateway(tenantId, gatewayId);
    if (gateway == null || StringUtils.isEmpty(gateway.getGatewayIcon())) {
      HttpCacheUtil.sendError(response, "未配置图标");
      return;
    }
    IconUtil.sendBase64Icon(response, gateway.getGatewayIcon());
  }
}

