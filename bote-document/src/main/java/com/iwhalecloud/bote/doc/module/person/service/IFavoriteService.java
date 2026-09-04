package com.iwhalecloud.bote.doc.module.person.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.AddFavoriteResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteListDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteOperationResponseDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoritePinnedDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.PinFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.RemoveFavoriteRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.UnpinFavoriteRequestDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 收藏模块 Service 接口
 *
 * @author lizuyin
 * @since 2025-08-21
 */
public interface IFavoriteService {

  /**
   * 获取置顶收藏列表
   *
   * @param spaceId 企业空间ID
   * @param targetType 资源类型过滤（可选）：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE
   * @return 置顶收藏列表
   */
  List<FavoritePinnedDTO> getPinnedFavorites(Long tenantId, Long spaceId, String targetType);

  /**
   * 获取收藏列表（分页）
   *
   * @param spaceId 企业空间ID
   * @param pageNum 页码，默认1
   * @param pageSize 每页大小，默认20
   * @param targetType 资源类型过滤（可选）：DOCUMENT、FOLDER、LIBRARY、KNOWLEDGE
   * @return 分页收藏列表
   */
  PageInfo<FavoriteListDTO> getFavoriteList(Long tenantId, Long spaceId, Integer pageNum, Integer pageSize, String targetType);

  /**
   * 添加收藏
   *
   * @param request 添加收藏请求
   * @return 添加收藏结果
   */
  AddFavoriteResponseDTO addFavorite(AddFavoriteRequestDTO request);

  /**
   * 取消收藏
   *
   * @param request 取消收藏请求
   * @return 取消收藏结果
   */
  ResultVO<Void> removeFavorite(RemoveFavoriteRequestDTO request);

  /**
   * 删除租户维度收藏数据
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标资源类型
   * @param spaceId 空间ID
   * @return 删除结果
   */
  ResultVO<Void> removeTenantFavorite(Long tenantId, String targetId, String targetType, Long spaceId);

  /**
   * 设置收藏置顶
   *
   * @param request 设置置顶请求
   * @return 设置置顶结果
   */
  FavoriteOperationResponseDTO pinFavorite(PinFavoriteRequestDTO request);

  /**
   * 取消收藏置顶
   *
   * @param request 取消置顶请求
   * @return 取消置顶结果
   */
  FavoriteOperationResponseDTO unpinFavorite(UnpinFavoriteRequestDTO request);
}


