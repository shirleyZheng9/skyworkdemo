package com.iwhalecloud.bote.doc.module.person.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.consts.FavoriteConsts;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteListDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteOperationResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoritePinnedDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.PinFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.RemoveFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.UnpinFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.service.IFavoriteService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 收藏模块接口
 *
 * @author lizuyin
 * @since 2025-08-21
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/favorite", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：收藏管理")
public class FavoriteController {
  private final IFavoriteService favoriteService;

  /**
   * 获取置顶收藏列表
   *
   * @param targetType 资源类型过滤：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE（可选）
   * @return 置顶收藏列表
   */
  @GetMapping("pinned")
  @Operation(summary = "获取置顶收藏")
  public ResultVO<List<FavoritePinnedDTO>> getPinnedFavorites(
    @Parameter(description = "企业空间ID", required = true)
    @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "资源类型过滤：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE")
    @RequestParam(value = "targetType", required = false) String targetType, @RequestParam("tenantId") Long tenantId) {
    Assert.notNull(spaceId, "企业空间ID不能为空");
    return ResultVO.success(favoriteService.getPinnedFavorites(tenantId, spaceId, targetType));
  }

  /**
   * 获取收藏列表（分页）
   *
   * @param pageNum 页码，默认1
   * @param pageSize 每页大小，默认20
   * @param targetType 资源类型过滤：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE（可选）
   * @return 分页收藏列表
   */
  @GetMapping("list")
  @Operation(summary = "获取收藏列表")
  public ResultVO<PageInfo<FavoriteListDTO>> getFavoriteList(
    @Parameter(description = "企业空间ID", required = true)
    @RequestParam("spaceId") Long spaceId,
    @Parameter(description = "页码，默认1") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    @Parameter(description = "每页大小，默认20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
    @Parameter(description = "资源类型过滤：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE")
    @RequestParam(value = "targetType", required = false) String targetType,
    @RequestParam("tenantId") Long tenantId) {

    Assert.notNull(spaceId, "企业空间ID不能为空");
    Assert.isTrue(pageNum > 0, FavoriteConsts.ERROR_MSG_PAGE_NUM_INVALID);
    Assert.isTrue(pageSize > 0, FavoriteConsts.ERROR_MSG_PAGE_SIZE_INVALID);

    return ResultVO.success(favoriteService.getFavoriteList(tenantId, spaceId, pageNum, pageSize, targetType));
  }

  /**
   * 添加收藏
   *
   * @param request 添加收藏请求
   * @return 添加收藏结果
   */
  @PostMapping("add")
  @Operation(summary = "添加收藏")
  public ResultVO<AddFavoriteResponseDTO> addFavorite(@RequestBody AddFavoriteRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getTargetId(), "目标资源ID不能为空");
    Assert.hasText(request.getTargetType(), "目标类型不能为空");
    Assert.notNull(request.getSpaceId(), "企业空间ID不能为空");

    return ResultVO.success(favoriteService.addFavorite(request));
  }

  /**
   * 取消收藏
   *
   * @param request 取消收藏请求
   * @return 取消收藏结果
   */
  @PostMapping("remove")
  @Operation(summary = "取消收藏")
  public ResultVO<Void> removeFavorite(@RequestBody RemoveFavoriteRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getTargetId(), "目标资源ID不能为空");
    Assert.hasText(request.getTargetType(), "目标类型不能为空");
    Assert.notNull(request.getSpaceId(), "企业空间ID不能为空");

    return favoriteService.removeFavorite(request);
  }

  /**
   * 设置收藏置顶
   *
   * @param request 设置置顶请求
   * @return 设置置顶结果
   */
  @PostMapping("pin")
  @Operation(summary = "设置收藏置顶")
  public ResultVO<FavoriteOperationResponseDTO> pinFavorite(@RequestBody PinFavoriteRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFavoriteId(), "收藏ID不能为空");
    Assert.notNull(request.getSpaceId(), "企业空间ID不能为空");

    return ResultVO.success(favoriteService.pinFavorite(request));
  }

  /**
   * 取消收藏置顶
   *
   * @param request 取消置顶请求
   * @return 取消置顶结果
   */
  @PostMapping("unpin")
  @Operation(summary = "取消收藏置顶")
  public ResultVO<FavoriteOperationResponseDTO> unpinFavorite(@RequestBody UnpinFavoriteRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getFavoriteId(), "收藏ID不能为空");
    Assert.notNull(request.getSpaceId(), "企业空间ID不能为空");

    return ResultVO.success(favoriteService.unpinFavorite(request));
  }
}


