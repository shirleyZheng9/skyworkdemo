package com.iwhalecloud.bote.controller.publish;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogItem;
import com.iwhalecloud.bote.dto.beyond.BeyondCatalogTreeRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgAdminRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgDetailRequest;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgItem;
import com.iwhalecloud.bote.dto.beyond.BeyondOrgTreeRequest;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordQueryParams;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRequest;
import com.iwhalecloud.bote.dto.publish.ResourceUnpublishRequest;
import com.iwhalecloud.bote.service.publish.IPublishResourceService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发布资源控制器
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "publishResource", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "发布资源管理")
@RequiredArgsConstructor
public class PublishResourceController {
  private final IPublishResourceService publishResourceService;

  @PostMapping("/queryCatalogTree")
  @Operation(summary = "查询发布目录树")
  public ResultVO<List<BeyondCatalogItem>> queryCatalogTree(@RequestBody BeyondCatalogTreeRequest request) {
    Assert.notNull(request, "请求参数不可为空");
    Assert.hasLength(request.getCatalogType(), "目录类型不可为空");
    return publishResourceService.queryCatalogTree(request);
  }

  @PostMapping("/getOrgTree")
  @Operation(summary = "查询组织树")
  public ResultVO<List<BeyondOrgItem>> getOrgTree(@RequestBody BeyondOrgTreeRequest request) {
    Assert.notNull(request, "请求参数不可为空");
    return publishResourceService.getOrgTree(request);
  }

  @PostMapping("/getOrgAdmin")
  @Operation(summary = "查询组织管理员")
  public ResultVO<List<BeyondOrgAdminItem>> getOrgAdmin(@RequestBody BeyondOrgAdminRequest request) {
    Assert.notNull(request.getOrgId(), "组织标识不可为空");
    return publishResourceService.getOrgAdmin(request);
  }

  @PostMapping("/getOrgDetail")
  @Operation(summary = "查询组织路径")
  public ResultVO<BeyondOrgDetailItem> getOrgDetail(@RequestBody BeyondOrgDetailRequest request) {
    Assert.notNull(request, "请求参数不可为空");
    Assert.notNull(request.getOrgId(), "组织标识不可为空");
    return publishResourceService.getOrgDetail(request);
  }

  @PostMapping("/publish")
  @Operation(summary = "发布资源")
  public ResultVO<?> publishResource(@RequestBody ResourcePublishRequest request) {
    Assert.notNull(request.getTenantId(), "租户ID不可为空");
    Assert.notNull(request.getResourceType(), "资源类型不可为空");
    Assert.notEmpty(request.getResourceIdList(), "资源ID列表不可为空");
    Assert.notNull(request.getPublishChannel(), "发布渠道不可为空");
    Assert.notNull(request.getPublishChannelConfig(), "发布渠道配置不可为空");
    // 根据发布渠道进行分发
    return switch (request.getPublishChannel()) {
      case BEYOND -> publishResourceService.beyondPublishResource(request);
      case WECHAT -> publishResourceService.wechatPublishResource(request);
      case A2A -> publishResourceService.a2aPublishResource(request);
      case WEWORK -> publishResourceService.weWorkPublishResource(request);
      case DINGTALK -> publishResourceService.dingTalkPublishResource(request);
      case FEISHU -> publishResourceService.feishuPublishResource(request);
      default -> ResultVO.fail("不支持的发布渠道：" + request.getPublishChannel().getDesc());
    };
  }

  @PostMapping("unpublish")
  @Operation(summary = "取消发布资源")
  public ResultVO<Void> unpublish(@Valid @RequestBody ResourceUnpublishRequest request) {
    return publishResourceService.unpublishResource(request);
  }

  @GetMapping("queryPublishRecordsByResourceId")
  @Operation(summary = "查询指定资源的发布记录")
  public ResultVO<List<ResourcePublishRecordDTO>> queryPublishRecordsByResourceId(@NotNull @RequestParam("tenantId") Long tenantId,
                                                                                  @NotEmpty @RequestParam("resourceType") String resourceType,
                                                                                  @NotNull @RequestParam("resourceId") Long resourceId) {
    return ResultVO.success(publishResourceService.queryPublishRecordsByResourceId(tenantId, resourceType, resourceId));
  }

  @PostMapping("/queryResourcePublishRecordPage")
  @Operation(summary = "分页查询资源发布记录")
  public ResultVO<PageInfo<ResourcePublishRecordDTO>> queryResourcePublishRecordPage(@RequestBody ResourcePublishRecordQueryParams params) {
    Assert.notNull(params, "查询参数不可为空");
    return ResultVO.success(publishResourceService.queryResourcePublishRecordPage(params));
  }

  @GetMapping("/getRecordByResourceAndChannel")
  @Operation(summary = "根据资源和渠道查询发布记录")
  public ResultVO<ResourcePublishRecordDTO> getRecordByResourceAndChannel(@NotNull @RequestParam("tenantId") Long tenantId,
                                                                          @NotNull @RequestParam("resourceId") Long resourceId,
                                                                          @NotEmpty @RequestParam("publishChannel") String publishChannel) {
    return ResultVO.success(publishResourceService.getRecordByResourceAndChannel(tenantId, resourceId, publishChannel));
  }
}
