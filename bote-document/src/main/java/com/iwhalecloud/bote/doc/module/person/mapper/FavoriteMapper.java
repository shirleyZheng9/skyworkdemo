package com.iwhalecloud.bote.doc.module.person.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoriteListDTO;
import com.iwhalecloud.bote.doc.module.person.dto.favorite.FavoritePinnedDTO;
import com.iwhalecloud.bote.doc.module.person.entity.UserFavoriteEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 收藏模块 Mapper
 *
 * @author lizuyin
 * @since 2025-08-21
 */
public interface FavoriteMapper {

  List<FavoritePinnedDTO> selectPinnedDocumentOrFolderFavorites(@Param("tenantId") Long tenantId,
    @Param("userId") Long userId, @Param("spaceId") Long spaceId, @Param("targetType") String targetType);

  List<FavoritePinnedDTO> selectPinnedLibraryFavorites(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("targetType") String targetType);

  List<FavoritePinnedDTO> selectPinnedKnowledgeFavorites(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("targetType") String targetType);

  /**
   * 查询所有类型的置顶收藏列表（UNION 查询）
   */
  List<FavoritePinnedDTO> selectPinnedAllFavorites(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId);

  /**
   * 分页查询收藏列表：文档或文件夹
   */
  Page<FavoriteListDTO> selectFavoriteDocumentOrFolderList(@Param("tenantId") Long tenantId,
    @Param("userId") Long userId, @Param("spaceId") Long spaceId, @Param("targetType") String targetType,
    RowBounds rowBounds);

  /**
   * 分页查询收藏列表：文档库
   */
  Page<FavoriteListDTO> selectFavoriteLibraryList(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("targetType") String targetType, RowBounds rowBounds);

  /**
   * 分页查询收藏列表：知识库
   */
  Page<FavoriteListDTO> selectFavoriteKnowledgeList(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("targetType") String targetType, RowBounds rowBounds);

  /**
   * 分页查询收藏列表：合并所有类型（文档/文件夹、文档库、知识库）
   */
  Page<FavoriteListDTO> selectFavoriteAllList(@Param("tenantId") Long tenantId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, RowBounds rowBounds);

  /**
   * 检查用户是否已收藏指定资源
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 是否已收藏
   */
  boolean existsFavorite(@Param("userId") Long userId, @Param("spaceId") Long spaceId,
    @Param("targetId") String targetId, @Param("targetType") String targetType, @Param("tenantId") Long tenantId);

  /**
   * 获取用户收藏指定资源的收藏ID
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 收藏ID
   */
  Long getFavoriteId(@Param("userId") Long userId, @Param("targetId") String targetId,
    @Param("targetType") String targetType);

  /**
   * 插入收藏记录
   *
   * @param favorite 收藏实体
   * @return 影响行数
   */
  int insert(UserFavoriteEntity favorite);

  /**
   * 根据收藏ID和用户ID查询收藏记录
   *
   * @param favoriteId 收藏ID
   * @param userId 用户ID
   * @return 收藏实体
   */
  UserFavoriteEntity selectByFavoriteIdAndUserId(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId);

  /**
   * 删除收藏记录（软删除）
   *
   * @param favoriteId 收藏ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int deleteFavorite(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId);

  /**
   * 根据目标资源删除收藏记录（软删除）
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 影响行数
   */
  int deleteFavoriteByTarget(@Param("userId") Long userId, @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId,
    @Param("targetId") String targetId, @Param("targetType") String targetType);

  /**
   * 根据租户和目标资源删除所有收藏记录（软删除）
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @param spaceId 空间ID
   * @return 影响行数
   */
  int deleteTenantFavoriteByTarget(@Param("tenantId") Long tenantId, @Param("targetId") String targetId,
    @Param("targetType") String targetType, @Param("spaceId") Long spaceId);

  /**
   * 更新收藏置顶状态
   *
   * @param favoriteId 收藏ID
   * @param userId 用户ID
   * @param isPinned 是否置顶
   * @param pinOrder 置顶排序
   * @return 影响行数
   */
  int updatePinStatus(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId,
    @Param("spaceId") Long spaceId, @Param("isPinned") String isPinned, @Param("pinOrder") Integer pinOrder,
    @Param("tenantId") Long tenantId);

  /**
   * 获取用户收藏的最大置顶排序号
   *
   * @param userId 用户ID
   * @return 最大置顶排序号
   */
  Integer getMaxPinOrder(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 检查知识库是否存在
   *
   * @param spaceId 租户ID
   * @param kbCode 知识库ID
   * @return 是否存在
   */
  boolean existsKnowledgeBaseByKbCode(@Param("tenantId") Long tenantId, @Param("kbCode") String kbCode,
    @Param("spaceId") Long spaceId, @Param("platform")  String platform);

  /**
   * 恢复无效收藏记录为有效状态
   *
   * @param favoriteId 收藏ID
   * @param userId 用户ID
   * @return 影响行数
   */
  int restoreFavorite(@Param("favoriteId") Long favoriteId, @Param("userId") Long userId);

  /**
   * 根据用户ID和目标资源查询收藏记录（无论状态）
   *
   * @param userId 用户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 收藏实体
   */
  UserFavoriteEntity selectByUserIdAndTarget(@Param("userId") Long userId, @Param("spaceId") Long spaceId,
    @Param("targetId") String targetId, @Param("targetType") String targetType, @Param("tenantId") Long tenantId);
}


